package com.example.jciclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.jciclient.databinding.ZipViewerFragmentBinding

class ZipViewerFragment : BaseFragment() {

    private val viewModel by viewModels<ZipViewerViewModel> {
        ZipViewerViewModel.Factory(
            args.remoteId,
            args.path
        )
    }

    private val args by navArgs<ZipViewerFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ZipViewerFragmentBinding.inflate(inflater).also {
            it.viewModel = viewModel
            it.lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.extract()
        }
    }
}