package com.example.rpmp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.rpmp.databinding.ActivityScannerBinding
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityScannerBinding
    private lateinit var cameraExecutor: ExecutorService
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraController = LifecycleCameraController(baseContext)
        val previewView: PreviewView = viewBinding.viewFinder

        /*cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            MlKitAnalyzer(
                listOf(textRecognizer),
                CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result: MlKitAnalyzer.Result? ->
                val textResults = result?.getValue(textRecognizer)
                if (textResults == null) {
                    previewView.overlay.clear()
                    return@MlKitAnalyzer
                }

                val scannerDrawable = ScannerDrawable(textResults.textBlocks)

                previewView.setOnTouchListener { v: View, e: MotionEvent ->
                    if (e.action == MotionEvent.ACTION_UP) {
                        v.performClick()
                    }
                    for (block in textResults.textBlocks) {
                        for (line in block.lines) {
                            val tmp = SEARCH_REGEX
                            val matches = SEARCH_REGEX.findAll(line.text)

                            var currentElementIndex = 0
                            var currentSymbolIndex = 0
                            var currentCharacterIndex = 0

                            for (match in matches) {
                                var rect: Rect? = null
                                match_loop@ while (currentElementIndex < line.elements.size) {
                                    val element = line.elements[currentElementIndex]
                                    if (rect == null && currentCharacterIndex + element.text.length - currentSymbolIndex > match.range.first || rect != null && currentCharacterIndex + element.text.length - currentSymbolIndex > match.range.last) {
                                        while (currentSymbolIndex < element.symbols.size) {
                                            val symbol = element.symbols[currentSymbolIndex]
                                            if (currentCharacterIndex == match.range.first) {
                                                rect = symbol.boundingBox!!
                                            }
                                            if (currentCharacterIndex == match.range.last) {
                                                rect!!.right = symbol.boundingBox!!.right
                                                break@match_loop
                                            }
                                            currentCharacterIndex++
                                            currentSymbolIndex++
                                        }
                                        currentSymbolIndex = 0
                                    } else {
                                        currentCharacterIndex += element.symbols.size - currentSymbolIndex
                                    }
                                    currentElementIndex++
                                    currentCharacterIndex++
                                }
                                if (e.action == MotionEvent.ACTION_DOWN && rect!!.contains(e.getX().toInt(), e.getY().toInt())) {
                                    val resultIntent = Intent()
                                    resultIntent.putExtra("result", match.value)
                                    setResult(RESULT_OK, resultIntent)
                                    finish()
                                }
                            }
                        }
                    }
                    true
                }

                previewView.overlay.clear()
                previewView.overlay.add(scannerDrawable)
            }
        )*/

        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            MlKitAnalyzer(
                listOf(textRecognizer),
                CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result: MlKitAnalyzer.Result? ->
                val textResults = result?.getValue(textRecognizer)
                if (textResults == null) {
                    previewView.overlay.clear()
                    return@MlKitAnalyzer
                }

                val scannerDrawable = ScannerDrawable(textResults.textBlocks)

                previewView.setOnTouchListener { v: View, e: MotionEvent ->
                    if (e.action == MotionEvent.ACTION_UP) {
                        v.performClick()
                    }
                    for (block in textResults.textBlocks) {
                        for (line in block.lines) {
                            if (e.action == MotionEvent.ACTION_DOWN && line.boundingBox!!.contains(e.getX().toInt(), e.getY().toInt())) {
                                val resultIntent = Intent()
                                resultIntent.putExtra("result", line.text)
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            }
                        }
                    }
                    true
                }

                previewView.overlay.clear()
                previewView.overlay.add(scannerDrawable)
            }
        )

        cameraController.bindToLifecycle(this)
        previewView.controller = cameraController
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        textRecognizer.close()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).toTypedArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}