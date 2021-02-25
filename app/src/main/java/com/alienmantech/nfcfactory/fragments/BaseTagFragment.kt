package com.alienmantech.nfcfactory.fragments

import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * Allows us to implements some methods that all fragments handling NFC tags should have.
 */
open class BaseTagFragment : Fragment() {

    /**
     * Handling the intent from the system when a new tag is scanned.
     */
    open fun processTag(intent: Intent) {

    }

    /**
     * When a barcode is scanned by the main activity we can read it here.
     */
    open fun processBarcodeRead(format: Int, barcode: String) {

    }
}