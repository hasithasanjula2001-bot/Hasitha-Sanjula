package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ai.DocumentSecurityUtil
import com.example.ui.components.OfflinePrivacyPill
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
import com.example.ui.theme.SteelCyan
import com.example.ui.theme.VerifiedEmerald
import com.example.ui.theme.VerifiedEmeraldMuted
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.VerificationStage
import com.example.viewmodel.VerificationUiState

@Composable
fun VerifyLcScreen(
    uiState: VerificationUiState,
    onDocumentSelected: (Uri, String?) -> Unit,
    onRetry: () -> Unit,
    onSaveToVault: () -> Unit,
    onOpenSavedLc: (Long) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var cameraPermissionMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onDocumentSelected(uri, "image/jpeg")
        }
    }

    val pdfDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onDocumentSelected(uri, null)
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val capturedUri = pendingCameraUri
        if (success && capturedUri != null) {
            onDocumentSelected(capturedUri, "image/jpeg")
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraPermissionMessage = null
            val uri = DocumentSecurityUtil.createCameraCaptureUri(context)
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            cameraPermissionMessage = context.getString(R.string.camera_permission_rationale)
        }
    }

    val launchCameraScan = {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            cameraPermissionMessage = null
            val uri = DocumentSecurityUtil.createCameraCaptureUri(context)
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OfflinePrivacyPill()
                Text(
                    text = stringResource(R.string.verify_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = IceWhite
                )
                Text(
                    text = stringResource(R.string.verify_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate
                )
            }
        }

        item {
            IntakeActionCard(
                isProcessing = uiState is VerificationUiState.Processing,
                onScanCamera = launchCameraScan,
                onPickImage = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onUploadPdf = {
                    pdfDocLauncher.launch(
                        arrayOf("application/pdf", "image/*", "text/plain")
                    )
                }
            )
        }

        if (cameraPermissionMessage != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ElevatedNavy,
                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = cameraPermissionMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarningAmber,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        item {
            AnimatedVisibility(visible = uiState is VerificationUiState.Processing) {
                val proc = uiState as? VerificationUiState.Processing
                if (proc != null) {
                    VerificationProgressCard(proc = proc)
                }
            }
        }

        if (uiState is VerificationUiState.Error) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SanctionCrimsonMuted,
                    border = BorderStroke(1.dp, SanctionCrimson.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = SanctionCrimson
                            )
                            Text(
                                text = "Verification Error",
                                style = MaterialTheme.typography.titleSmall,
                                color = IceWhite
                            )
                        }
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftSilver
                        )
                        if (uiState.doc != null) {
                            TamperSealChip(sha256Hash = uiState.doc.sha256Hex)
                            Button(
                                onClick = onRetry,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("verify_retry_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SovereignGold,
                                    contentColor = MidnightNavy
                                )
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry AI Compliance Check", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (uiState is VerificationUiState.Success) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (uiState.savedLcId != null) VerifiedEmeraldMuted else ElevatedNavy,
                    border = BorderStroke(
                        1.dp,
                        if (uiState.savedLcId != null) VerifiedEmerald else SovereignGold.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (uiState.savedLcId == null) {
                            Text(
                                text = "Audit Complete — Ready to Commit Locally",
                                style = MaterialTheme.typography.titleSmall,
                                color = IceWhite
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onSaveToVault,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("verify_save_vault_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SovereignGold,
                                        contentColor = MidnightNavy
                                    )
                                ) {
                                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.action_save_vault), fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onReset,
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("verify_new_scan_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, HairlineSlate)
                                ) {
                                    Text("Clear", color = SoftSilver)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerifiedEmerald)
                                    Text(
                                        text = "Sealed & Saved in Local Vault",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = IceWhite
                                    )
                                }
                                TextButton(
                                    onClick = { onOpenSavedLc(uiState.savedLcId) },
                                    modifier = Modifier.testTag("verify_open_saved_button")
                                ) {
                                    Text("Inspect", color = SovereignGold, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                LcAuditReportBody(lc = uiState.result.entity)
            }
        }
    }
}

@Composable
private fun IntakeActionCard(
    isProcessing: Boolean,
    onScanCamera: () -> Unit,
    onPickImage: () -> Unit,
    onUploadPdf: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SlateNavy,
        border = BorderStroke(1.dp, HairlineSlate)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Submit Letter of Credit or Supporting Trade Document",
                style = MaterialTheme.typography.titleSmall,
                color = IceWhite
            )
            Text(
                text = "Supports SWIFT MT700 PDFs/text, Commercial Invoices, Bills of Lading, and Certificates of Origin (max 18 MB). Cryptographic SHA-256 hash is computed locally before AI inspection.",
                style = MaterialTheme.typography.bodySmall,
                color = MutedSlate
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onScanCamera,
                    enabled = !isProcessing,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("verify_scan_camera_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SovereignGold,
                        contentColor = MidnightNavy
                    )
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_scan_camera), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onPickImage,
                    enabled = !isProcessing,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("verify_pick_image_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SteelCyan.copy(alpha = 0.6f))
                ) {
                    Icon(
                        Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        tint = SteelCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_pick_image), color = SteelCyan)
                }
            }

            OutlinedButton(
                onClick = onUploadPdf,
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("verify_upload_pdf_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, HairlineSlate)
            ) {
                Icon(
                    Icons.Filled.Description,
                    contentDescription = null,
                    tint = IceWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.action_upload_pdf), color = IceWhite)
            }
        }
    }
}

@Composable
private fun VerificationProgressCard(proc: VerificationUiState.Processing) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ElevatedNavy,
        border = BorderStroke(1.dp, SovereignGold.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = SovereignGold,
                    strokeWidth = 2.5.dp
                )
                Column {
                    Text(
                        text = "Inspecting ${proc.doc.fileName}",
                        style = MaterialTheme.typography.titleSmall,
                        color = IceWhite
                    )
                    Text(
                        text = "${proc.doc.mimeType} · ${proc.doc.sizeBytes / 1024} KB",
                        style = MonoMetricStyle,
                        color = MutedSlate
                    )
                }
            }
            TamperSealChip(sha256Hash = proc.doc.sha256Hex)

            StageRow("1. Local SHA-256 Integrity Seal", done = true, active = proc.stage == VerificationStage.HASHING)
            StageRow(
                "2. Gemini Multimodal MT700 & UCP 600 Extraction",
                done = proc.stage == VerificationStage.COMPLIANCE_RULES,
                active = proc.stage == VerificationStage.AI_EXTRACTION
            )
            StageRow(
                "3. Sanctions, Dual-Use & Exposure Limit Engine",
                done = false,
                active = proc.stage == VerificationStage.COMPLIANCE_RULES
            )
        }
    }
}

@Composable
private fun StageRow(label: String, done: Boolean, active: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (done) Icons.Filled.CheckCircle else Icons.Filled.VerifiedUser,
            contentDescription = null,
            tint = when {
                done -> VerifiedEmerald
                active -> SovereignGold
                else -> MutedSlate
            },
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (done || active) IceWhite else MutedSlate
        )
    }
}
