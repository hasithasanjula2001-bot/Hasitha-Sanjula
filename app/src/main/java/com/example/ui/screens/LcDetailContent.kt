package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ComplianceEvaluator
import com.example.data.ComplianceJsonCodec
import com.example.data.LcDiscrepancy
import com.example.data.VerifiedLcEntity
import com.example.ui.components.ComplianceStatusBadge
import com.example.ui.components.RiskScoreRing
import com.example.ui.components.TamperSealChip
import com.example.ui.theme.ElevatedNavy
import com.example.ui.theme.HairlineSlate
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MonoMetricStyle
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SanctionCrimson
import com.example.ui.theme.SanctionCrimsonMuted
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.SoftSilver
import com.example.ui.theme.SovereignGold
import com.example.ui.theme.SovereignGoldMuted
import com.example.ui.theme.SteelCyan
import com.example.ui.theme.VerifiedEmerald
import com.example.ui.theme.VerifiedEmeraldMuted
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberMuted
import com.example.viewmodel.IntegrityCheckState

@Composable
fun LcAuditReportBody(
    lc: VerifiedLcEntity,
    integrityState: IntegrityCheckState = IntegrityCheckState.Idle,
    onVerifyHash: (() -> Unit)? = null,
    onDeleteLc: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val discrepancies = remember(lc.discrepanciesJson) {
        ComplianceJsonCodec.decodeDiscrepancies(lc.discrepanciesJson)
    }
    val sanctionsMatches = remember(lc.sanctionsMatchesJson) {
        ComplianceJsonCodec.decodeStringList(lc.sanctionsMatchesJson)
    }
    val limitAlerts = remember(lc.limitAlertsJson) {
        ComplianceJsonCodec.decodeStringList(lc.limitAlertsJson)
    }
    val docTypes = remember(lc.documentTypesJson) {
        ComplianceJsonCodec.decodeStringList(lc.documentTypesJson)
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Executive Verdict Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SlateNavy,
            border = BorderStroke(1.dp, HairlineSlate)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ComplianceStatusBadge(statusName = lc.status)
                        Text(
                            text = lc.lcNumber,
                            style = MaterialTheme.typography.headlineSmall,
                            color = IceWhite
                        )
                        Text(
                            text = "${lc.currency} ${ComplianceEvaluator.formatUsdCompact(lc.amountUsd)} · ${lc.lcType}",
                            style = MonoMetricStyle,
                            color = SovereignGold
                        )
                    }
                    RiskScoreRing(
                        score = lc.riskScore,
                        label = "RISK SCORE",
                        size = 92.dp
                    )
                }

                Text(
                    text = lc.aiSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftSilver
                )

                TamperSealChip(sha256Hash = lc.sha256Hash)

                if (docTypes.isNotEmpty()) {
                    Text(
                        text = "Detected Docs: ${docTypes.joinToString(" · ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SteelCyan
                    )
                }
            }
        }

        // Sanctions Screening Section
        AuditSectionCard(
            title = "Sanctions Screening (OFAC / EU / UN)",
            icon = Icons.Filled.Gavel,
            accent = if (lc.sanctionsFlagged) SanctionCrimson else VerifiedEmerald
        ) {
            if (sanctionsMatches.isEmpty()) {
                StatusBannerRow(
                    text = "No OFAC SDN, EU Consolidated, or UN Security Council hits detected.",
                    color = VerifiedEmerald,
                    bgColor = VerifiedEmeraldMuted
                )
            } else {
                sanctionsMatches.forEach { hit ->
                    StatusBannerRow(
                        text = hit,
                        color = SanctionCrimson,
                        bgColor = SanctionCrimsonMuted
                    )
                }
            }
        }

        // Dual-Use Goods Control Section
        AuditSectionCard(
            title = "Dual-Use & Strategic Goods (HS ${lc.hsCode})",
            icon = Icons.Filled.Security,
            accent = if (lc.dualUseFlagged) WarningAmber else VerifiedEmerald
        ) {
            if (lc.dualUseFlagged) {
                StatusBannerRow(
                    text = "CONTROLLED [${lc.dualUseEccn}]: ${lc.dualUseRationale}",
                    color = WarningAmber,
                    bgColor = WarningAmberMuted
                )
            } else {
                StatusBannerRow(
                    text = lc.dualUseRationale,
                    color = VerifiedEmerald,
                    bgColor = VerifiedEmeraldMuted
                )
            }
        }

        // Exposure Limits Impact
        AuditSectionCard(
            title = "Exposure Limits & Thresholds",
            icon = Icons.Filled.Warning,
            accent = if (limitAlerts.isNotEmpty()) WarningAmber else VerifiedEmerald
        ) {
            if (limitAlerts.isEmpty()) {
                StatusBannerRow(
                    text = "Transaction remains within configured single-LC, country, and issuing-bank caps.",
                    color = VerifiedEmerald,
                    bgColor = VerifiedEmeraldMuted
                )
            } else {
                limitAlerts.forEach { alert ->
                    val isBreach = alert.startsWith("BREACH")
                    StatusBannerRow(
                        text = alert,
                        color = if (isBreach) SanctionCrimson else WarningAmber,
                        bgColor = if (isBreach) SanctionCrimsonMuted else WarningAmberMuted
                    )
                }
            }
        }

        // UCP 600 / ISBP 821 Discrepancies
        AuditSectionCard(
            title = "UCP 600 & ISBP 821 Documentary Checks",
            icon = Icons.Filled.ErrorOutline,
            accent = if (discrepancies.isEmpty()) VerifiedEmerald else SovereignGold
        ) {
            if (discrepancies.isEmpty()) {
                StatusBannerRow(
                    text = "No documentary credit discrepancies detected under UCP 600.",
                    color = VerifiedEmerald,
                    bgColor = VerifiedEmeraldMuted
                )
            } else {
                discrepancies.forEach { d ->
                    DiscrepancyItemCard(d)
                }
            }
        }

        // SWIFT MT700 Structured Fields
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SlateNavy,
            border = BorderStroke(1.dp, HairlineSlate)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Extracted SWIFT MT700 Fields",
                    style = MaterialTheme.typography.titleSmall,
                    color = IceWhite
                )
                HorizontalDivider(color = HairlineSlate)
                SwiftFieldRow(":50: Applicant", "${lc.applicantName} (${lc.applicantCountry})")
                SwiftFieldRow(":59: Beneficiary", "${lc.beneficiaryName} (${lc.beneficiaryCountry})")
                SwiftFieldRow("Issuing Bank", "${lc.issuingBank} [${lc.issuingBankBic}]")
                SwiftFieldRow("Advising Bank", "${lc.advisingBank} [${lc.advisingBankBic}]")
                SwiftFieldRow(":44E: Loading Port", lc.portOfLoading)
                SwiftFieldRow(":44F: Discharge Port", lc.portOfDischarge)
                SwiftFieldRow(":44C: Latest Shipment", lc.latestShipmentDate)
                SwiftFieldRow(":31D: Expiry Date", lc.expiryDate)
                SwiftFieldRow(":45A: Goods", lc.goodsDescription)
                SwiftFieldRow("Source File", lc.documentName)
                SwiftFieldRow("Full SHA-256", lc.sha256Hash)
            }
        }

        // Integrity Check Feedback + Action Buttons
        when (integrityState) {
            is IntegrityCheckState.VerifiedMatch -> {
                StatusBannerRow(
                    text = "SHA-256 Re-Verification Passed: Document bytes match sealed audit digest identically.",
                    color = VerifiedEmerald,
                    bgColor = VerifiedEmeraldMuted
                )
            }
            is IntegrityCheckState.TamperMismatch -> {
                StatusBannerRow(
                    text = "TAMPER ALERT: Current file SHA-256 differs from the original audited seal!",
                    color = SanctionCrimson,
                    bgColor = SanctionCrimsonMuted
                )
            }
            is IntegrityCheckState.Unavailable -> {
                StatusBannerRow(
                    text = integrityState.reason,
                    color = SteelCyan,
                    bgColor = ElevatedNavy
                )
            }
            IntegrityCheckState.Idle -> Unit
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { shareComplianceReport(context, lc, discrepancies, sanctionsMatches, limitAlerts) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("detail_share_report_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SovereignGold.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = null,
                    tint = SovereignGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Report", color = SovereignGold)
            }

            if (onVerifyHash != null) {
                Button(
                    onClick = onVerifyHash,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("detail_verify_hash_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SteelCyan,
                        contentColor = MidnightNavy
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Verify Seal", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (onDeleteLc != null) {
            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("detail_delete_lc_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = null,
                    tint = SanctionCrimson,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Audit Record from Device", color = SanctionCrimson)
            }
        }
    }

    if (showDeleteConfirm && onDeleteLc != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = SlateNavy,
            title = { Text("Delete Local Audit Record?", color = IceWhite) },
            text = {
                Text(
                    "This removes ${lc.lcNumber} and its SHA-256 audit log from your on-device Vault permanently.",
                    color = SoftSilver
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteLc()
                    }
                ) {
                    Text("Delete", color = SanctionCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = SoftSilver)
                }
            }
        )
    }
}

