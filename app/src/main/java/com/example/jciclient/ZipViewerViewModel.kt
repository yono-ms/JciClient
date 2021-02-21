package com.example.jciclient

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ZipViewerViewModel(private val remoteId: Int, val path: String) : BaseViewModel() {

    class Factory(private val remoteId: Int, val path: String) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ZipViewerViewModel(remoteId, path) as T
        }
    }

    val index by lazy { MutableLiveData<String>() }

    fun extract() {
        logger.info("extract $remoteId $path")
    }
}