package com.example.jciclient

class HomeViewModel : BaseViewModel() {
    val items by lazy {
        logger.info("read items.")
        App.db.remoteDao().getAllLiveData()
    }
}