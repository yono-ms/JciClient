package com.example.jciclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.jciclient.databinding.ZipViewerFragmentBinding
import com.example.jciclient.databinding.ZipViewerPageBinding

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
        return ZipViewerFragmentBinding.inflate(inflater).also { binding ->
            binding.viewModel = viewModel
            binding.lifecycleOwner = viewLifecycleOwner

            binding.viewPager.adapter = ZipViewerAdapter().also { adapter ->
                viewModel.items.observe(viewLifecycleOwner) {
                    logger.info("item changed.")
                    adapter.submitList(it)
                }
            }
            binding.viewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.index.value = position
                }
            })

            viewModel.throwable.observe(viewLifecycleOwner) {
                findNavController().navigate(
                    HomeFragmentDirections.actionGlobalMessageDialogFragment(
                        it.message.toString()
                    )
                )
            }
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.extract(requireContext().cacheDir.path)
        }
    }

    inner class ZipViewerAdapter :
        ListAdapter<ZipViewerViewModel.EntryInfo, ZipViewerAdapter.ViewHolder>(object :
            DiffUtil.ItemCallback<ZipViewerViewModel.EntryInfo>() {
            override fun areItemsTheSame(
                oldItem: ZipViewerViewModel.EntryInfo,
                newItem: ZipViewerViewModel.EntryInfo
            ): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(
                oldItem: ZipViewerViewModel.EntryInfo,
                newItem: ZipViewerViewModel.EntryInfo
            ): Boolean {
                return oldItem == newItem
            }
        }) {
        inner class ViewHolder(val binding: ZipViewerPageBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // ViewPager2 needs 3 arguments.
            return ViewHolder(
                ZipViewerPageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.entryInfo = getItem(position)
        }
    }
}