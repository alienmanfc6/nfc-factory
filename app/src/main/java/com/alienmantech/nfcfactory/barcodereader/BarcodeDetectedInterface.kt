package com.alienmantech.nfcfactory.barcodereader

fun interface BarcodeDetectedInterface {
    fun onBarcodeDetected(format: Int, barcode: String)
}