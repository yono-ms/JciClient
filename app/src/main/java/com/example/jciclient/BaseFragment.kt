package com.example.jciclient

import androidx.fragment.app.Fragment
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class BaseFragment : Fragment() {
    val logger: Logger by lazy { LoggerFactory.getLogger(javaClass.simpleName) }
}