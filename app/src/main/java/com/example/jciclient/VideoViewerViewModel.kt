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

    val controlVisible by lazy { MutableLiveData(false) }

    val playing by lazy { MutableLiveData(false) }

    val playButtonResId = Transformations.map(playing) {
        if (it) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
    }

    val fileName by lazy { MutableLiveData(path.split('/').last()) }

    val uriString by lazy { MutableLiveData<String>() }

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