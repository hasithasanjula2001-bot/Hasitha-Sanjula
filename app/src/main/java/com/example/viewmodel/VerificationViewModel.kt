package com.example.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.PreparedDocument
import com.example.data.EvaluatedLcResult
import com.example.data.TradeComplianceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class VerificationStage {
    HASHING,
    AI_EXTRACTION,
    COMPLIANCE_RULES
}

sealed interface VerificationUiState {
    data object Idle : VerificationUiState
    data class DocumentReady(val doc: PreparedDocument) : VerificationUiState
    data class Processing(val doc: PreparedDocument, val stage: VerificationStage) : VerificationUiState
    data class Success(
        val doc: PreparedDocument,
        val result: EvaluatedLcResult,
        val savedLcId: Long? = null
    ) : VerificationUiState
    data class Error(
        val message: String,
        val doc: PreparedDocument? = null
    ) : VerificationUiState
}

class VerificationViewModel(
    private val repository: TradeComplianceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VerificationUiState>(VerificationUiState.Idle)
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    fun onDocumentSelected(uri: Uri, fallbackMime: String? = null) {
        viewModelScope.launch {
            val prepResult = repository.prepareDocumentForVerification(uri, fallbackMime)
            prepResult.onSuccess { doc ->
                runVerification(doc)
            }.onFailure { err ->
                _uiState.value = VerificationUiState.Error(
                    message = err.localizedMessage ?: "Unable to read document from storage."
                )
            }
        }
    }

    fun retryCurrentDocument() {
        val doc = when (val current = _uiState.value) {
            is VerificationUiState.Error -> current.doc
            is VerificationUiState.DocumentReady -> current.doc
            else -> null
        } ?: return
        runVerification(doc)
    }

    private fun runVerification(doc: PreparedDocument) {
        viewModelScope.launch {
            _uiState.value = VerificationUiState.Processing(doc, VerificationStage.HASHING)
            _uiState.value = VerificationUiState.Processing(doc, VerificationStage.AI_EXTRACTION)

            val result = repository.verifyPreparedDocument(doc)
            result.onSuccess { evaluated ->
                _uiState.value = VerificationUiState.Processing(doc, VerificationStage.COMPLIANCE_RULES)
                _uiState.value = VerificationUiState.Success(
                    doc = doc,
                    result = evaluated,
                    savedLcId = null
                )
            }.onFailure { err ->
                _uiState.value = VerificationUiState.Error(
                    message = err.localizedMessage ?: "Verification failed. Please retry.",
                    doc = doc
                )
            }
        }
    }

    fun saveResultToVault() {
        val current = _uiState.value as? VerificationUiState.Success ?: return
        if (current.savedLcId != null) return
        viewModelScope.launch {
            val newId = repository.saveVerifiedLc(current.result.entity)
            _uiState.value = current.copy(savedLcId = newId)
        }
    }

    fun reset() {
        _uiState.value = VerificationUiState.Idle
    }
}
