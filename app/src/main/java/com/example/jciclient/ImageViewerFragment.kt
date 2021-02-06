package com.example.jciclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.jciclient.databinding.ImageViewerFragmentBinding

class ImageViewerFragment : BaseFragment() {

    val viewModel by viewModels<ImageViewerViewModel> {
        ImageViewerViewModel.Factory(
            args.remoteId,
            args.path
        )
    }

    private val args by navArgs<ImageViewerFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ImageViewerFragmentBinding.inflate(inflater).also {
            it.viewModel = viewModel
            it.lifecycleOwner = viewLifecycleOwner

            viewModel.downloadFile(requireContext().cacheDir.path)
        }.root
    }

    override fun onResume() {
        super.onResume()
        val words = args.path.split("/")
        val title = words.lastOrNull()
        (activity as? AppCompatActivity)?.supportActionBar?.title = title
    }
}