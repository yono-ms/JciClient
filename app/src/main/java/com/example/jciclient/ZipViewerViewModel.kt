package com.example.jciclient

import androidx.lifecycle.*
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ZipViewerViewModel(private val remoteId: Int, val path: String) : BaseViewModel() {

    class Factory(private val remoteId: Int, val path: String) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ZipViewerViewModel(remoteId, path) as T
        }
    }

    data class EntryInfo(
        val name: String,
        val cacheName: String,
    )

    companion object {
        const val MAX_SIZE = 1024 * 1024 * 500
        const val SUFFIX = ".tmp"
        const val ZIP_FILE_NAME = "zip.tmp"
    }

    val items by lazy { MutableLiveData<List<EntryInfo>>() }

    val index by lazy { MutableLiveData(0) }

    private val max = Transformations.map(items) {
        it.size
    }

    val displayIndex by lazy {
        MediatorLiveData<String>().apply {
            addSource(index) {
                value = "$it/${max.value ?: "-"}"
            }
            addSource(max) {
                value = "${index.value}/$it"
            }
        }
    }

    fun extract(dir: String) {
        logger.info("extract $remoteId $path $dir")
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                progress.postValue(true)
                deleteAllCacheFiles(dir)
                val credentials = App.db.remoteDao().get(remoteId)?.credentials()
                val baseContext = BaseContext(PropertyConfiguration(App.cifsProperties))
                val cifsContext = baseContext.withCredentials(credentials)
                val smbFile = SmbFile(path, cifsContext)
                if (smbFile.length() > MAX_SIZE) {
                    throw Throwable("smbFile length=${smbFile.length()} over ${MAX_SIZE}.")
                }
                val list = mutableListOf<EntryInfo>()
                val zipFile = File(dir, ZIP_FILE_NAME)
                smbFile.inputStream.use { inputStream ->
                    zipFile.outputStream().use { outputStream ->
                        logger.info("copy start.")
                        val result = inputStream.copyTo(outputStream)
                        logger.info("copy end. $result")
                    }
                }
                zipFile.inputStream().use { fileInputStream ->
                    ZipInputStream(fileInputStream).use { zipInputStream ->
                        var zipEntry = zipInputStream.nextEntry
                        while (zipEntry != null) {
                            if (checkImageEntry(zipEntry)) {
                                val cacheName = getTempFileName()
                                val destFile = File(dir, cacheName)
                                destFile.outputStream().use { outputStream ->
                                    val result = zipInputStream.copyTo(outputStream)
                                    logger.trace("${zipEntry.name} $result bytes.")
                                    list.add(EntryInfo(zipEntry.name, destFile.path))
                                }
                            }

                            zipInputStream.closeEntry()
                            zipEntry = zipInputStream.nextEntry
                        }
                    }
                }
                list
            }.onSuccess {
                logger.debug("$it")
                items.postValue(it)
            }.onFailure {
                logger.error("extract", it)
                throwable.postValue(it)
            }.also {
                progress.postValue(false)
            }
        }
    }

    private fun deleteAllCacheFiles(dir: String) {
        logger.info("deleteAllCacheFiles $dir")
        File(dir).listFiles { e -> e.name.endsWith(SUFFIX) }?.forEach {
            val result = it.delete()
            logger.trace("delete $it $result")
        }
    }

    private fun getTempFileName(): String {
        logger.trace("getTempFileName")
        val uuid = UUID.randomUUID().toString()
        return "$uuid$SUFFIX"
    }

    private fun checkImageEntry(zipEntry: ZipEntry): Boolean {
        logger.trace("checkImageEntry")
        if (zipEntry.isDirectory) {
            return false
        }
        return ViewerType.fromPath(zipEntry.name) == ViewerType.IMAGE
    }
}