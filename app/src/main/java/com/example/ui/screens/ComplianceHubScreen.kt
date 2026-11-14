package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ComplianceRuleEntity
import com.example.data.ExposureLimitEntity
import com.example.data.LimitScope
import com.example.data.RiskTier
import com.example.data.RuleCategory
import com.example.data.SanctionEntryEntity
import com.example.data.SanctionProgram
import com.example.data.ScreeningQueryResult
import com.example.ui.components.ExposureLimitGaugeCard
import com.example.ui.theme.DeepNavy
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
import com.example.ui.theme.SteelCyanMuted
import com.example.ui.theme.VerifiedEmerald
import com.example.ui.theme.VerifiedEmeraldMuted
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberMuted
import com.example.viewmodel.ComplianceHubUiState
import com.example.viewmodel.ComplianceTab

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComplianceHubScreen(
    uiState: ComplianceHubUiState,
    onSelectTab: (ComplianceTab) -> Unit,
    onUpdateSearch: (String) -> Unit,
    onSelectFilterChip: (String) -> Unit,
    onQuickScreenQueryChange: (String) -> Unit,
    onRunQuickScreen: () -> Unit,
    onClearQuickScreen: () -> Unit,
    onUpdateLimit: (ExposureLimitEntity, Double, Int, Boolean) -> Unit,
    onAddLimit: (LimitScope, String, String, Double, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingLimit by remember { mutableStateOf<ExposureLimitEntity?>(null) }
    var showAddLimitDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.compliance_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = IceWhite
                )
                Text(
                    text = "Offline reference intelligence: OFAC/EU/UN lists, UCP 600, ISBP 821, Wassenaar dual-use HS schedules, and live institutional caps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSlate
                )
            }
        }

        // Instant Party / Vessel / HS Code Screener
        item {
            InstantScreenerCard(
                query = uiState.quickScreenQuery,
                result = uiState.quickScreenResult,
                onQueryChange = onQuickScreenQueryChange,
                onRunScreen = onRunQuickScreen,
                onClear = onClearQuickScreen
            )
        }

        // Segmented Tabs
        item {
            val selectedIdx = ComplianceTab.entries.indexOf(uiState.activeTab)
            TabRow(
                selectedTabIndex = selectedIdx,
                containerColor = SlateNavy,
                contentColor = SovereignGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIdx]),
                        color = SovereignGold
                    )
                },
                divider = { }
            ) {
                ComplianceTab.entries.forEach { tab ->
                    Tab(
                        selected = tab == uiState.activeTab,
                        onClick = { onSelectTab(tab) },
                        modifier = Modifier.testTag("compliance_tab_${tab.name.lowercase()}"),
                        text = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (tab == uiState.activeTab) SovereignGold else MutedSlate
                            )
                        }
                    )
                }
            }
        }

        // Search & Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onUpdateSearch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("compliance_search_input"),
                    placeholder = {
                        Text(
                            text = when (uiState.activeTab) {
                                ComplianceTab.SANCTIONS -> "Filter sanctioned entities, vessels, or countries..."
                                ComplianceTab.RULES -> "Filter UCP 600 articles, dual-use ECCN, or HS code..."
                                ComplianceTab.LIMITS -> "Filter country, bank BIC, or transaction caps..."
                            },
                            color = MutedSlate
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MutedSlate) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onUpdateSearch("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search", tint = MutedSlate)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SovereignGold,
                        unfocusedBorderColor = HairlineSlate,
                        focusedContainerColor = SlateNavy,
                        unfocusedContainerColor = SlateNavy,
                        focusedTextColor = IceWhite,
                        unfocusedTextColor = IceWhite
                    )
                )

                val chips: List<Pair<String, String>> = when (uiState.activeTab) {
                    ComplianceTab.SANCTIONS -> listOf("ALL" to "All Lists") +
                        SanctionProgram.entries.map { it.name to it.label }
                    ComplianceTab.RULES -> listOf("ALL" to "All Rules") +
                        RuleCategory.entries.map { it.name to it.label }
                    ComplianceTab.LIMITS -> listOf("ALL" to "All Scopes") +
                        LimitScope.entries.map { it.name to it.label }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    chips.forEach { (key, label) ->
                        val selected = uiState.selectedFilterChip == key
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectFilterChip(key) },
                            modifier = Modifier.testTag("compliance_chip_${key.lowercase()}"),
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SovereignGoldMuted,
                                selectedLabelColor = SovereignGold,
                                containerColor = SlateNavy,
                                labelColor = SoftSilver
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = HairlineSlate,
                                selectedBorderColor = SovereignGold.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }

        // Tab Content
        when (uiState.activeTab) {
            ComplianceTab.SANCTIONS -> {
                items(uiState.sanctions, key = { "s_${it.id}" }) { entry ->
                    SanctionEntryCard(entry)
                }
            }

            ComplianceTab.RULES -> {
                items(uiState.rules, key = { "r_${it.id}" }) { rule ->
                    ComplianceRuleCard(rule)
                }
            }

            ComplianceTab.LIMITS -> {
                item {
                    OutlinedButton(
                        onClick = { showAddLimitDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("compliance_add_limit_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SovereignGold.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = SovereignGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Country or Bank Exposure Cap", color = SovereignGold)
                    }
                }

                items(uiState.limitTelemetries, key = { "lim_${it.limit.id}" }) { telemetry ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ExposureLimitGaugeCard(
                            telemetry = telemetry,
                            onClick = { editingLimit = telemetry.limit }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { editingLimit = telemetry.limit },
                                modifier = Modifier.testTag("edit_limit_btn_${telemetry.limit.id}")
                            ) {
                                Icon(
                                    Icons.Filled.Tune,
                                    contentDescription = null,
                                    tint = SteelCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Adjust Cap & Threshold", color = SteelCyan)
                            }
                        }
                    }
                }
            }
        }
    }

    editingLimit?.let { target ->
        EditExposureLimitDialog(
            limit = target,
            onDismiss = { editingLimit = null },
            onSave = { newCap, newThreshold, active ->
                onUpdateLimit(target, newCap, newThreshold, active)
                editingLimit = null
            }
        )
    }

    if (showAddLimitDialog) {
        AddExposureLimitDialog(
            onDismiss = { showAddLimitDialog = false },
            onAdd = { scope, name, id, cap, threshold ->
                onAddLimit(scope, name, id, cap, threshold)
                showAddLimitDialog = false
            }
        )
    }
}

