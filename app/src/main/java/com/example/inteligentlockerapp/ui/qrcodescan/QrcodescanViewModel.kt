package com.example.inteligentlockerapp.ui.qrcodescan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QrcodescanViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is QRCode Scanner Fragment"
    }
    val text: LiveData<String> = _text
}