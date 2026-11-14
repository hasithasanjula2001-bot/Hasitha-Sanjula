package com.example.data

import com.example.ai.AiLcExtraction
import kotlin.math.min

data class ScreeningQueryResult(
    val query: String,
    val matchedSanctions: List<SanctionEntryEntity>,
    val matchedRules: List<ComplianceRuleEntity>,
    val matchedLimits: List<ExposureLimitEntity>
) {
    val hasAnyHit: Boolean
        get() = matchedSanctions.isNotEmpty() || matchedRules.any { it.category == RuleCategory.DUAL_USE.name }
}

data class EvaluatedLcResult(
    val entity: VerifiedLcEntity,
    val discrepancies: List<LcDiscrepancy>,
    val sanctionsMatches: List<String>,
    val limitAlerts: List<String>,
    val documentTypes: List<String>
)

object ComplianceEvaluator {

    fun evaluate(
        aiResult: AiLcExtraction,
        fileName: String,
        mimeType: String,
        sourceUri: String,
        sha256Hex: String,
        sanctions: List<SanctionEntryEntity>,
        rules: List<ComplianceRuleEntity>,
        limits: List<ExposureLimitEntity>,
        existingLcs: List<VerifiedLcEntity>
    ): EvaluatedLcResult {
        val searchCorpus = listOf(
            aiResult.applicantName,
            aiResult.applicantCountry,
            aiResult.beneficiaryName,
            aiResult.beneficiaryCountry,
            aiResult.issuingBank,
            aiResult.issuingBankBic,
            aiResult.advisingBank,
            aiResult.advisingBankBic,
            aiResult.portOfLoading,
            aiResult.portOfDischarge,
            aiResult.goodsDescription,
            aiResult.hsCode
        ).joinToString(" | ").lowercase()

        val sanctionsMatches = mutableListOf<String>()
        sanctionsMatches.addAll(aiResult.sanctionsIndicators.filter { it.isNotBlank() })

        var sanctionScorePenalty = 0
        for (entry in sanctions) {
            val tokens = buildList {
                add(entry.entityName.lowercase())
                if (entry.countryName.length > 3) add(entry.countryName.lowercase())
                entry.keywords.split(",").map { it.trim().lowercase() }.filter { it.length >= 3 }.forEach { add(it) }
            }
            val hit = tokens.any { token -> searchCorpus.contains(token) }
            if (hit) {
                val msg = "[${entry.program}] ${entry.entityName} (${entry.countryIso}) — ${entry.riskTier} risk: ${entry.notes}"
                if (msg !in sanctionsMatches) {
                    sanctionsMatches.add(msg)
                }
                sanctionScorePenalty += when (entry.riskTier) {
                    RiskTier.CRITICAL.name -> 45
                    RiskTier.HIGH.name -> 30
                    else -> 18
                }
            }
        }

        // Dual-use rulebook cross-check (HS prefixes & controlled goods keywords)
        val cleanHs = aiResult.hsCode.replace(".", "").trim()
        val matchedDualUseRules = rules.filter { rule ->
            rule.category == RuleCategory.DUAL_USE.name && (
                rule.hsPrefixes.split(",").map { it.trim() }.filter { it.isNotEmpty() }.any { prefix ->
                    cleanHs.startsWith(prefix)
                } ||
                    rule.keywords.split(",").map { it.trim().lowercase() }.filter { it.length >= 3 }.any { kw ->
                        searchCorpus.contains(kw)
                    }
                )
        }

        val discrepancies = aiResult.ucpFindings.toMutableList()
        if (aiResult.expiryDate.isNotBlank() && aiResult.latestShipmentDate.isNotBlank() &&
            aiResult.expiryDate < aiResult.latestShipmentDate
        ) {
            discrepancies.add(
                LcDiscrepancy(
                    code = "UCP600-Art6",
                    severity = "CRITICAL",
                    title = "Expiry precedes latest shipment date",
                    detail = "LC expiry (${aiResult.expiryDate}) is earlier than latest shipment (${aiResult.latestShipmentDate}).",
                    recommendation = "Amend Field :31D: (Expiry) or :44C: (Latest Shipment) before advising."
                )
            )
        }

        val dualUseFlagged = aiResult.dualUseFlagged || matchedDualUseRules.isNotEmpty()
        val combinedEccn = when {
            aiResult.dualUseEccn.isNotBlank() -> aiResult.dualUseEccn
            matchedDualUseRules.isNotEmpty() -> matchedDualUseRules.joinToString(", ") { it.ruleCode }
            else -> "NONE"
        }
        val combinedDualUseRationale = buildString {
            if (aiResult.dualUseRationale.isNotBlank()) append(aiResult.dualUseRationale)
            if (matchedDualUseRules.isNotEmpty()) {
                if (isNotEmpty()) append(" ")
                append("Matched local control schedule: ")
                append(matchedDualUseRules.joinToString("; ") { "${it.ruleCode} (${it.title})" })
            }
        }.ifBlank { "No dual-use goods control indicators detected." }

        // Exposure limits validation
        val limitAlerts = mutableListOf<String>()
        var limitPenalty = 0
        for (limit in limits.filter { it.isActive }) {
            val currentCommitted = computeCommittedExposureUsd(limit, existingLcs)
            val applies = when (limit.scope) {
                LimitScope.SINGLE_TX.name -> true
                LimitScope.DUAL_USE_CAP.name -> dualUseFlagged
                LimitScope.COUNTRY.name -> {
                    val target = limit.targetName.lowercase()
                    val iso = limit.targetIdentifier.lowercase()
                    searchCorpus.contains(target) || (iso.length >= 2 && searchCorpus.contains(iso))
                }
                LimitScope.ISSUING_BANK.name -> {
                    val target = limit.targetName.lowercase()
                    val bic = limit.targetIdentifier.lowercase()
                    aiResult.issuingBank.lowercase().contains(target) ||
                        (bic.length >= 4 && aiResult.issuingBankBic.lowercase().contains(bic))
                }
                else -> false
            }

            if (applies && limit.maxCapUsd > 0) {
                val projected = if (limit.scope == LimitScope.SINGLE_TX.name) {
                    aiResult.amountUsd
                } else {
                    currentCommitted + aiResult.amountUsd
                }
                val utilizationPct = (projected / limit.maxCapUsd) * 100.0
                if (projected > limit.maxCapUsd) {
                    limitAlerts.add(
                        "BREACH [${limit.targetName}]: Projected ${formatUsdCompact(projected)} exceeds cap ${formatUsdCompact(limit.maxCapUsd)} (${utilizationPct.toInt()}%)."
                    )
                    limitPenalty += 25
                } else if (utilizationPct >= limit.alertThresholdPercent) {
                    limitAlerts.add(
                        "WARNING [${limit.targetName}]: Utilization reaches ${utilizationPct.toInt()}% of ${formatUsdCompact(limit.maxCapUsd)} cap."
                    )
                    limitPenalty += 12
                }
            }
        }

        val ucpPenalty = discrepancies.sumOf { d ->
            when (d.severity.uppercase()) {
                "CRITICAL" -> 22
                "HIGH" -> 14
                "MEDIUM" -> 8
                else -> 4
            }
        }
        val dualUsePenalty = if (dualUseFlagged) 22 else 0

        val totalRiskScore = min(100, sanctionScorePenalty + ucpPenalty + dualUsePenalty + limitPenalty)
        val sanctionsFlagged = sanctionsMatches.isNotEmpty()
        val hasLimitBreach = limitAlerts.any { it.startsWith("BREACH") }
        val hasCriticalUcp = discrepancies.any { it.severity.equals("CRITICAL", ignoreCase = true) }

        val status = when {
            sanctionsFlagged || totalRiskScore >= 75 -> LcStatus.BLOCKED
            dualUseFlagged || hasLimitBreach || hasCriticalUcp || discrepancies.isNotEmpty() || limitAlerts.isNotEmpty() || totalRiskScore >= 30 ->
                LcStatus.REVIEW_REQUIRED
            else -> LcStatus.CLEARED
        }

        val entity = VerifiedLcEntity(
            lcNumber = aiResult.lcNumber.ifBlank { "LC-UNPARSED" },
            lcType = aiResult.lcType.ifBlank { "IRREVOCABLE" },
            applicantName = aiResult.applicantName.ifBlank { "Unknown Applicant" },
            applicantCountry = aiResult.applicantCountry.ifBlank { "N/A" },
            beneficiaryName = aiResult.beneficiaryName.ifBlank { "Unknown Beneficiary" },
            beneficiaryCountry = aiResult.beneficiaryCountry.ifBlank { "N/A" },
            issuingBank = aiResult.issuingBank.ifBlank { "Unspecified Bank" },
            issuingBankBic = aiResult.issuingBankBic.ifBlank { "N/A" },
            advisingBank = aiResult.advisingBank.ifBlank { "Unspecified Bank" },
            advisingBankBic = aiResult.advisingBankBic.ifBlank { "N/A" },
            currency = aiResult.currency.ifBlank { "USD" },
            amountUsd = aiResult.amountUsd,
            expiryDate = aiResult.expiryDate.ifBlank { "Not specified" },
            latestShipmentDate = aiResult.latestShipmentDate.ifBlank { "Not specified" },
            portOfLoading = aiResult.portOfLoading.ifBlank { "Not specified" },
            portOfDischarge = aiResult.portOfDischarge.ifBlank { "Not specified" },
            goodsDescription = aiResult.goodsDescription.ifBlank { "Not extracted" },
            hsCode = aiResult.hsCode.ifBlank { "N/A" },
            documentName = fileName,
            mimeType = mimeType,
            sourceUri = sourceUri,
            sha256Hash = sha256Hex,
            documentTypesJson = ComplianceJsonCodec.encodeStringList(aiResult.documentTypesDetected),
            riskScore = totalRiskScore,
            status = status.name,
            dualUseFlagged = dualUseFlagged,
            dualUseEccn = combinedEccn,
            dualUseRationale = combinedDualUseRationale,
            sanctionsFlagged = sanctionsFlagged,
            sanctionsMatchesJson = ComplianceJsonCodec.encodeStringList(sanctionsMatches),
            discrepanciesJson = ComplianceJsonCodec.encodeDiscrepancies(discrepancies),
            limitAlertsJson = ComplianceJsonCodec.encodeStringList(limitAlerts),
            aiSummary = aiResult.overallAiSummary.ifBlank { "Verification completed." }
        )

        return EvaluatedLcResult(
            entity = entity,
            discrepancies = discrepancies,
            sanctionsMatches = sanctionsMatches,
            limitAlerts = limitAlerts,
            documentTypes = aiResult.documentTypesDetected
        )
    }

