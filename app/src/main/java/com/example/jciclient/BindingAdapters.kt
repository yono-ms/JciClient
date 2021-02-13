package com.example.jciclient

import android.view.View
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

@BindingAdapter("android:visibility")
fun setVisibility(view: View, visible: Boolean?) {
    visible?.let {
        view.visibility = if (it) View.VISIBLE else View.INVISIBLE
    }
}