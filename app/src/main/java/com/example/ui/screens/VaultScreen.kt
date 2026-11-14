package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.LcStatus
import com.example.data.VerifiedLcEntity
import com.example.ui.components.LcAuditSummaryCard
import com.example.ui.components.OfflinePrivacyPill
import com.example.ui.theme.HairlineSlate
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.SoftSilver
import com.example.ui.theme.SovereignGold
import com.example.ui.theme.SovereignGoldMuted
import com.example.viewmodel.IntegrityCheckState
import com.example.viewmodel.VaultUiState

@Composable
fun VaultScreen(
    uiState: VaultUiState,
    integrityState: IntegrityCheckState,
    onUpdateSearch: (String) -> Unit,
    onSelectStatusFilter: (String) -> Unit,
    onSelectLc: (Long) -> Unit,
    onOpenLcDetail: (Long) -> Unit,
    onVerifyHash: (VerifiedLcEntity) -> Unit,
    onDeleteLc: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
    ) {
        val isTwoPane = maxWidth >= 720.dp
        if (isTwoPane) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VaultListPane(
                    uiState = uiState,
                    onUpdateSearch = onUpdateSearch,
                    onSelectStatusFilter = onSelectStatusFilter,
                    onItemClick = { lc -> onSelectLc(lc.id) },
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                )
                val activeLc = uiState.items.firstOrNull { it.id == uiState.selectedLcId }
                    ?: uiState.items.firstOrNull()
                Box(
                    modifier = Modifier
                        .weight(0.55f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (activeLc != null) {
                        LcAuditReportBody(
                            lc = activeLc,
                            integrityState = integrityState,
                            onVerifyHash = { onVerifyHash(activeLc) },
                            onDeleteLc = { onDeleteLc(activeLc.id) }
                        )
                    } else {
                        VaultEmptyCard()
                    }
                }
            }
        } else {
            VaultListPane(
                uiState = uiState,
                onUpdateSearch = onUpdateSearch,
                onSelectStatusFilter = onSelectStatusFilter,
                onItemClick = { lc -> onOpenLcDetail(lc.id) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VaultListPane(
    uiState: VaultUiState,
    onUpdateSearch: (String) -> Unit,
    onSelectStatusFilter: (String) -> Unit,
    onItemClick: (VerifiedLcEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OfflinePrivacyPill()
                Text(
                    text = stringResource(R.string.vault_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = IceWhite
                )
            }
        }

        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onUpdateSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_search_input"),
                placeholder = { Text("Search LC number, bank, party, or HS code...", color = MutedSlate) },
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
        }

        item {
            val filters = listOf(
                "ALL" to "All (${uiState.items.size})",
                LcStatus.CLEARED.name to "Cleared",
                LcStatus.REVIEW_REQUIRED.name to "Review",
                LcStatus.BLOCKED.name to "Blocked"
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filters.forEach { (key, label) ->
                    val selected = uiState.selectedStatusFilter == key
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectStatusFilter(key) },
                        modifier = Modifier.testTag("vault_filter_${key.lowercase()}"),
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

        if (uiState.items.isEmpty()) {
            item { VaultEmptyCard() }
        } else {
            items(uiState.items, key = { "vault_${it.id}" }) { lc ->
                LcAuditSummaryCard(
                    lc = lc,
                    onClick = { onItemClick(lc) }
                )
            }
        }
    }
}

@Composable
private fun VaultEmptyCard() {
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
                imageVector = Icons.Filled.FolderOff,
                contentDescription = null,
                tint = MutedSlate,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "No matching LC audits in local vault",
                style = MaterialTheme.typography.titleSmall,
                color = IceWhite
            )
            Text(
                text = "Adjust your search/filter or verify a new Letter of Credit.",
                style = MaterialTheme.typography.bodySmall,
                color = MutedSlate
            )
        }
    }
}

@Composable
fun LcDetailScreen(
    lc: VerifiedLcEntity?,
    integrityState: IntegrityCheckState,
    onBack: () -> Unit,
    onVerifyHash: (VerifiedLcEntity) -> Unit,
    onDeleteLc: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = IceWhite
                )
            }
            Text(
                text = lc?.lcNumber ?: "LC Audit Record",
                style = MaterialTheme.typography.titleLarge,
                color = IceWhite
            )
        }

        if (lc == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Audit record not found.", color = MutedSlate)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                LcAuditReportBody(
                    lc = lc,
                    integrityState = integrityState,
                    onVerifyHash = { onVerifyHash(lc) },
                    onDeleteLc = {
                        onDeleteLc(lc.id)
                        onBack()
                    }
                )
            }
        }
    }
}