    fun computeCommittedExposureUsd(
        limit: ExposureLimitEntity,
        lcs: List<VerifiedLcEntity>
    ): Double {
        val activeLcs = lcs.filter { it.status != LcStatus.BLOCKED.name }
        val delta = when (limit.scope) {
            LimitScope.COUNTRY.name -> activeLcs.filter { lc ->
                val target = limit.targetName.lowercase()
                val iso = limit.targetIdentifier.lowercase()
                lc.applicantCountry.lowercase().contains(target) ||
                    lc.beneficiaryCountry.lowercase().contains(target) ||
                    lc.portOfLoading.lowercase().contains(target) ||
                    lc.portOfDischarge.lowercase().contains(target) ||
                    (iso.length >= 2 && (
                        lc.applicantCountry.equals(iso, ignoreCase = true) ||
                            lc.beneficiaryCountry.equals(iso, ignoreCase = true)
                        ))
            }.sumOf { it.amountUsd }

            LimitScope.ISSUING_BANK.name -> activeLcs.filter { lc ->
                val target = limit.targetName.lowercase()
                val bic = limit.targetIdentifier.lowercase()
                lc.issuingBank.lowercase().contains(target) ||
                    (bic.length >= 4 && lc.issuingBankBic.lowercase().contains(bic))
            }.sumOf { it.amountUsd }

            LimitScope.DUAL_USE_CAP.name -> activeLcs.filter { it.dualUseFlagged }.sumOf { it.amountUsd }
            LimitScope.SINGLE_TX.name -> activeLcs.maxOfOrNull { it.amountUsd } ?: 0.0
            else -> 0.0
        }
        return if (limit.scope == LimitScope.SINGLE_TX.name) {
            maxOf(limit.baselineCommittedUsd, delta)
        } else {
            limit.baselineCommittedUsd + delta
        }
    }

