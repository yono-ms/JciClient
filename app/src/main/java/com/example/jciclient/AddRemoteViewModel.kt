package com.example.jciclient

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations

class AddRemoteViewModel : BaseViewModel() {
    val domainName by lazy { MutableLiveData<String>() }

    val domainNameError = Transformations.map(domainName) {
        if (it.isNullOrBlank()) R.string.error_domain_name else null
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

    init {
        fun updateCanCommit() {
            canCommit.value = domainName.value?.isNotBlank() == true
                    && accountName.value?.isNotBlank() == true
                    && accountPassword.value?.isNotBlank() == true
        }
        domainName.observeForever { updateCanCommit() }
        accountName.observeForever { updateCanCommit() }
        accountPassword.observeForever { updateCanCommit() }
    }
}