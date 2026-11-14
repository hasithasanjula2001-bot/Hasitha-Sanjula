package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ComplianceEvaluator
import com.example.data.ComplianceRuleEntity
import com.example.data.ExposureLimitEntity
import com.example.data.LimitScope
import com.example.data.SanctionEntryEntity
import com.example.data.ScreeningQueryResult
import com.example.data.TradeComplianceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ComplianceTab(val label: String) {
    SANCTIONS("Sanctions Lists"),
    RULES("Rules & Dual-Use"),
    LIMITS("Exposure Limits")
}

data class ComplianceHubUiState(
    val activeTab: ComplianceTab = ComplianceTab.SANCTIONS,
    val searchQuery: String = "",
    val selectedFilterChip: String = "ALL",
    val sanctions: List<SanctionEntryEntity> = emptyList(),
    val rules: List<ComplianceRuleEntity> = emptyList(),
    val limitTelemetries: List<LimitTelemetry> = emptyList(),
    val quickScreenQuery: String = "",
    val quickScreenResult: ScreeningQueryResult? = null
)

class ComplianceViewModel(
    private val repository: TradeComplianceRepository
) : ViewModel() {

    private val _activeTab = MutableStateFlow(ComplianceTab.SANCTIONS)
    private val _searchQuery = MutableStateFlow("")
    private val _filterChip = MutableStateFlow("ALL")
    private val _quickQuery = MutableStateFlow("")
    private val _quickResult = MutableStateFlow<ScreeningQueryResult?>(null)

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    private data class FilterParams(
        val tab: ComplianceTab,
        val search: String,
        val chip: String,
        val quickQuery: String,
        val quickResult: ScreeningQueryResult?
    )

    private val filterFlow = combine(
        _activeTab,
        _searchQuery,
        _filterChip,
        _quickQuery,
        _quickResult
    ) { tab, search, chip, qq, qr ->
        FilterParams(tab, search, chip, qq, qr)
    }

    val uiState: StateFlow<ComplianceHubUiState> = combine(
        filterFlow,
        repository.sanctions,
        repository.rules,
        repository.exposureLimits,
        repository.verifiedLcs
    ) { params, sanctions, rules, limits, lcs ->
        val q = params.search.trim().lowercase()

        val filteredSanctions = sanctions.filter { s ->
            val chipMatch = params.chip == "ALL" || s.program == params.chip
            val textMatch = q.isEmpty() ||
                s.entityName.lowercase().contains(q) ||
                s.countryName.lowercase().contains(q) ||
                s.keywords.lowercase().contains(q)
            chipMatch && textMatch
        }

        val filteredRules = rules.filter { r ->
            val chipMatch = params.chip == "ALL" || r.category == params.chip
            val textMatch = q.isEmpty() ||
                r.ruleCode.lowercase().contains(q) ||
                r.title.lowercase().contains(q) ||
                r.summary.lowercase().contains(q) ||
                r.hsPrefixes.lowercase().contains(q)
            chipMatch && textMatch
        }

        val limitTelemetries = limits.filter { l ->
            val chipMatch = params.chip == "ALL" || l.scope == params.chip
            val textMatch = q.isEmpty() ||
                l.targetName.lowercase().contains(q) ||
                l.targetIdentifier.lowercase().contains(q)
            chipMatch && textMatch
        }.map { limit ->
            val committed = ComplianceEvaluator.computeCommittedExposureUsd(limit, lcs)
            val ratio = if (limit.maxCapUsd > 0) (committed / limit.maxCapUsd).toFloat() else 0f
            LimitTelemetry(
                limit = limit,
                committedUsd = committed,
                utilizationRatio = ratio.coerceIn(0f, 1.25f),
                isAlertTriggered = ratio * 100f >= limit.alertThresholdPercent,
                isBreached = committed > limit.maxCapUsd
            )
        }

        ComplianceHubUiState(
            activeTab = params.tab,
            searchQuery = params.search,
            selectedFilterChip = params.chip,
            sanctions = filteredSanctions,
            rules = filteredRules,
            limitTelemetries = limitTelemetries,
            quickScreenQuery = params.quickQuery,
            quickScreenResult = params.quickResult
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ComplianceHubUiState()
    )

    fun selectTab(tab: ComplianceTab) {
        _activeTab.value = tab
        _filterChip.value = "ALL"
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectFilterChip(chip: String) {
        _filterChip.value = chip
    }

    fun updateQuickScreenQuery(query: String) {
        _quickQuery.value = query
        if (query.isBlank()) {
            _quickResult.value = null
        }
    }

    fun executeQuickScreen() {
        val query = _quickQuery.value.trim()
        if (query.isEmpty()) return
        viewModelScope.launch {
            _quickResult.value = repository.runQuickScreen(query)
        }
    }

    fun clearQuickScreen() {
        _quickQuery.value = ""
        _quickResult.value = null
    }

    fun saveUpdatedLimit(
        limit: ExposureLimitEntity,
        newMaxCapUsd: Double,
        newAlertThresholdPercent: Int,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            repository.updateExposureLimit(
                limit.copy(
                    maxCapUsd = newMaxCapUsd,
                    alertThresholdPercent = newAlertThresholdPercent.coerceIn(10, 99),
                    isActive = isActive
                )
            )
        }
    }

    fun addCustomLimit(
        scope: LimitScope,
        targetName: String,
        targetIdentifier: String,
        maxCapUsd: Double,
        alertThresholdPercent: Int
    ) {
        viewModelScope.launch {
            repository.addExposureLimit(
                ExposureLimitEntity(
                    scope = scope.name,
                    targetName = targetName.trim(),
                    targetIdentifier = targetIdentifier.trim().uppercase(),
                    maxCapUsd = maxCapUsd,
                    baselineCommittedUsd = 0.0,
                    alertThresholdPercent = alertThresholdPercent.coerceIn(10, 99),
                    isActive = true
                )
            )
        }
    }
}
