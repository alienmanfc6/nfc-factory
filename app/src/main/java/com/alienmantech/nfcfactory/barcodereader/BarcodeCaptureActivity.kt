/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alienmantech.nfcfactory.barcodereader

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import com.alienmantech.nfcfactory.barcodereader.ui.camera.CameraSourcePreview
import com.alienmantech.nfcfactory.barcodereader.ui.camera.GraphicOverlay
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.os.Bundle
import com.alienmantech.nfcfactory.R
import android.annotation.SuppressLint
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.Barcode
import android.content.IntentFilter
import android.content.Intent
import android.widget.Toast
import kotlin.Throws
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import android.app.Activity
import android.hardware.Camera
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.alienmantech.nfcfactory.barcodereader.ui.camera.CameraSource
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.lang.NullPointerException

/**
 * Activity for the multi-tracker app.  This app detects barcodes and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and ID of each barcode.
 */
class BarcodeCaptureActivity : AppCompatActivity() {
    private var autoFocus = true
    private var useFlash = false

    private var cameraSource: CameraSource? = null
    private val callback: BarcodeDetectedCallback = BarcodeDetectedCallback { format, barcode ->
        returnResults(
            format,
            barcode
        )
    }

    private lateinit var preview: CameraSourcePreview
    private lateinit var graphicOverlay: GraphicOverlay<BarcodeGraphic>
    private lateinit var fab: FloatingActionButton

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()

        // trying to use camera for the scan
        initCamera(false)
    }

    private fun setupUI() {
        setContentView(R.layout.activity_barcode_scanner)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar.apply {
            this?.setDisplayHomeAsUpEnabled(true)
            this?.setHomeAsUpIndicator(R.drawable.ic_close)
        }

        preview = findViewById(R.id.preview)
        graphicOverlay = findViewById(R.id.graphicOverlay)
        fab = findViewById(R.id.fab)

        fab.setOnClickListener { toggleTorch() }
    }

    override fun onResume() {
        super.onResume()

        startCamera()
    }

    override fun onPause() {
        super.onPause()

        stopCamera()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            returnResults(-1, null)
            false
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     *
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private fun initCamera(startAfterInit: Boolean) {
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        val rc = ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
        if (rc != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
            return
        }

        // do we already have the camera source?
        if (cameraSource == null) {
            // nope, build it now
            cameraSource = buildCameraSource()
        }

        if (startAfterInit) {
            startCamera()
        }
    }

    private fun buildCameraSource(): CameraSource {
        val context = applicationContext

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        val barcodeDetector = BarcodeDetector.Builder(context).build()
        val barcodeFactory = BarcodeTrackerFactory(graphicOverlay, callback)
        barcodeDetector.setProcessor(
            MultiProcessor.Builder(barcodeFactory).build()
        )
        if (!barcodeDetector.isOperational) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            val lowstorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
            val hasLowStorage = registerReceiver(null, lowstorageFilter) != null
            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show()
            }
        }

        // try to set preview close to the screen site
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        var builder = CameraSource.Builder(
            applicationContext, barcodeDetector
        )
            .setFacing(CameraSource.CAMERA_FACING_BACK) //.setRequestedPreviewSize(1600, 1024)
            .setRequestedPreviewSize(screenWidth, screenHeight)
            .setRequestedFps(15.0f)

        // make sure that auto focus is an available option
        builder = builder.setFocusMode(
            if (autoFocus) Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE else Camera.Parameters.FOCUS_MODE_MACRO
        )
        return builder
            .setFlashMode(if (useFlash) Camera.Parameters.FLASH_MODE_TORCH else null)
            .build()
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    @Throws(SecurityException::class)
    private fun startCamera() {
        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            applicationContext
        )
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }
        try {
            preview.start(cameraSource, graphicOverlay)
        } catch (e: IOException) {
            if (cameraSource != null) {
                cameraSource?.release()
                cameraSource = null
            }
        } catch (e: NullPointerException) {
            if (cameraSource != null) {
                cameraSource?.release()
                cameraSource = null
            }
        }
    }

    private fun stopCamera() {
        preview.stop()
    }

    private fun returnResults(format: Int, rawValue: String?) {
        val i = Intent()
        if (format >= 0) {
            i.putExtra(RETURN_BARCODE_FORMAT, format)
        }
        if (rawValue != null) {
            i.putExtra(RETURN_BARCODE, rawValue)
        }
        setResult(RESULT_OK, i)
        finish()
    }

    private fun toggleFocus() {
        autoFocus = if (autoFocus) {
            // disable auto focus
            cameraSource?.focusMode = Camera.Parameters.FOCUS_MODE_MACRO
            false
        } else {
            // enable auto focus
            cameraSource?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            true
        }
    }

    private fun toggleTorch() {
        if (cameraSource == null) return
        useFlash = if (useFlash) {
            cameraSource?.flashMode = Camera.Parameters.FLASH_MODE_OFF
            false
        } else {
            cameraSource?.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            true
        }
    }

    private fun barcodeDisplayFormat(format: Int): String {
        return when (format) {
            Barcode.AZTEC -> "AZTEC"
            Barcode.CODABAR -> "CODABAR"
            Barcode.CODE_39 -> "CODE_39"
            Barcode.CODE_93 -> "CODE_93"
            Barcode.CODE_128 -> "CODE_128"
            Barcode.DATA_MATRIX -> "DATA_MATRIX"
            Barcode.EAN_8 -> "EAN_8"
            Barcode.EAN_13 -> "EAN_13"
            Barcode.ISBN -> "ISBN"
            Barcode.ITF -> "ITF"
            Barcode.PDF417 -> "PDF417"
            Barcode.UPC_A -> "UPC_A"
            Barcode.UPC_E -> "UPC_E"
            else -> "Unknown: $format"
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private fun requestCameraPermission() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }
        val thisActivity: Activity = this
        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(
                thisActivity, permissions,
                RC_HANDLE_CAMERA_PERM
            )
        }
        findViewById<View>(R.id.root_layout).setOnClickListener(listener)
        Snackbar.make(
            graphicOverlay!!, R.string.permission_camera_rationale,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.common_ok, listener)
            .show()
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on [.requestPermissions].
     *
     *
     * **Note:** It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     *
     *
     * @param requestCode  The request code passed in [.requestPermissions].
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either [PackageManager.PERMISSION_GRANTED]
     * or [PackageManager.PERMISSION_DENIED]. Never null.
     * @see .requestPermissions
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (grantResults.isNotEmpty() && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            // we have permission, so create the camerasource now
            initCamera(true)
            return
        }
    }

    companion object {
        private const val TAG = "BarcodeCaptureActivity"

        // intent request code to handle updating play services if needed.
        private const val RC_HANDLE_GMS = 9001

        // permission request codes need to be < 256
        private const val RC_HANDLE_CAMERA_PERM = 2
        const val RETURN_BARCODE = "com.alienmantech.barcode.BARCODE"
        const val RETURN_BARCODE_FORMAT = "com.alienmantech.barcode.BARCODE_FORMAT"

        /**
         * Helper method for staring the service.
         *
         * @param context Context
         * @param rc      int - The return code id.
         */
        fun scanBarcode(context: Activity, rc: Int) {
            val i = Intent(context, BarcodeCaptureActivity::class.java)
            context.startActivityForResult(i, rc)
        }
    }
}