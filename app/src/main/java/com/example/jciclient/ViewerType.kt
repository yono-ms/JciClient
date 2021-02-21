package com.example.jciclient

import android.webkit.MimeTypeMap
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

enum class ViewerType(
    private val mimeTypes: Set<String>
) {
    IMAGE(
        setOf("image/")
    ),
    VIDEO(
        setOf("video/")
    ),
    ZIP(
        setOf("application/zip")
    ),
    EXTERNAL(
        setOf()
    ),
    UNKNOWN(
        setOf()
    );

    companion object {

        private val logger: Logger by lazy { LoggerFactory.getLogger("ViewerType") }

        fun fromPath(path: String): ViewerType {
            val ext = File(path).extension
            if (runBlocking { App.db.externalDao().getAll().any { e -> e.ext == ext } }) {
                return EXTERNAL
            }
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)?.let { mimeType ->
                logger.info("mimeType=$mimeType")
                values().forEach { viewerType ->
                    viewerType.mimeTypes.forEach {
                        if (mimeType.startsWith(it)) {
                            return viewerType
                        }
                    }
                }
                return UNKNOWN
            } ?: UNKNOWN
        }
    }
}