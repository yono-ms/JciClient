package com.example.jciclient

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("app:errorText")
fun setErrorMessage(view: TextInputLayout, errorMessage: Int?) {
    view.error = errorMessage?.let { resId ->
        view.resources.getText(resId)
    }
}

@BindingAdapter("app:srcCompat")
fun setSrcCompat(view: ImageView, src: Int?) {
    src?.let {
        view.setImageResource(it)
    }
}