@Composable
private fun InstantScreenerCard(
    query: String,
    result: ScreeningQueryResult?,
    onQueryChange: (String) -> Unit,
    onRunScreen: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DeepNavy,
        border = BorderStroke(1.dp, SteelCyan.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = SteelCyan, modifier = Modifier.size(18.dp))
                Text(
                    text = "Instant Party, Vessel & HS Code Screener",
                    style = MaterialTheme.typography.titleSmall,
                    color = IceWhite
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_screener_input"),
                    placeholder = { Text("e.g., 8542, Severny, CNC, TR", color = MutedSlate) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SteelCyan,
                        unfocusedBorderColor = HairlineSlate,
                        focusedContainerColor = SlateNavy,
                        unfocusedContainerColor = SlateNavy,
                        focusedTextColor = IceWhite,
                        unfocusedTextColor = IceWhite
                    )
                )
                Button(
                    onClick = onRunScreen,
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("quick_screener_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SteelCyan,
                        contentColor = MidnightNavy
                    )
                ) {
                    Text("Screen", fontWeight = FontWeight.Bold)
                }
            }

            if (result != null) {
                val hasHit = result.hasAnyHit
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = if (hasHit) SanctionCrimsonMuted else VerifiedEmeraldMuted,
                    border = BorderStroke(
                        1.dp,
                        if (hasHit) SanctionCrimson.copy(alpha = 0.5f) else VerifiedEmerald.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (hasHit) Icons.Filled.Gavel else Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = if (hasHit) SanctionCrimson else VerifiedEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (hasHit) {
                                        "${result.matchedSanctions.size} Sanction · ${result.matchedRules.size} Rule Hits"
                                    } else {
                                        "No Sanction or Dual-Use Matches"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = IceWhite
                                )
                            }
                            TextButton(onClick = onClear) {
                                Text("Clear", color = SoftSilver)
                            }
                        }
                        result.matchedSanctions.forEach { s ->
                            Text(
                                text = "• [${s.program}] ${s.entityName} (${s.countryIso}) — ${s.riskTier}",
                                style = MaterialTheme.typography.bodySmall,
                                color = IceWhite
                            )
                        }
                        result.matchedRules.forEach { r ->
                            Text(
                                text = "• [${r.ruleCode}] ${r.title}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SovereignGold
                            )
                        }
                        result.matchedLimits.forEach { l ->
                            Text(
                                text = "• Limit Active: ${l.targetName} (${l.scope})",
                                style = MaterialTheme.typography.bodySmall,
                                color = SteelCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SanctionEntryCard(entry: SanctionEntryEntity) {
    val tierColor = when (entry.riskTier) {
        RiskTier.CRITICAL.name -> SanctionCrimson
        RiskTier.HIGH.name -> WarningAmber
        else -> SteelCyan
    }
    val tierBg = when (entry.riskTier) {
        RiskTier.CRITICAL.name -> SanctionCrimsonMuted
        RiskTier.HIGH.name -> WarningAmberMuted
        else -> SteelCyanMuted
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                    text = "${entry.program.replace('_', ' ')} · ${entry.countryIso}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SteelCyan
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tierBg
                ) {
                    Text(
                        text = entry.riskTier,
                        style = MaterialTheme.typography.labelSmall,
                        color = tierColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = entry.entityName,
                style = MaterialTheme.typography.titleSmall,
                color = IceWhite
            )
            Text(
                text = entry.notes,
                style = MaterialTheme.typography.bodySmall,
                color = SoftSilver
            )
            Text(
                text = "${entry.entityType} · ${entry.countryName} · Updated ${entry.updatedDate}",
                style = MaterialTheme.typography.labelSmall,
                color = MutedSlate
            )
        }
    }
}

@Composable
private fun ComplianceRuleCard(rule: ComplianceRuleEntity) {
    val isDualUse = rule.category == RuleCategory.DUAL_USE.name
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SlateNavy,
        border = BorderStroke(1.dp, if (isDualUse) SovereignGold.copy(alpha = 0.35f) else HairlineSlate)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Policy,
                        contentDescription = null,
                        tint = if (isDualUse) SovereignGold else SteelCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = rule.ruleCode,
                        style = MonoMetricStyle,
                        color = if (isDualUse) SovereignGold else SteelCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = rule.effectiveDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate
                )
            }
            Text(
                text = rule.title,
                style = MaterialTheme.typography.titleSmall,
                color = IceWhite
            )
            Text(
                text = rule.summary,
                style = MaterialTheme.typography.bodySmall,
                color = SoftSilver
            )
            if (rule.hsPrefixes.isNotBlank()) {
                Text(
                    text = "Controlled HS Prefixes: ${rule.hsPrefixes}",
                    style = MonoMetricStyle,
                    color = WarningAmber
                )
            }
        }
    }
}