    fun quickScreen(
        rawQuery: String,
        sanctions: List<SanctionEntryEntity>,
        rules: List<ComplianceRuleEntity>,
        limits: List<ExposureLimitEntity>
    ): ScreeningQueryResult {
        val q = rawQuery.trim().lowercase()
        if (q.isEmpty()) {
            return ScreeningQueryResult(rawQuery, emptyList(), emptyList(), emptyList())
        }
        val cleanHs = q.replace(".", "")

        val matchedSanctions = sanctions.filter { s ->
            s.entityName.lowercase().contains(q) ||
                s.countryName.lowercase().contains(q) ||
                s.countryIso.lowercase() == q ||
                s.keywords.lowercase().contains(q) ||
                s.program.lowercase().contains(q)
        }
        val matchedRules = rules.filter { r ->
            r.ruleCode.lowercase().contains(q) ||
                r.title.lowercase().contains(q) ||
                r.keywords.lowercase().contains(q) ||
                r.hsPrefixes.split(",").map { it.trim() }.filter { it.isNotEmpty() }.any { prefix ->
                    cleanHs.startsWith(prefix) || prefix.startsWith(cleanHs)
                }
        }
        val matchedLimits = limits.filter { l ->
            l.targetName.lowercase().contains(q) || l.targetIdentifier.lowercase().contains(q)
        }
        return ScreeningQueryResult(rawQuery, matchedSanctions, matchedRules, matchedLimits)
    }

    fun formatUsdCompact(amount: Double): String {
        return when {
            amount >= 1_000_000.0 -> String.format("$%.2fM", amount / 1_000_000.0)
            amount >= 1_000.0 -> String.format("$%.1fK", amount / 1_000.0)
            else -> String.format("$%.0f", amount)
        }
    }
}
