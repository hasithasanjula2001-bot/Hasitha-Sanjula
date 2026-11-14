package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

enum class LcStatus(val label: String) {
    CLEARED("Cleared"),
    REVIEW_REQUIRED("Review Required"),
    BLOCKED("Blocked")
}

enum class RiskTier(val label: String) {
    CRITICAL("Critical"),
    HIGH("High"),
    ELEVATED("Elevated"),
    LOW("Low")
}

enum class SanctionProgram(val label: String) {
    OFAC_SDN("OFAC SDN"),
    EU_CONSOLIDATED("EU Consolidated"),
    UN_SC("UN Security Council")
}

enum class RuleCategory(val label: String) {
    UCP_600("UCP 600"),
    ISBP_821("ISBP 821"),
    DUAL_USE("Dual-Use"),
    SANCTIONS_POLICY("Sanctions Policy")
}

enum class LimitScope(val label: String) {
    COUNTRY("Country Exposure"),
    ISSUING_BANK("Issuing Bank Cap"),
    SINGLE_TX("Single LC Limit"),
    DUAL_USE_CAP("Dual-Use Cap")
}

data class LcDiscrepancy(
    val code: String,
    val severity: String,
    val title: String,
    val detail: String,
    val recommendation: String
)

@Entity(tableName = "verified_lcs")
data class VerifiedLcEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    val documentName: String,
    val mimeType: String,
    val sourceUri: String,
    val sha256Hash: String,
    val documentTypesJson: String,
    val riskScore: Int,
    val status: String,
    val dualUseFlagged: Boolean,
    val dualUseEccn: String,
    val dualUseRationale: String,
    val sanctionsFlagged: Boolean,
    val sanctionsMatchesJson: String,
    val discrepanciesJson: String,
    val limitAlertsJson: String,
    val aiSummary: String,
    val verifiedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "sanction_entries")
data class SanctionEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityName: String,
    val program: String,
    val countryIso: String,
    val countryName: String,
    val entityType: String, // PERSON, ENTITY, VESSEL, PORT, JURISDICTION
    val riskTier: String,
    val updatedDate: String,
    val keywords: String, // comma-separated screening tokens
    val notes: String
)

@Entity(tableName = "compliance_rules")
data class ComplianceRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ruleCode: String,
    val category: String,
    val title: String,
    val summary: String,
    val severity: String,
    val effectiveDate: String,
    val hsPrefixes: String, // comma-separated HS prefixes if applicable
    val keywords: String
)

@Entity(tableName = "exposure_limits")
data class ExposureLimitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scope: String,
    val targetName: String,
    val targetIdentifier: String, // Country ISO, BIC, or "ALL"
    val maxCapUsd: Double,
    val baselineCommittedUsd: Double,
    val alertThresholdPercent: Int,
    val isActive: Boolean = true,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

object ComplianceJsonCodec {
    fun encodeStringList(items: List<String>): String {
        val arr = JSONArray()
        items.forEach { arr.put(it) }
        return arr.toString()
    }

    fun decodeStringList(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }
        }.getOrDefault(emptyList())
    }

    fun encodeDiscrepancies(items: List<LcDiscrepancy>): String {
        val arr = JSONArray()
        items.forEach { d ->
            arr.put(
                JSONObject().apply {
                    put("code", d.code)
                    put("severity", d.severity)
                    put("title", d.title)
                    put("detail", d.detail)
                    put("recommendation", d.recommendation)
                }
            )
        }
        return arr.toString()
    }

    fun decodeDiscrepancies(raw: String): List<LcDiscrepancy> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val obj = arr.getJSONObject(idx)
                LcDiscrepancy(
                    code = obj.optString("code", "RULE"),
                    severity = obj.optString("severity", "HIGH"),
                    title = obj.optString("title", "Discrepancy"),
                    detail = obj.optString("detail", ""),
                    recommendation = obj.optString("recommendation", "")
                )
            }
        }.getOrDefault(emptyList())
    }
}
