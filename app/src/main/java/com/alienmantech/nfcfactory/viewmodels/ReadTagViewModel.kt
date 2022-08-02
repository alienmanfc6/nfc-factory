package com.alienmantech.nfcfactory.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alienmantech.nfcfactory.Utils
import com.alienmantech.nfcfactory.models.NfcTag

class ReadTagViewModel : ViewModel() {

    val output = MutableLiveData<String>()
    val writeNextButtonEnabled = MutableLiveData(false)

    var tag: NfcTag? = null
        set(value) {
            field = value

            output.value = value?.print()
            writeNextButtonEnabled.value = (tag?.getTagBarcode() != null)
        }
}