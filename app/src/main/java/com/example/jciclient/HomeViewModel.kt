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
}