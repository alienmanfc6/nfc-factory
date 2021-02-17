package com.alienmantech.nfcfactory.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReadTagViewModel : ViewModel() {

    val output = MutableLiveData<String>()

    fun init() {

    }

    fun updateOutput() {
        output.postValue("test")
    }
}