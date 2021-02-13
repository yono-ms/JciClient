package com.example.jciclient

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VideoViewerViewModel(private val path: String) : BaseViewModel() {
    class Factory(private val path: String) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return VideoViewerViewModel(path) as T
        }
    }

    val controlVisible by lazy { MutableLiveData(false) }

    val playing by lazy { MutableLiveData(false) }

    val playButtonResId = Transformations.map(playing) {
        if (it) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
    }

    val fileName by lazy { MutableLiveData(path.split('/').last()) }

    fun toggleControlVisible() {
        controlVisible.value?.let {
            controlVisible.value = !it
        }
    }
}