package com.example.jciclient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VideoViewerViewModel(private val remoteId: Int, private val path: String) : BaseViewModel() {
    class Factory(private val remoteId: Int, private val path: String) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return VideoViewerViewModel(remoteId, path) as T
        }
    }
}