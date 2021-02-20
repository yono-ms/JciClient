package com.example.jciclient

import android.webkit.MimeTypeMap
import androidx.lifecycle.*
import fi.iki.elonen.NanoHTTPD
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.SmbFile
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.Socket

class VideoViewerViewModel(private val remoteId: Int, private val path: String) : BaseViewModel() {
    class Factory(private val remoteId: Int, private val path: String) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return VideoViewerViewModel(remoteId, path) as T
        }
    }

    companion object {
        const val SEEK_BAR_MAX = 100
    }

    val controlVisible by lazy { MutableLiveData(false) }

    val playing by lazy { MutableLiveData(false) }

    val playButtonResId = Transformations.map(playing) {
        if (it) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
    }

    val position by lazy { MutableLiveData(0F) }

    val seekBarProgress = Transformations.map(position) {
        (it * SEEK_BAR_MAX).toInt()
    }

    val seekBarMax by lazy { MutableLiveData(SEEK_BAR_MAX) }

    val time by lazy { MutableLiveData(0L) }

    val timeString = Transformations.map(time) {
        stringForTime(it)
    }

    val length by lazy { MutableLiveData(0L) }

    val lengthString = Transformations.map(length) {
        stringForTime(it)
    }

    val fileName by lazy { MutableLiveData(path.split('/').last()) }

    val uriString by lazy { MutableLiveData<String>() }

    private fun stringForTime(time: Long): String {
        val total = time / 1000
        val sec = total % 60
        val min = (total / 60) % 60
        val hour = total / (60 * 60)
        return "%02d:%02d:%02d".format(hour, min, sec)
    }

    fun toggleControlVisible() {
        controlVisible.value?.let {
            controlVisible.value = !it
        }
    }

    fun startWebServer() {
        logger.info("startWebServer")
        viewModelScope.launch {
            kotlin.runCatching {
                val port = Socket().use {
                    it.bind(null)
                    it.localPort
                }
                webServer = BuiltInWebServer(port)
                webServer.start()
                "http://localhost:$port/${fileName.value}"
            }.onSuccess {
                uriString.value = it
            }.onFailure {
                logger.error("startWebServer", it)
                throwable.value = it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        logger.info("onCleared")
        kotlin.runCatching {
            if (webServer.isAlive) {
                webServer.closeAllConnections()
                webServer.stop()
            }
        }.onFailure {
            logger.error("startWebServer", it)
        }
    }

    private lateinit var webServer: BuiltInWebServer

    lateinit var smbFile: SmbFile

    inner class BuiltInWebServer(port: Int) : NanoHTTPD(port) {
        override fun serve(session: IHTTPSession?): Response {
            logger.info("serve ${session?.uri}")
            kotlin.runCatching {
                val uriFileName = session?.uri?.split('/')?.last()
                val pathFileName = path.split('/').last()
                if (uriFileName != pathFileName) {
                    return newFixedLengthResponse(
                        Response.Status.NOT_FOUND,
                        "text/plain",
                        session?.uri
                    )
                }
                val mimeType = File(path).extension.let {
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(it)
                }
                val credentials = runBlocking { App.db.remoteDao().get(remoteId)?.credentials() }
                val baseContext = BaseContext(PropertyConfiguration(App.cifsProperties))
                val cifsContext = baseContext.withCredentials(credentials)
                smbFile = SmbFile(path, cifsContext)
                val stream = smbFile.inputStream
                return newChunkedResponse(Response.Status.OK, mimeType, stream)
            }.onFailure {
                logger.error("serve", it)
                return newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR,
                    "text/plain",
                    it.message
                )
            }
            return super.serve(session)
        }

        override fun closeAllConnections() {
            super.closeAllConnections()
            logger.info("closeAllConnections")
            kotlin.runCatching {
                smbFile.close()
            }.onFailure {
                logger.error("closeAllConnections", it)
            }
        }
    }
}