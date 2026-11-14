package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TradeComplianceRepository
import com.example.data.VerifiedLcEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VaultUiState(
    val searchQuery: String = "",
    val selectedStatusFilter: String = "ALL",
    val items: List<VerifiedLcEntity> = emptyList(),
    val selectedLcId: Long? = null
)

sealed interface IntegrityCheckState {
    data object Idle : IntegrityCheckState
    data object VerifiedMatch : IntegrityCheckState
    data object TamperMismatch : IntegrityCheckState
    data class Unavailable(val reason: String) : IntegrityCheckState
}

class VaultViewModel(
    private val repository: TradeComplianceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow("ALL")
    private val _selectedLcId = MutableStateFlow<Long?>(null)

    private val _integrityState = MutableStateFlow<IntegrityCheckState>(IntegrityCheckState.Idle)
    val integrityState: StateFlow<IntegrityCheckState> = _integrityState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    val uiState: StateFlow<VaultUiState> = combine(
        _searchQuery,
        _statusFilter,
        _selectedLcId,
        repository.verifiedLcs
    ) { query, status, selectedId, lcs ->
        val q = query.trim().lowercase()
        val filtered = lcs.filter { lc ->
            val statusMatch = status == "ALL" || lc.status == status
            val textMatch = q.isEmpty() ||
                lc.lcNumber.lowercase().contains(q) ||
                lc.applicantName.lowercase().contains(q) ||
                lc.beneficiaryName.lowercase().contains(q) ||
                lc.issuingBank.lowercase().contains(q) ||
                lc.hsCode.lowercase().contains(q)
            statusMatch && textMatch
        }
        val activeSelected = selectedId?.takeIf { id -> filtered.any { it.id == id } }
            ?: filtered.firstOrNull()?.id

        VaultUiState(
            searchQuery = query,
            selectedStatusFilter = status,
            items = filtered,
            selectedLcId = activeSelected
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VaultUiState()
    )

    fun observeLc(lcId: Long): Flow<VerifiedLcEntity?> = repository.observeLcById(lcId)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectStatusFilter(status: String) {
        _statusFilter.value = status
    }

    fun selectLc(lcId: Long) {
        _selectedLcId.value = lcId
        _integrityState.value = IntegrityCheckState.Idle
    }

    fun deleteLc(lcId: Long) {
        viewModelScope.launch {
            repository.deleteLc(lcId)
        }
    }

    fun reverifyDocumentHash(lc: VerifiedLcEntity) {
        if (lc.sourceUri.isBlank()) {
            _integrityState.value = IntegrityCheckState.Unavailable(
                "Pre-seeded institutional reference record — SHA-256 seal is locked in local vault."
            )
            return
        }
        viewModelScope.launch {
            val result = repository.verifyStoredDocumentIntegrity(lc.sourceUri, lc.sha256Hash)
            result.onSuccess { matched ->
                _integrityState.value = if (matched) {
                    IntegrityCheckState.VerifiedMatch
                } else {
                    IntegrityCheckState.TamperMismatch
                }
            }.onFailure { err ->
                _integrityState.value = IntegrityCheckState.Unavailable(
                    err.localizedMessage ?: "Original document URI unavailable."
                )
            }
        }
    }

    fun resetIntegrityCheck() {
        _integrityState.value = IntegrityCheckState.Idle
    }
}
