package com.example.letitgoat.ui.sell

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class SellViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is sell Fragment"
    }
    val text: LiveData<String> = _text
}