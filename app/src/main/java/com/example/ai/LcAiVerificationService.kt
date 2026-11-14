package com.example.ai

import android.util.Log
import com.example.data.LcDiscrepancy
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "LcAiVerification"

data class AiLcExtraction(
    val lcNumber: String,
    val lcType: String,
    val applicantName: String,
    val applicantCountry: String,
    val beneficiaryName: String,
    val beneficiaryCountry: String,
    val issuingBank: String,
    val issuingBankBic: String,
    val advisingBank: String,
    val advisingBankBic: String,
    val currency: String,
    val amountUsd: Double,
    val expiryDate: String,
    val latestShipmentDate: String,
    val portOfLoading: String,
    val portOfDischarge: String,
    val goodsDescription: String,
    val hsCode: String,
    val documentTypesDetected: List<String>,
    val ucpFindings: List<LcDiscrepancy>,
    val dualUseFlagged: Boolean,
    val dualUseEccn: String,
    val dualUseRationale: String,
    val sanctionsIndicators: List<String>,
    val overallAiSummary: String
)

class LcAiVerificationService {

    private val lcAuditSchema = Schema.obj(
        properties = mapOf(
            "lcNumber" to Schema.string("SWIFT :20: Documentary Credit Number or reference ID"),
            "lcType" to Schema.string("SWIFT :40A: Form of Documentary Credit, e.g., IRREVOCABLE"),
            "applicantName" to Schema.string("SWIFT :50: Applicant / Importer name"),
            "applicantCountry" to Schema.string("Applicant country name or ISO code"),
            "beneficiaryName" to Schema.string("SWIFT :59: Beneficiary / Exporter name"),
            "beneficiaryCountry" to Schema.string("Beneficiary country name or ISO code"),
            "issuingBank" to Schema.string("Issuing bank institution name"),
            "issuingBankBic" to Schema.string("Issuing bank SWIFT/BIC code if visible"),
            "advisingBank" to Schema.string("Advising or negotiating bank name"),
            "advisingBankBic" to Schema.string("Advising bank SWIFT/BIC code if visible"),
            "currency" to Schema.string("3-letter ISO currency code, e.g., USD, EUR"),
            "amountUsd" to Schema.double("Transaction amount converted to approximate USD value"),
            "expiryDate" to Schema.string("SWIFT :31D: Expiry date in YYYY-MM-DD format if available"),
            "latestShipmentDate" to Schema.string("SWIFT :44C: Latest date of shipment in YYYY-MM-DD format if available"),
            "portOfLoading" to Schema.string("SWIFT :44E: Port of loading or airport of departure"),
            "portOfDischarge" to Schema.string("SWIFT :44F: Port of discharge or airport of destination"),
            "goodsDescription" to Schema.string("SWIFT :45A: Concise description of goods and Incoterms"),
            "hsCode" to Schema.string("Harmonized System (HS) tariff code if stated or inferred from goods"),
            "documentTypesDetected" to Schema.array(
                items = Schema.string(),
                description = "Trade document types identified (e.g., SWIFT MT700, Commercial Invoice, Bill of Lading)"
            ),
            "ucpFindings" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "code" to Schema.string("UCP 600 or ISBP 821 rule reference, e.g., UCP600-Art14"),
                        "severity" to Schema.enumeration(listOf("CRITICAL", "HIGH", "MEDIUM", "LOW")),
                        "title" to Schema.string("Short discrepancy title"),
                        "detail" to Schema.string("Detailed discrepancy or compliance finding"),
                        "recommendation" to Schema.string("Actionable remediation step")
                    )
                ),
                description = "Documentary compliance discrepancies or clause risks found under UCP 600 / ISBP 821"
            ),
            "dualUseFlagged" to Schema.boolean("True if goods suggest dual-use military/civilian technology under Wassenaar/EAR/EU Dual-Use"),
            "dualUseEccn" to Schema.string("Relevant ECCN or EU dual-use category code if flagged, else NONE"),
            "dualUseRationale" to Schema.string("Explanation of dual-use risk assessment"),
            "sanctionsIndicators" to Schema.array(
                items = Schema.string(),
                description = "Any sanctions red flags (high-risk ports, vessels, sanctioned clauses, transshipment hubs)"
            ),
            "overallAiSummary" to Schema.string("Executive 2-sentence compliance summary of this trade document")
        )
    )

    private val model by lazy {
        val config = generationConfig {
            responseMimeType = "application/json"
            responseSchema = lcAuditSchema
            temperature = 0.1f
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config
        )
    }

    suspend fun analyzeDocument(doc: PreparedDocument): Result<AiLcExtraction> {
        val systemInstructionPrompt = """
            You are an expert international trade finance compliance officer and ICC Certified Documentary Credit Specialist (CDCS).
            Inspect the provided Letter of Credit (LC) or supporting trade finance document (${doc.fileName}).
            1. Extract all SWIFT MT700 / documentary credit fields accurately. If a field is absent, return an empty string (or 0.0 for amountUsd).
            2. Perform strict UCP 600 & ISBP 821 discrepancy screening (e.g., ambiguous goods descriptions, stale shipping dates, missing Incoterms, non-negotiable transport clause mismatches, sanction-evasion clauses).
            3. Perform Dual-Use & Strategic Goods screening against Wassenaar Arrangement, BIS EAR, and EU Dual-Use Regulation 2021/821 (e.g., precision CNC machinery, optical/thermal sensors, aerospace alloys, cryptography/telecom, chemical precursors, semiconductors).
            4. Flag any sanctions indicators (sanctioned jurisdictions, high-risk maritime transshipment hubs, shell entity patterns, or vessel-stripping clauses).
            Return strictly structured JSON matching the schema.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                val inputContent = content {
                    when {
                        doc.bitmap != null -> {
                            image(doc.bitmap)
                            text(systemInstructionPrompt)
                        }
                        doc.textContent != null -> {
                            text(systemInstructionPrompt)
                            text("\n--- DOCUMENT PAYLOAD (${doc.fileName}) ---\n${doc.textContent.take(60_000)}")
                        }
                        doc.rawBytes != null -> {
                            inlineData(doc.rawBytes, doc.mimeType)
                            text(systemInstructionPrompt)
                        }
                        else -> {
                            throw IllegalArgumentException("Unsupported document payload format.")
                        }
                    }
                }
                model.generateContent(inputContent)
            }

            val rawJson = response.text
                ?: return Result.failure(IllegalStateException("Gemini returned an empty response."))

            Result.success(parseExtractionJson(rawJson))
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI LC verification timed out", e)
            Result.failure(
                IllegalStateException("Document analysis timed out after 60 seconds. Check connectivity and retry.")
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI LC verification failed", e)
            Result.failure(
                IllegalStateException(e.localizedMessage ?: "Firebase AI verification failed.")
            )
        }
    }

    private fun parseExtractionJson(jsonString: String): AiLcExtraction {
        val obj = JSONObject(jsonString)

        val docTypesArr = obj.optJSONArray("documentTypesDetected")
        val docTypes = if (docTypesArr != null) {
            (0 until docTypesArr.length()).mapNotNull { docTypesArr.optString(it).takeIf { s -> s.isNotBlank() } }
        } else {
            emptyList()
        }

        val findingsArr = obj.optJSONArray("ucpFindings")
        val findings = if (findingsArr != null) {
            (0 until findingsArr.length()).map { idx ->
                val item = findingsArr.getJSONObject(idx)
                LcDiscrepancy(
                    code = item.optString("code", "UCP600"),
                    severity = item.optString("severity", "MEDIUM"),
                    title = item.optString("title", "Documentary Observation"),
                    detail = item.optString("detail", ""),
                    recommendation = item.optString("recommendation", "Review original credit terms.")
                )
            }
        } else {
            emptyList()
        }

        val sanctionsArr = obj.optJSONArray("sanctionsIndicators")
        val sanctions = if (sanctionsArr != null) {
            (0 until sanctionsArr.length()).mapNotNull { sanctionsArr.optString(it).takeIf { s -> s.isNotBlank() } }
        } else {
            emptyList()
        }

        return AiLcExtraction(
            lcNumber = obj.optString("lcNumber", ""),
            lcType = obj.optString("lcType", "IRREVOCABLE"),
            applicantName = obj.optString("applicantName", ""),
            applicantCountry = obj.optString("applicantCountry", ""),
            beneficiaryName = obj.optString("beneficiaryName", ""),
            beneficiaryCountry = obj.optString("beneficiaryCountry", ""),
            issuingBank = obj.optString("issuingBank", ""),
            issuingBankBic = obj.optString("issuingBankBic", ""),
            advisingBank = obj.optString("advisingBank", ""),
            advisingBankBic = obj.optString("advisingBankBic", ""),
            currency = obj.optString("currency", "USD"),
            amountUsd = obj.optDouble("amountUsd", 0.0).let { if (it.isNaN()) 0.0 else it },
            expiryDate = obj.optString("expiryDate", ""),
            latestShipmentDate = obj.optString("latestShipmentDate", ""),
            portOfLoading = obj.optString("portOfLoading", ""),
            portOfDischarge = obj.optString("portOfDischarge", ""),
            goodsDescription = obj.optString("goodsDescription", ""),
            hsCode = obj.optString("hsCode", ""),
            documentTypesDetected = docTypes,
            ucpFindings = findings,
            dualUseFlagged = obj.optBoolean("dualUseFlagged", false),
            dualUseEccn = obj.optString("dualUseEccn", "NONE"),
            dualUseRationale = obj.optString("dualUseRationale", ""),
            sanctionsIndicators = sanctions,
            overallAiSummary = obj.optString("overallAiSummary", "")
        )
    }
}
