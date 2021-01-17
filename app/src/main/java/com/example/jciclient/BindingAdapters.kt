package com.example.jciclient

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("app:errorText")
fun setErrorMessage(view: TextInputLayout, errorMessage: Int?) {
    view.error = errorMessage?.let { resId ->
        view.resources.getText(resId)
    }
}