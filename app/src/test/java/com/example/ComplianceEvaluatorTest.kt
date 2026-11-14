package com.example

import com.example.ai.AiLcExtraction
import com.example.ai.DocumentSecurityUtil
import com.example.data.ComplianceEvaluator
import com.example.data.ComplianceSeedData
import com.example.data.LcStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ComplianceEvaluatorTest {

    @Test
    fun sha256_computesDeterministicDigest() {
        val digest1 = DocumentSecurityUtil.computeSha256("MERIDIAN_LC_TEST".toByteArray())
        val digest2 = DocumentSecurityUtil.computeSha256("MERIDIAN_LC_TEST".toByteArray())
        assertEquals(64, digest1.length)
        assertEquals(digest1, digest2)
    }

    @Test
    fun evaluate_blocksSanctionedEntityAndDualUseGoods() {
        val extraction = AiLcExtraction(
            lcNumber = "LC-TEST-991",
            lcType = "IRREVOCABLE",
            applicantName = "Severny Port Maritime Logistics LLC",
            applicantCountry = "Russia",
            beneficiaryName = "Test Supplier Ltd",
            beneficiaryCountry = "Turkey",
            issuingBank = "Test Bank",
            issuingBankBic = "TESTXXXX",
            advisingBank = "Advising Bank",
            advisingBankBic = "ADVBXXXX",
            currency = "USD",
            amountUsd = 6_000_000.0,
            expiryDate = "2026-12-01",
            latestShipmentDate = "2026-12-10",
            portOfLoading = "Yokohama",
            portOfDischarge = "Kaliningrad",
            goodsDescription = "5-Axis CNC Precision Machining Center",
            hsCode = "8457.10",
            documentTypesDetected = listOf("SWIFT MT700"),
            ucpFindings = emptyList(),
            dualUseFlagged = false,
            dualUseEccn = "",
            dualUseRationale = "",
            sanctionsIndicators = emptyList(),
            overallAiSummary = "Test LC"
        )

        val evaluated = ComplianceEvaluator.evaluate(
            aiResult = extraction,
            fileName = "test.pdf",
            mimeType = "application/pdf",
            sourceUri = "",
            sha256Hex = "abc123",
            sanctions = ComplianceSeedData.sanctions(),
            rules = ComplianceSeedData.rules(),
            limits = ComplianceSeedData.exposureLimits(),
            existingLcs = emptyList()
        )

        assertEquals(LcStatus.BLOCKED.name, evaluated.entity.status)
        assertTrue(evaluated.entity.sanctionsFlagged)
        assertTrue(evaluated.entity.dualUseFlagged)
        assertTrue(evaluated.limitAlerts.any { it.startsWith("BREACH") })
        assertTrue(evaluated.discrepancies.any { it.code == "UCP600-Art6" })
    }

    @Test
    fun evaluate_clearsCompliantCivilianTradeWithinLimits() {
        val extraction = AiLcExtraction(
            lcNumber = "LC-TEST-102",
            lcType = "IRREVOCABLE",
            applicantName = "Nordic Retail GmbH",
            applicantCountry = "Germany",
            beneficiaryName = "Pacific Textiles Pte Ltd",
            beneficiaryCountry = "Singapore",
            issuingBank = "Deutsche Handelsbank AG",
            issuingBankBic = "DEUTDEFFXXX",
            advisingBank = "Meridian Pacific Bank",
            advisingBankBic = "MPBCSGSGXXX",
            currency = "USD",
            amountUsd = 250_000.0,
            expiryDate = "2027-06-30",
            latestShipmentDate = "2027-06-01",
            portOfLoading = "Singapore",
            portOfDischarge = "Hamburg",
            goodsDescription = "100% Organic Cotton Apparel",
            hsCode = "6109.10",
            documentTypesDetected = listOf("SWIFT MT700", "Commercial Invoice"),
            ucpFindings = emptyList(),
            dualUseFlagged = false,
            dualUseEccn = "NONE",
            dualUseRationale = "Civilian apparel.",
            sanctionsIndicators = emptyList(),
            overallAiSummary = "Compliant trade transaction."
        )

        val evaluated = ComplianceEvaluator.evaluate(
            aiResult = extraction,
            fileName = "clean.pdf",
            mimeType = "application/pdf",
            sourceUri = "",
            sha256Hex = "def456",
            sanctions = ComplianceSeedData.sanctions(),
            rules = ComplianceSeedData.rules(),
            limits = ComplianceSeedData.exposureLimits(),
            existingLcs = emptyList()
        )

        assertEquals(LcStatus.CLEARED.name, evaluated.entity.status)
        assertFalse(evaluated.entity.sanctionsFlagged)
        assertFalse(evaluated.entity.dualUseFlagged)
        assertTrue(evaluated.limitAlerts.isEmpty())
    }
}
