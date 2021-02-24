package com.alienmantech.nfcfactory.barcodereader;

public interface BarcodeDetectedInterface {
    void onBarcodeDetected(int format, String barcode);
}

