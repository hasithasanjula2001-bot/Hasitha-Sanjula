package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ComplianceEvaluator
import com.example.ui.components.ExposureLimitGaugeCard
import com.example.ui.components.LcAuditSummaryCard
import com.example.ui.components.OfflinePrivacyPill
import com.example.ui.components.RiskScoreRing
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.HairlineSlate
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MonoHeroFigureStyle
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SanctionCrimson
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.SoftSilver
import com.example.ui.theme.SovereignGold
import com.example.ui.theme.SteelCyan
import com.example.ui.theme.VerifiedEmerald
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.DashboardUiState

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onVerifyNewLc: () -> Unit,
    onOpenComplianceHub: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenLcDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ExecutiveHeroCard(
                uiState = uiState,
                onVerifyNewLc = onVerifyNewLc
            )
        }

        item {
            KpiTelemetryGrid(
                uiState = uiState,
                onOpenComplianceHub = onOpenComplianceHub,
                onOpenVault = onOpenVault
            )
        }

        item {
            SectionHeader(
                title = "Exposure Limits & Utilization",
                actionLabel = "Manage Limits",
                onAction = onOpenComplianceHub,
                actionTestTag = "dashboard_manage_limits_button"
            )
        }

        items(
            items = uiState.limitTelemetries.take(3),
            key = { "limit_${it.limit.id}" }
        ) { telemetry ->
            ExposureLimitGaugeCard(
                telemetry = telemetry,
                onClick = onOpenComplianceHub
            )
        }

        item {
            SectionHeader(
                title = "Recent LC Verifications",
                actionLabel = "View Vault (${uiState.totalLcCount})",
                onAction = onOpenVault,
                actionTestTag = "dashboard_view_vault_button"
            )
        }

        if (uiState.recentLcs.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SlateNavy,
                    border = BorderStroke(1.dp, HairlineSlate)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = SovereignGold,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No verified Letters of Credit yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = IceWhite
                        )
                        Text(
                            text = "Scan or upload an MT700, commercial invoice, or bill of lading to generate a SHA-256 compliance audit.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSlate
                        )
                    }
                }
            }
        } else {
            items(
                items = uiState.recentLcs,
                key = { "lc_${it.id}" }
            ) { lc ->
                LcAuditSummaryCard(
                    lc = lc,
                    onClick = { onOpenLcDetail(lc.id) }
                )
            }
        }
    }
}

@Composable
private fun ExecutiveHeroCard(
    uiState: DashboardUiState,
    onVerifyNewLc: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = DeepNavy,
        border = BorderStroke(1.dp, SovereignGold.copy(alpha = 0.28f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_vault),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.26f,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MidnightNavy.copy(alpha = 0.45f),
                                DeepNavy.copy(alpha = 0.94f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OfflinePrivacyPill()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = SteelCyan
                        )
                        Text(
                            text = ComplianceEvaluator.formatUsdCompact(uiState.totalVerifiedVolumeUsd),
                            style = MaterialTheme.typography.displaySmall,
                            color = IceWhite
                        )
                        Text(
                            text = "Audited Trade Volume · ${uiState.clearedCount} Cleared · ${uiState.blockedCount} Blocked",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftSilver
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    RiskScoreRing(
                        score = uiState.averageRiskScore,
                        label = "AVG RISK",
                        size = 92.dp
                    )
                }

                Button(
                    onClick = onVerifyNewLc,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("dashboard_verify_new_lc_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SovereignGold,
                        contentColor = MidnightNavy
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.DocumentScanner,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.dashboard_cta_verify),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiTelemetryGrid(
    uiState: DashboardUiState,
    onOpenComplianceHub: () -> Unit,
    onOpenVault: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiMetricTile(
                title = "Sanction & Dual-Use",
                value = "${uiState.blockedCount + uiState.dualUseFlaggedCount}",
                subtitle = "${uiState.blockedCount} blocked · ${uiState.dualUseFlaggedCount} dual-use",
                accentColor = if (uiState.blockedCount > 0) SanctionCrimson else WarningAmber,
                icon = Icons.Filled.Gavel,
                onClick = onOpenVault,
                modifier = Modifier.weight(1f),
                testTag = "kpi_sanctions_tile"
            )
            KpiMetricTile(
                title = "UCP Discrepancies",
                value = "${uiState.ucpDiscrepancyRatePercent}%",
                subtitle = "${uiState.reviewRequiredCount} pending review",
                accentColor = if (uiState.ucpDiscrepancyRatePercent > 40) WarningAmber else SteelCyan,
                icon = Icons.Filled.Policy,
                onClick = onOpenVault,
                modifier = Modifier.weight(1f),
                testTag = "kpi_discrepancies_tile"
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiMetricTile(
                title = "Active Sanctions & Rules",
                value = "${uiState.activeSanctionsCount + uiState.activeRulesCount}",
                subtitle = "OFAC · EU · UN · UCP 600",
                accentColor = SteelCyan,
                icon = Icons.Filled.Security,
                onClick = onOpenComplianceHub,
                modifier = Modifier.weight(1f),
                testTag = "kpi_rules_tile"
            )
            KpiMetricTile(
                title = "Clearance Ratio",
                value = if (uiState.totalLcCount == 0) "0%" else "${(uiState.clearedCount * 100) / uiState.totalLcCount}%",
                subtitle = "${uiState.clearedCount} of ${uiState.totalLcCount} verified LCs",
                accentColor = VerifiedEmerald,
                icon = Icons.Filled.TrendingUp,
                onClick = onOpenVault,
                modifier = Modifier.weight(1f),
                testTag = "kpi_clearance_tile"
            )
        }
    }
}

@Composable
private fun KpiMetricTile(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = SlateNavy,
        border = BorderStroke(1.dp, HairlineSlate)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedSlate
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                style = MonoHeroFigureStyle,
                color = IceWhite
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SoftSilver
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    actionTestTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = IceWhite
        )
        TextButton(
            onClick = onAction,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag(actionTestTag)
        ) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = SovereignGold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = SovereignGold,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
