package com.example.jciclient

import androidx.fragment.app.Fragment
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class BaseFragment : Fragment() {

    val logger: Logger by lazy { LoggerFactory.getLogger(javaClass.simpleName) }

//    fun hideBars() {
//        logger.info("hideBars")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            activity?.window?.setDecorFitsSystemWindows(false)
//            activity?.window?.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.systemBars())
//        } else {
//            @Suppress("DEPRECATION")
//            activity?.window?.decorView?.systemUiVisibility =
//                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//        }
//    }
//
//    fun showBars() {
//        logger.info("showBars")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            activity?.window?.setDecorFitsSystemWindows(true)
//            activity?.window?.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.systemBars())
//        }
//    }
}