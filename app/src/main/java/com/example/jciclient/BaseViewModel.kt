package com.example.jciclient

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class BaseViewModel : ViewModel() {
    val logger: Logger by lazy { LoggerFactory.getLogger(javaClass.simpleName) }

    val progress by lazy { MutableLiveData(false) }
    val throwable by lazy { MutableLiveData<Throwable>() }
}