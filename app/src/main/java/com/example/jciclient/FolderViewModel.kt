package com.example.jciclient

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class FolderViewModel : BaseViewModel() {

    fun downloadFile(id: Int, path: String, dir: String) {
        logger.info("downloadFile id=$id path=$path")
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                progress.postValue(true)
                val credentials = App.db.remoteDao().get(id)?.let { entity ->
                    NtlmPasswordAuthenticator(
                        entity.domainName,
                        entity.accountName,
                        entity.accountPassword
                    )
                }
                val baseContext = BaseContext(PropertyConfiguration(Properties().apply {
                    setProperty("jcifs.smb.client.minVersion", "SMB202")
                    setProperty("jcifs.smb.client.maxVersion", "SMB300")
                }))
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
                destFile.path
            }.onSuccess {
                filePath.postValue(it)
            }.onFailure {
                logger.error("getFiles", it)
                throwable.postValue(it)
            }.also {
                progress.postValue(false)
            }
        }
    }

    val filePath by lazy { MutableLiveData<String>() }

    fun getFiles(id: Int, path: String) {
        logger.info("getFiles id=$id path=$path")
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                progress.postValue(true)
                App.db.remoteDao().get(id)?.let { entity ->
                    val credentials = NtlmPasswordAuthenticator(
                        entity.domainName,
                        entity.accountName,
                        entity.accountPassword
                    )
                    val baseContext = BaseContext(PropertyConfiguration(Properties().apply {
                        setProperty("jcifs.smb.client.minVersion", "SMB202")
                        setProperty("jcifs.smb.client.maxVersion", "SMB300")
                    }))
                    val cifsContext = baseContext.withCredentials(credentials)
                    val smbFile = SmbFile(path, cifsContext)
                    smbFile.run {
                        logger.debug(parent)
                        logger.debug(path)
                        logger.debug(contentType)
                        logger.debug(name)
                        logger.debug("isFile=${isFile}")
                        logger.debug("isDirectory=${isDirectory}")
                    }
                    val fileItems = mutableListOf<FileItem>()
                    smbFile.list().forEach { child ->
                        SmbFile("${smbFile.path}$child", cifsContext).let {
                            logger.debug("${it.name} isFile=${it.isFile} isDirectory=${it.isDirectory} isHidden=${it.isHidden}")
                            if (!it.isHidden) {
                                fileItems.add(FileItem.from(it))
                            }
                            it.close()
                        }
                    }
                    items.postValue(fileItems.sortedWith(compareBy({ it.file }, { it.name })))
                    smbFile.close()
                } ?: logger.error("entity is null.")
            }.onFailure {
                logger.error("getFiles", it)
                throwable.postValue(it)
            }.also {
                progress.postValue(false)
            }
        }
    }

    val items by lazy { MutableLiveData<List<FileItem>>() }

    data class FileItem(
        val path: String,
        val name: String,
        val contentType: String?,
        val directory: Boolean,
        val file: Boolean,
    ) {
        companion object {
            fun from(smbFile: SmbFile): FileItem {
                return FileItem(
                    smbFile.path,
                    smbFile.name,
                    smbFile.contentType,
                    smbFile.isDirectory,
                    smbFile.isFile
                )
            }
        }
    }
}