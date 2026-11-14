package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ComplianceEvaluator
import com.example.data.ComplianceJsonCodec
import com.example.data.ComplianceRuleEntity
import com.example.data.ExposureLimitEntity
import com.example.data.LcStatus
import com.example.data.SanctionEntryEntity
import com.example.data.TradeComplianceRepository
import com.example.data.VerifiedLcEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LimitTelemetry(
    val limit: ExposureLimitEntity,
    val committedUsd: Double,
    val utilizationRatio: Float,
    val isAlertTriggered: Boolean,
    val isBreached: Boolean
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalVerifiedVolumeUsd: Double = 0.0,
    val totalLcCount: Int = 0,
    val clearedCount: Int = 0,
    val reviewRequiredCount: Int = 0,
    val blockedCount: Int = 0,
    val dualUseFlaggedCount: Int = 0,
    val averageRiskScore: Int = 0,
    val ucpDiscrepancyRatePercent: Int = 0,
    val activeSanctionsCount: Int = 0,
    val activeRulesCount: Int = 0,
    val limitTelemetries: List<LimitTelemetry> = emptyList(),
    val recentLcs: List<VerifiedLcEntity> = emptyList()
)

class DashboardViewModel(
    private val repository: TradeComplianceRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.verifiedLcs,
        repository.exposureLimits,
        repository.sanctions,
        repository.rules
    ) { lcs, limits, sanctions, rules ->
        val totalVol = lcs.sumOf { it.amountUsd }
        val cleared = lcs.count { it.status == LcStatus.CLEARED.name }
        val review = lcs.count { it.status == LcStatus.REVIEW_REQUIRED.name }
        val blocked = lcs.count { it.status == LcStatus.BLOCKED.name }
        val dualUse = lcs.count { it.dualUseFlagged }
        val avgRisk = if (lcs.isEmpty()) 0 else lcs.map { it.riskScore }.average().toInt()
        val withDiscrepancies = lcs.count {
            ComplianceJsonCodec.decodeDiscrepancies(it.discrepanciesJson).isNotEmpty()
        }
        val discrepancyRate = if (lcs.isEmpty()) 0 else (withDiscrepancies * 100) / lcs.size

        val telemetries = limits.filter { it.isActive }.map { limit ->
            val committed = ComplianceEvaluator.computeCommittedExposureUsd(limit, lcs)
            val ratio = if (limit.maxCapUsd > 0) (committed / limit.maxCapUsd).toFloat() else 0f
            val pct = ratio * 100f
            LimitTelemetry(
                limit = limit,
                committedUsd = committed,
                utilizationRatio = ratio.coerceIn(0f, 1.25f),
                isAlertTriggered = pct >= limit.alertThresholdPercent,
                isBreached = committed > limit.maxCapUsd
            )
        }.sortedByDescending { it.utilizationRatio }

        DashboardUiState(
            isLoading = false,
            totalVerifiedVolumeUsd = totalVol,
            totalLcCount = lcs.size,
            clearedCount = cleared,
            reviewRequiredCount = review,
            blockedCount = blocked,
            dualUseFlaggedCount = dualUse,
            averageRiskScore = avgRisk,
            ucpDiscrepancyRatePercent = discrepancyRate,
            activeSanctionsCount = sanctions.size,
            activeRulesCount = rules.size,
            limitTelemetries = telemetries,
            recentLcs = lcs.take(5)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}

class MeridianViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    private val repository by lazy {
        TradeComplianceRepository(
            context = application.applicationContext,
            database = AppDatabase.getInstance(application)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(repository) as T
            modelClass.isAssignableFrom(VerificationViewModel::class.java) ->
                VerificationViewModel(repository) as T
            modelClass.isAssignableFrom(ComplianceViewModel::class.java) ->
                ComplianceViewModel(repository) as T
            modelClass.isAssignableFrom(VaultViewModel::class.java) ->
                VaultViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
