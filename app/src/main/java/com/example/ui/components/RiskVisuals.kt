package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.ComplianceEvaluator
import com.example.data.LcStatus
import com.example.data.VerifiedLcEntity
import com.example.ui.theme.ElevatedNavy
import com.example.ui.theme.HairlineSlate
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MonoHeroFigureStyle
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
import com.example.viewmodel.LimitTelemetry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun riskColorForScore(score: Int): Color = when {
    score >= 75 -> SanctionCrimson
    score >= 30 -> WarningAmber
    else -> VerifiedEmerald
}

fun statusColors(statusName: String): Pair<Color, Color> = when (statusName) {
    LcStatus.BLOCKED.name -> SanctionCrimson to SanctionCrimsonMuted
    LcStatus.REVIEW_REQUIRED.name -> WarningAmber to WarningAmberMuted
    else -> VerifiedEmerald to VerifiedEmeraldMuted
}

@Composable
fun RiskScoreRing(
    score: Int,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    val clamped = score.coerceIn(0, 100)
    val animatedSweep by animateFloatAsState(
        targetValue = (clamped / 100f) * 260f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "risk_ring_sweep"
    )
    val accentColor = riskColorForScore(clamped)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = this.size.minDimension * 0.11f
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = HairlineSlate,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            drawArc(
                color = accentColor,
                startAngle = 140f,
                sweepAngle = animatedSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$clamped",
                style = MonoHeroFigureStyle,
                color = IceWhite
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = accentColor
            )
        }
    }
}

@Composable
fun ComplianceStatusBadge(
    statusName: String,
    modifier: Modifier = Modifier
) {
    val (fg, bg) = statusColors(statusName)
    val (icon, text) = when (statusName) {
        LcStatus.BLOCKED.name -> Icons.Filled.Block to "BLOCKED"
        LcStatus.REVIEW_REQUIRED.name -> Icons.Filled.Warning to "REVIEW"
        else -> Icons.Filled.CheckCircle to "CLEARED"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = fg,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OfflinePrivacyPill(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(99.dp),
        color = ElevatedNavy.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, SovereignGold.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = SovereignGold,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "ON-DEVICE VAULT · ZERO CLOUD SYNC",
                style = MaterialTheme.typography.labelSmall,
                color = SovereignGold
            )
        }
    }
}

@Composable
fun TamperSealChip(
    sha256Hash: String,
    modifier: Modifier = Modifier
) {
    val shortHash = if (sha256Hash.length > 16) {
        "${sha256Hash.take(8)}…${sha256Hash.takeLast(8)}"
    } else {
        sha256Hash
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = ElevatedNavy,
        border = BorderStroke(1.dp, SteelCyan.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.VerifiedUser,
                contentDescription = "SHA-256 document seal",
                tint = SteelCyan,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "SHA-256 $shortHash",
                style = MaterialTheme.typography.labelSmall,
                color = SoftSilver
            )
        }
    }
}

@Composable
fun ExposureLimitGaugeCard(
    telemetry: LimitTelemetry,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val barColor = when {
        telemetry.isBreached -> SanctionCrimson
        telemetry.isAlertTriggered -> WarningAmber
        else -> VerifiedEmerald
    }
    val animatedRatio by animateFloatAsState(
        targetValue = telemetry.utilizationRatio.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "limit_bar_ratio"
    )
    val pctText = "${(telemetry.utilizationRatio * 100f).toInt()}%"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        color = SlateNavy,
        border = BorderStroke(1.dp, if (telemetry.isBreached) SanctionCrimson.copy(alpha = 0.55f) else HairlineSlate)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = telemetry.limit.targetName,
                        style = MaterialTheme.typography.titleSmall,
                        color = IceWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${telemetry.limit.scope.replace('_', ' ')} · Alert @ ${telemetry.limit.alertThresholdPercent}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = pctText,
                    style = MonoMetricStyle,
                    color = barColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            ) {
                val trackHeight = size.height
                drawRoundRect(
                    color = ElevatedNavy,
                    topLeft = Offset.Zero,
                    size = Size(size.width, trackHeight),
                    cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
                )
                val fillWidth = (size.width * animatedRatio).coerceAtLeast(if (animatedRatio > 0f) trackHeight else 0f)
                if (fillWidth > 0f) {
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset.Zero,
                        size = Size(fillWidth, trackHeight),
                        cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
                    )
                }
                val thresholdX = size.width * (telemetry.limit.alertThresholdPercent / 100f)
                drawLine(
                    color = SovereignGold,
                    start = Offset(thresholdX, 0f),
                    end = Offset(thresholdX, trackHeight),
                    strokeWidth = 3f
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Used: ${ComplianceEvaluator.formatUsdCompact(telemetry.committedUsd)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftSilver
                )
                Text(
                    text = "Cap: ${ComplianceEvaluator.formatUsdCompact(telemetry.limit.maxCapUsd)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate
                )
            }
        }
    }
}

@Composable
fun LcAuditSummaryCard(
    lc: VerifiedLcEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("lc_card_${lc.lcNumber}"),
        shape = RoundedCornerShape(16.dp),
        color = SlateNavy,
        border = BorderStroke(1.dp, HairlineSlate)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = lc.lcNumber,
                            style = MonoMetricStyle,
                            color = SovereignGold,
                            fontWeight = FontWeight.Bold
                        )
                        if (lc.dualUseFlagged) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SovereignGoldMuted
                            ) {
                                Text(
                                    text = "DUAL-USE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SovereignGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${lc.applicantName} → ${lc.beneficiaryName}",
                        style = MaterialTheme.typography.titleSmall,
                        color = IceWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                ComplianceStatusBadge(statusName = lc.status)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${lc.currency} ${ComplianceEvaluator.formatUsdCompact(lc.amountUsd)} · HS ${lc.hsCode}",
                    style = MonoMetricStyle,
                    color = SoftSilver
                )
                Text(
                    text = "Risk ${lc.riskScore}/100",
                    style = MaterialTheme.typography.labelSmall,
                    color = riskColorForScore(lc.riskScore),
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TamperSealChip(sha256Hash = lc.sha256Hash)
                Text(
                    text = formatTimestamp(lc.verifiedAtMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate
                )
            }
        }
    }
}

fun formatTimestamp(millis: Long): String {
    val fmt = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
    return fmt.format(Date(millis))
}
