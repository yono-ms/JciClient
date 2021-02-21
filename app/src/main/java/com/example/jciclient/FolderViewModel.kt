package com.example.jciclient

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jciclient.database.FileEntity
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class FolderViewModel(private val remoteId: Int, val path: String) : BaseViewModel() {

    class Factory(private val remoteId: Int, val path: String) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FolderViewModel(remoteId, path) as T
        }
    }

    val orientation by lazy { MutableLiveData<Int>() }

    val items by lazy { App.db.fileDao().getLiveData(remoteId, path) }

    fun getFiles() {
        logger.info("getFiles id=$remoteId path=$path")
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                progress.postValue(true)
                App.db.remoteDao().get(remoteId)?.let { entity ->
                    val credentials = entity.credentials()
                    val baseContext = BaseContext(PropertyConfiguration(App.cifsProperties))
                    val cifsContext = baseContext.withCredentials(credentials)
                    val smbFile = SmbFile(path, cifsContext)
                    smbFile.run {
                        logger.debug(parent)
                        logger.debug(path)
                        logger.debug(contentType)
                        logger.debug(name)
                        logger.debug("isFile=${isFile}")
                        logger.debug("isDirectory=${isDirectory}")
                        logger.debug("lastModified=$lastModified")
                    }
                    App.db.fileDao().get(remoteId, path)?.lastModified?.let {
                        val dbDate = Date(it)
                        val smbDate = Date(smbFile.lastModified)
                        logger.debug("smbDate=$smbDate")
                        logger.debug(" dbDate=$dbDate")
                        if (smbFile.lastModified != it) {
                            logger.warn("folder update.")
                            App.db.fileDao().deleteAllChildren(remoteId, path)
                        }
                    }
                    if (App.db.fileDao().getChildrenCount(remoteId, path) > 0) {
                        logger.warn("already download.")
                        return@runCatching
                    }
                    smbFile.listFiles { e ->
                        e.isDirectory
                    }.sortedBy { e ->
                        e.name
                    }.forEach { child ->
                        logger.debug("${child.name} isFile=${child.isFile} isDirectory=${child.isDirectory} isHidden=${child.isHidden}")
                        if (!child.isHidden) {
                            App.db.fileDao().insertAll(FileEntity.from(remoteId, child))
                        }
                        child.close()
                    }
                    smbFile.listFiles { e ->
                        e.isFile
                    }.sortedBy { e ->
                        e.name
                    }.forEach { child ->
                        logger.debug("${child.name} isFile=${child.isFile} isDirectory=${child.isDirectory} isHidden=${child.isHidden}")
                        if (!child.isHidden) {
                            App.db.fileDao().insertAll(FileEntity.from(remoteId, child))
                        }
                        child.close()
                    }
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
}