package com.example.jciclient

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.example.jciclient.database.RemoteEntity
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AddRemoteViewModel : BaseViewModel() {
    val remoteName by lazy { MutableLiveData<String>() }

    val remoteNameError = Transformations.map(remoteName) {
        if (it.isNullOrBlank()) R.string.error_remote_name else null
    }

    val domainName by lazy { MutableLiveData<String>() }

    val domainNameError = Transformations.map(domainName) {
        if (it.isNullOrBlank()) R.string.error_domain_name else null
    }

    val shareName by lazy { MutableLiveData<String>() }

    val shareNameError = Transformations.map(shareName) {
        if (it.isNullOrBlank()) R.string.error_share_name else null
    }

    val accountName by lazy { MutableLiveData<String>() }

    val accountNameError = Transformations.map(accountName) {
        if (it.isNullOrBlank()) R.string.error_account_name else null
    }

    val accountPassword by lazy { MutableLiveData<String>() }

    val accountPasswordError = Transformations.map(accountPassword) {
        if (it.isNullOrBlank()) R.string.error_account_password else null
    }

    val canCommit by lazy { MutableLiveData(false) }

    val checkOk by lazy { MutableLiveData<Unit>() }

    init {
        fun updateCanCommit() {
            canCommit.value = remoteName.value?.isNotBlank() == true
                    && domainName.value?.isNotBlank() == true
                    && shareName.value?.isNotBlank() == true
                    && accountName.value?.isNotBlank() == true
                    && accountPassword.value?.isNotBlank() == true
        }
        remoteName.observeForever { updateCanCommit() }
        domainName.observeForever { updateCanCommit() }
        shareName.observeForever { updateCanCommit() }
        accountName.observeForever { updateCanCommit() }
        accountPassword.observeForever { updateCanCommit() }
    }

    fun checkRemote() {
        logger.info("checkRemote ${domainName.value} ${accountName.value} ${accountPassword.value}")
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val credentials = NtlmPasswordAuthenticator(
                    domainName.value,
                    accountName.value,
                    accountPassword.value
                )
                val baseContext = BaseContext(PropertyConfiguration(Properties().apply {
                    setProperty("jcifs.smb.client.minVersion", "SMB202")
                    setProperty("jcifs.smb.client.maxVersion", "SMB300")
                }))
                val cifsContext = baseContext.withCredentials(credentials)
                val smbFile = SmbFile("smb://${domainName.value}/${shareName.value}/", cifsContext)
                val list = smbFile.list()
                smbFile.close()
                list.forEach {
                    logger.debug(it)
                }
                App.db.remoteDao().insertAll(
                    RemoteEntity(
                        0,
                        remoteName.value ?: throw IllegalArgumentException("remoteName"),
                        domainName.value ?: throw IllegalArgumentException("domainName"),
                        shareName.value ?: throw IllegalArgumentException("shareName"),
                        accountName.value ?: throw IllegalArgumentException("accountName"),
                        accountPassword.value ?: throw IllegalArgumentException("accountPassword"),
                    )
                )
            }.onSuccess {
                checkOk.postValue(Unit)
            }.onFailure {
                logger.error("checkRemote", it)
                throwable.postValue(it)
            }
        }
    }
}