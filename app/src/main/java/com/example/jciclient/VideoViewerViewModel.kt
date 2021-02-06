package com.example.jciclient

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class VideoViewerViewModel(private val remoteId: Int, private val path: String) : BaseViewModel() {
    class Factory(private val remoteId: Int, private val path: String) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return VideoViewerViewModel(remoteId, path) as T
        }
    }

    val videoUri by lazy { MutableLiveData<Uri>() }

    fun downloadFile(dir: String) {
        logger.info("downloadFile id=$remoteId path=$path")
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                progress.postValue(true)
                val credentials = App.db.remoteDao().get(remoteId)?.credentials()
                val baseContext = BaseContext(PropertyConfiguration(App.cifsProperties))
                val cifsContext = baseContext.withCredentials(credentials)
                val smbFile = SmbFile(path, cifsContext)
                val destFile = File(dir, smbFile.name)
                smbFile.inputStream.use { inputStream ->
                    destFile.outputStream().use { outputStream ->
                        logger.info("copy start.")
                        val result = inputStream.copyTo(outputStream)
                        logger.info("copy end. $result")
                    }
                }
                Uri.fromFile(destFile)
            }.onSuccess {
                videoUri.postValue(it)
            }.onFailure {
                logger.error("getFiles", it)
                throwable.postValue(it)
            }.also {
                progress.postValue(false)
            }
        }
    }
}