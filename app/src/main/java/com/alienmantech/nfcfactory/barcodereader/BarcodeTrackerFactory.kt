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

import com.alienmantech.nfcfactory.barcodereader.ui.camera.GraphicOverlay
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.Tracker

/**
 * Factory for creating a tracker and associated graphic to be associated with a new barcode.  The
 * multi-processor uses this factory to create barcode trackers as needed -- one for each barcode.
 */
internal class BarcodeTrackerFactory(
    private val graphicOverlay: GraphicOverlay<BarcodeGraphic>,
    private val callback: BarcodeDetectedCallback
) : MultiProcessor.Factory<Barcode> {

    override fun create(barcode: Barcode): Tracker<Barcode> {
        // trigger the callback for barcode detection
        callback.triggerBarcodeDetected(barcode.format, barcode.rawValue)
        val graphic = BarcodeGraphic(graphicOverlay)
        return BarcodeGraphicTracker(graphicOverlay, graphic)
    }
}