@Composable
private fun EditExposureLimitDialog(
    limit: ExposureLimitEntity,
    onDismiss: () -> Unit,
    onSave: (Double, Int, Boolean) -> Unit
) {
    var capText by remember { mutableStateOf((limit.maxCapUsd / 1_000_000.0).toString()) }
    var alertText by remember { mutableStateOf(limit.alertThresholdPercent.toString()) }
    var isActive by remember { mutableStateOf(limit.isActive) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SlateNavy,
        title = {
            Text(
                text = "Adjust Cap: ${limit.targetName}",
                style = MaterialTheme.typography.titleMedium,
                color = IceWhite
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = capText,
                    onValueChange = { capText = it; errorMsg = null },
                    label = { Text("Max Exposure Cap (USD Millions)", color = SoftSilver) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SovereignGold,
                        unfocusedBorderColor = HairlineSlate,
                        focusedTextColor = IceWhite,
                        unfocusedTextColor = IceWhite
                    )
                )
                OutlinedTextField(
                    value = alertText,
                    onValueChange = { alertText = it; errorMsg = null },
                    label = { Text("Alert Threshold (%)", color = SoftSilver) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SovereignGold,
                        unfocusedBorderColor = HairlineSlate,
                        focusedTextColor = IceWhite,
                        unfocusedTextColor = IceWhite
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Limit Enforcement Active", color = IceWhite, style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = SovereignGold)
                    )
                }
                if (errorMsg != null) {
                    Text(errorMsg.orEmpty(), color = SanctionCrimson, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val millions = capText.toDoubleOrNull()
                    val threshold = alertText.toIntOrNull()
                    if (millions == null || millions <= 0.0) {
                        errorMsg = "Enter a valid positive cap in USD millions."
                    } else if (threshold == null || threshold !in 10..99) {
                        errorMsg = "Alert threshold must be between 10% and 99%."
                    } else {
                        onSave(millions * 1_000_000.0, threshold, isActive)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SovereignGold,
                    contentColor = MidnightNavy
                )
            ) {
                Text("Save Cap", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SoftSilver)
            }
        }
    )
}

@Composable
private fun AddExposureLimitDialog(
    onDismiss: () -> Unit,
    onAdd: (LimitScope, String, String, Double, Int) -> Unit
) {
    var selectedScope by remember { mutableStateOf(LimitScope.COUNTRY) }
    var targetName by remember { mutableStateOf("") }
    var targetCode by remember { mutableStateOf("") }
    var capMillions by remember { mutableStateOf("5.0") }
    var alertPct by remember { mutableStateOf("75") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SlateNavy,
        title = { Text("Add Exposure Limit", color = IceWhite) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(LimitScope.COUNTRY, LimitScope.ISSUING_BANK).forEach { scope ->
                        FilterChip(
                            selected = selectedScope == scope,
                            onClick = { selectedScope = scope },
                            label = { Text(scope.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SovereignGoldMuted,
                                selectedLabelColor = SovereignGold
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = targetName,
                    onValueChange = { targetName = it; errorMsg = null },
                    label = {
                        Text(
                            if (selectedScope == LimitScope.COUNTRY) "Country Name (e.g. UAE)" else "Bank Name",
                            color = SoftSilver
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SovereignGold,
                        unfocusedBorderColor = HairlineSlate,
                        focusedTextColor = IceWhite,
                        unfocusedTextColor = IceWhite
                    )
                )
                OutlinedTextField(
                    value = targetCode,
                    onValueChange = { targetCode = it; errorMsg = null },
                    label = {
                        Text(
                            if (selectedScope == LimitScope.COUNTRY) "ISO Code (e.g. AE)" else "BIC Prefix (e.g. EBILAEAD)",
                            color = SoftSilver
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SovereignGold,
                        unfocusedBorderColor = HairlineSlate,
                        focusedTextColor = IceWhite,
                        unfocusedTextColor = IceWhite
                    )
                )
                OutlinedTextField(
                    value = capMillions,
                    onValueChange = { capMillions = it; errorMsg = null },
                    label = { Text("Max Cap (USD Millions)", color = SoftSilver) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SovereignGold,
                        unfocusedBorderColor = HairlineSlate,
                        focusedTextColor = IceWhite,
                        unfocusedTextColor = IceWhite
                    )
                )
                OutlinedTextField(
                    value = alertPct,
                    onValueChange = { alertPct = it; errorMsg = null },
                    label = { Text("Alert Threshold (%)", color = SoftSilver) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SovereignGold,
                        unfocusedBorderColor = HairlineSlate,
                        focusedTextColor = IceWhite,
                        unfocusedTextColor = IceWhite
                    )
                )
                if (errorMsg != null) {
                    Text(errorMsg.orEmpty(), color = SanctionCrimson, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capMillions.toDoubleOrNull()
                    val pct = alertPct.toIntOrNull()
                    if (targetName.isBlank() || targetCode.isBlank()) {
                        errorMsg = "Enter both target name and ISO/BIC code."
                    } else if (cap == null || cap <= 0.0) {
                        errorMsg = "Enter a valid positive cap."
                    } else if (pct == null || pct !in 10..99) {
                        errorMsg = "Alert threshold must be 10–99%."
                    } else {
                        onAdd(selectedScope, targetName, targetCode, cap * 1_000_000.0, pct)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SovereignGold,
                    contentColor = MidnightNavy
                )
            ) {
                Text("Add Cap", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SoftSilver)
            }
        }
    )
}
