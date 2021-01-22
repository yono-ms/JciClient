package com.example.jciclient

import androidx.lifecycle.viewModelScope
import com.example.jciclient.database.RemoteEntity
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel : BaseViewModel() {
    val items by lazy {
        logger.info("read items.")
        App.db.remoteDao().getAllLiveData()
    }

    fun checkRemote(entity: RemoteEntity) {
        logger.info("checkRemote ${entity.domainName} ${entity.shareName} ${entity.accountName} ${entity.accountPassword}")
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
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
                val smbFile = SmbFile("smb://${entity.domainName}/${entity.shareName}", cifsContext)
                val list = smbFile.list()
                smbFile.close()
                list.forEach {
                    logger.debug(it)
                }
            }.onFailure {
                logger.error("checkRemote", it)
                throwable.postValue(it)
            }
        }
    }
}