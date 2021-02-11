package com.example.jciclient

import android.webkit.MimeTypeMap
import fi.iki.elonen.NanoHTTPD
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.SmbFile
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class BridgeWebServer(
    port: Int,
    private val remoteId: Int,
    private val path: String
) : NanoHTTPD(port) {

    val logger: Logger by lazy { LoggerFactory.getLogger(javaClass.simpleName) }

    private lateinit var smbFile: SmbFile

    override fun serve(session: IHTTPSession?): Response {
        logger.info("serve")
        kotlin.runCatching {
            val uriFileName = session?.uri?.split('/')?.last()
            val pathFileName = path.split('/').last()
            logger.debug("uriFileName=$uriFileName pathFileName=$pathFileName")
            if (uriFileName != pathFileName) {
                throw Throwable("unknown uriFileName=$uriFileName")
            }

            val ext = File(path).extension
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)

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