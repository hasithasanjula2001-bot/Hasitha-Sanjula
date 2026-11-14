package com.example.data

import android.content.Context
import android.net.Uri
import com.example.ai.AiLcExtraction
import com.example.ai.DocumentSecurityUtil
import com.example.ai.LcAiVerificationService
import com.example.ai.PreparedDocument
import kotlinx.coroutines.flow.Flow

class TradeComplianceRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val aiService: LcAiVerificationService = LcAiVerificationService()
) {
    private val lcDao = database.verifiedLcDao()
    private val sanctionDao = database.sanctionEntryDao()
    private val ruleDao = database.complianceRuleDao()
    private val limitDao = database.exposureLimitDao()

    val verifiedLcs: Flow<List<VerifiedLcEntity>> = lcDao.observeAllLcs()
    val sanctions: Flow<List<SanctionEntryEntity>> = sanctionDao.observeAllSanctions()
    val rules: Flow<List<ComplianceRuleEntity>> = ruleDao.observeAllRules()
    val exposureLimits: Flow<List<ExposureLimitEntity>> = limitDao.observeAllLimits()

    fun observeLcById(id: Long): Flow<VerifiedLcEntity?> = lcDao.observeLcById(id)

    suspend fun ensureSeeded() {
        if (sanctionDao.count() == 0) {
            sanctionDao.insertAll(ComplianceSeedData.sanctions())
        }
        if (ruleDao.count() == 0) {
            ruleDao.insertAll(ComplianceSeedData.rules())
        }
        if (limitDao.count() == 0) {
            limitDao.insertAll(ComplianceSeedData.exposureLimits())
        }
        if (lcDao.count() == 0) {
            lcDao.insertAll(ComplianceSeedData.initialVerifiedLcs())
        }
    }

    fun prepareDocumentForVerification(uri: Uri, fallbackMime: String? = null): Result<PreparedDocument> {
        return DocumentSecurityUtil.prepareDocument(context, uri, fallbackMime)
    }

    suspend fun verifyPreparedDocument(doc: PreparedDocument): Result<EvaluatedLcResult> {
        ensureSeeded()
        val aiResult = aiService.analyzeDocument(doc)
        return aiResult.map { extraction ->
            evaluateExtraction(
                extraction = extraction,
                fileName = doc.fileName,
                mimeType = doc.mimeType,
                sourceUri = doc.sourceUriString,
                sha256Hex = doc.sha256HashSafe()
            )
        }
    }

    suspend fun evaluateSampleOrExtracted(
        extraction: AiLcExtraction,
        fileName: String,
        mimeType: String,
        sourceUri: String,
        sha256Hex: String
    ): EvaluatedLcResult {
        ensureSeeded()
        return evaluateExtraction(extraction, fileName, mimeType, sourceUri, sha256Hex)
    }

    private suspend fun evaluateExtraction(
        extraction: AiLcExtraction,
        fileName: String,
        mimeType: String,
        sourceUri: String,
        sha256Hex: String
    ): EvaluatedLcResult {
        val sanctionsSnapshot = sanctionDao.getAllSnapshot()
        val rulesSnapshot = ruleDao.getAllSnapshot()
        val limitsSnapshot = limitDao.getAllSnapshot()
        val existingLcs = lcDao.getAllLcsSnapshot()
        return ComplianceEvaluator.evaluate(
            aiResult = extraction,
            fileName = fileName,
            mimeType = mimeType,
            sourceUri = sourceUri,
            sha256Hex = sha256Hex,
            sanctions = sanctionsSnapshot,
            rules = rulesSnapshot,
            limits = limitsSnapshot,
            existingLcs = existingLcs
        )
    }

    suspend fun saveVerifiedLc(entity: VerifiedLcEntity): Long {
        return lcDao.insertLc(entity)
    }

    suspend fun deleteLc(id: Long) {
        lcDao.deleteLcById(id)
    }

    suspend fun updateExposureLimit(limit: ExposureLimitEntity) {
        limitDao.updateLimit(limit.copy(updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun addExposureLimit(limit: ExposureLimitEntity): Long {
        return limitDao.insertLimit(limit)
    }

    suspend fun runQuickScreen(query: String): ScreeningQueryResult {
        ensureSeeded()
        return ComplianceEvaluator.quickScreen(
            rawQuery = query,
            sanctions = sanctionDao.getAllSnapshot(),
            rules = ruleDao.getAllSnapshot(),
            limits = limitDao.getAllSnapshot()
        )
    }

    fun verifyStoredDocumentIntegrity(uriString: String, expectedSha256: String): Result<Boolean> {
        return DocumentSecurityUtil.verifyUriHash(context, uriString, expectedSha256)
    }

    private fun PreparedDocument.sha256HashSafe(): String = sha256Hex
}
