package com.example.aplicacionmovilnpi.qr

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.aplicacionmovilnpi.model.Node
import com.example.aplicacionmovilnpi.ui.LocalIsEnglish
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun QrScannerScreen(
    onQrResult: (String) -> Unit
) {
    val context = LocalContext.current
    val isEnglish = LocalIsEnglish.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                val scanner = BarcodeScanning.getClient()
                                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    processImageProxy(scanner, imageProxy, onQrResult)
                                }
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isEnglish) "Camera permission is required to scan QR codes." else "Se necesita permiso de cámara para escanear el código QR.")
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onQrResult: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrEmpty()) {
                        onQrResult(rawValue)
                        break
                    }
                }
            }
            .addOnFailureListener {
                // Ignorar errores individuales de análisis
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

@Composable
fun QrLocationScannerScreen(
    onResultDetected: (QrScanResult) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val isEnglish = LocalIsEnglish.current
    var isProcessing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        QrScannerScreen(
            onQrResult = { rawQrContent ->
                if (!isProcessing) {
                    isProcessing = true
                    val result = QrLocationResolver.resolveQrCodeResult(rawQrContent)

                    if (result != null) {
                        when (result) {
                            is QrScanResult.Location -> {
                                val locationName = result.poi?.let { if (isEnglish && it.nameEn.isNotBlank()) it.nameEn else it.nameEs } ?: result.node.id
                                Toast.makeText(
                                    context,
                                    if (isEnglish) "Location set: $locationName (Floor ${result.node.floor})" else "Ubicación fijada: $locationName (Planta ${result.node.floor})",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            is QrScanResult.Route -> {
                                Toast.makeText(
                                    context,
                                    if (isEnglish) "📍 Route loaded from QR: ${result.startPoi.nameEn} ➔ ${result.destPoi.nameEn}" else "📍 Ruta cargada por QR: ${result.startPoi.nameEs} ➔ ${result.destPoi.nameEs}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        onResultDetected(result)
                    } else {
                        Toast.makeText(
                            context,
                            if (isEnglish) "Unrecognized QR code: $rawQrContent" else "Código QR no reconocido: $rawQrContent",
                            Toast.LENGTH_SHORT
                        ).show()
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isProcessing = false
                        }, 1500)
                    }
                }
            }
        )

        Button(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(if (isEnglish) "Cancel scan" else "Cancelar escaneo")
        }
    }
}

@Composable
@JvmName("QrLocationScannerScreenOnNode")
fun QrLocationScannerScreen(
    onNodeDetected: (Node) -> Unit,
    onCancel: () -> Unit
) {
    QrLocationScannerScreen(
        onResultDetected = { result ->
            when (result) {
                is QrScanResult.Location -> onNodeDetected(result.node)
                is QrScanResult.Route -> {
                    com.example.aplicacionmovilnpi.navigation.EtsiitData.nodes[result.startPoi.nodeId]?.let { onNodeDetected(it) }
                }
            }
        },
        onCancel = onCancel
    )
}