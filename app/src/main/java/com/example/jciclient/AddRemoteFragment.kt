package com.example.jciclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.jciclient.databinding.AddRemoteFragmentBinding

class AddRemoteFragment : BaseFragment() {

    private val viewModel by viewModels<AddRemoteViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return AddRemoteFragmentBinding.inflate(inflater).also { binding ->
            binding.viewModel = viewModel
            binding.lifecycleOwner = viewLifecycleOwner

            viewModel.checkOk.observe(viewLifecycleOwner) {
                it?.let {
                    findNavController().popBackStack()
                    viewModel.checkOk.value = null
                }
            }
        }.also { binding ->
            binding.button.setOnClickListener {
                viewModel.checkRemote()
            }
        }.root
    }
}