@Composable
private fun AuditSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SlateNavy,
        border = BorderStroke(1.dp, HairlineSlate)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = IceWhite
                )
            }
            content()
        }
    }
}

@Composable
private fun StatusBannerRow(
    text: String,
    color: Color,
    bgColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = IceWhite,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun DiscrepancyItemCard(d: LcDiscrepancy) {
    val color = when (d.severity.uppercase()) {
        "CRITICAL" -> SanctionCrimson
        "HIGH" -> WarningAmber
        else -> SteelCyan
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ElevatedNavy,
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = d.code,
                    style = MonoMetricStyle,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = d.severity.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
            Text(
                text = d.title,
                style = MaterialTheme.typography.titleSmall,
                color = IceWhite
            )
            if (d.detail.isNotBlank()) {
                Text(
                    text = d.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftSilver
                )
            }
            if (d.recommendation.isNotBlank()) {
                Text(
                    text = "Action: ${d.recommendation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SovereignGold
                )
            }
        }
    }
}

@Composable
private fun SwiftFieldRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MutedSlate
        )
        Text(
            text = value,
            style = MonoMetricStyle,
            color = IceWhite
        )
    }
}

private fun shareComplianceReport(
    context: Context,
    lc: VerifiedLcEntity,
    discrepancies: List<LcDiscrepancy>,
    sanctions: List<String>,
    limits: List<String>
) {
    val report = buildString {
        appendLine("MERIDIAN LC SHIELD — TRADE COMPLIANCE AUDIT")
        appendLine("LC Reference: ${lc.lcNumber} (${lc.lcType})")
        appendLine("Status: ${lc.status} | Risk Score: ${lc.riskScore}/100")
        appendLine("Amount: ${lc.currency} ${ComplianceEvaluator.formatUsdCompact(lc.amountUsd)}")
        appendLine("Applicant: ${lc.applicantName} (${lc.applicantCountry})")
        appendLine("Beneficiary: ${lc.beneficiaryName} (${lc.beneficiaryCountry})")
        appendLine("Issuing Bank: ${lc.issuingBank} [${lc.issuingBankBic}]")
        appendLine("Goods (HS ${lc.hsCode}): ${lc.goodsDescription}")
        appendLine("Dual-Use Flagged: ${lc.dualUseFlagged} (${lc.dualUseEccn})")
        appendLine("SHA-256 Document Seal: ${lc.sha256Hash}")
        appendLine()
        appendLine("Summary: ${lc.aiSummary}")
        if (sanctions.isNotEmpty()) {
            appendLine("\nSanctions Matches:")
            sanctions.forEach { appendLine("- $it") }
        }
        if (limits.isNotEmpty()) {
            appendLine("\nExposure Limit Alerts:")
            limits.forEach { appendLine("- $it") }
        }
        if (discrepancies.isNotEmpty()) {
            appendLine("\nUCP 600 / ISBP Discrepancies:")
            discrepancies.forEach { appendLine("- [${it.code}] ${it.title}: ${it.detail}") }
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "LC Compliance Report: ${lc.lcNumber}")
        putExtra(Intent.EXTRA_TEXT, report)
    }
    context.startActivity(Intent.createChooser(intent, "Share LC Compliance Report"))
}
