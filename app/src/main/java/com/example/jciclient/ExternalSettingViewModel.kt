package com.example.jciclient

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.example.jciclient.database.ExternalEntity
import kotlinx.coroutines.launch

class ExternalSettingViewModel : BaseViewModel() {
    val items by lazy { App.db.externalDao().getLiveData() }
    val ext by lazy { MutableLiveData<String>() }
    val buttonEnable = Transformations.map(ext) {
        it.isNotBlank()
    }

    fun addExt() {
        logger.info("addExt ${ext.value}")
        viewModelScope.launch {
            ext.value?.let {
                App.db.externalDao().insert(ExternalEntity(it))
                ext.value = ""
            }
        }
    }

    fun deleteExt(entity: ExternalEntity) {
        logger.info("deleteExt $entity")
        viewModelScope.launch {
            ext.value?.let {
                App.db.externalDao().delete(entity)
            }
        }
    }
}