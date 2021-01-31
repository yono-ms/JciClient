package com.example.jciclient

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel : BaseViewModel() {
    val items by lazy {
        logger.info("read items.")
        App.db.remoteDao().getAllLiveData()
    }

    fun deleteAllFiles() {
        viewModelScope.launch {
            App.db.fileDao().deleteAll()
        }
    }
}