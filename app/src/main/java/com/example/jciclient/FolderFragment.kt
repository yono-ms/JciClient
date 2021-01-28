package com.example.jciclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import com.example.jciclient.databinding.FileItemBinding
import com.example.jciclient.databinding.FolderFragmentBinding
import java.io.File

class FolderFragment : BaseFragment() {

    private val viewModel by viewModels<FolderViewModel>()

    private val args by navArgs<FolderFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FolderFragmentBinding.inflate(inflater).also { binding ->
            binding.viewModel = viewModel
            binding.lifecycleOwner = viewLifecycleOwner

            binding.recyclerView.addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
            binding.recyclerView.layoutManager = LinearLayoutManager(context)
            binding.recyclerView.adapter = FileAdapter { item ->
                logger.info("onClick $item")
                if (item.directory) {
                    findNavController().navigate(
                        FolderFragmentDirections.actionFolderFragmentSelf(
                            args.remoteId,
                            item.path
                        )
                    )
                } else {
                    logger.info("is not folder. ${item.contentType}")
                    val ext = File(item.name).extension
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)?.let { mimeType ->
                        logger.info("mimeType=$mimeType")
                        when (mimeType) {
                            "application/zip",
                            "image/jpeg" -> {
                                viewModel.downloadFile(
                                    args.remoteId,
                                    item.path,
                                    requireContext().cacheDir.path
                                )
                            }
                            else -> {
                                logger.info("no action.")
                            }
                        }
                    }
                }
            }.also { adapter ->
                viewModel.items.observe(viewLifecycleOwner) { items ->
                    adapter.submitList(items)
                }
            }

            viewModel.filePath.observe(viewLifecycleOwner) {
                it?.let {
                    logger.info("filePath=$it")
                    findNavController().navigate(
                        FolderFragmentDirections.actionFolderFragmentToImageViewerFragment(
                            it
                        )
                    )
                    viewModel.filePath.value = null
                }
            }

            viewModel.throwable.observe(viewLifecycleOwner) {
                findNavController().navigate(
                    HomeFragmentDirections.actionGlobalMessageDialogFragment(
                        it.message.toString()
                    )
                )
            }

            viewModel.progress.observe(viewLifecycleOwner) {
                binding.progressBar.isVisible = it
            }
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getFiles(args.remoteId, args.path)
    }

    override fun onResume() {
        super.onResume()
        val words = args.path.split("/")
        val index = words.size - 2
        val title = words.getOrNull(index)
        (activity as? AppCompatActivity)?.supportActionBar?.title = title
    }

    class FileAdapter(private val onClick: (item: FolderViewModel.FileItem) -> Unit) :
        ListAdapter<FolderViewModel.FileItem, FileAdapter.ViewHolder>(object :
            DiffUtil.ItemCallback<FolderViewModel.FileItem>() {
            override fun areItemsTheSame(
                oldItem: FolderViewModel.FileItem,
                newItem: FolderViewModel.FileItem
            ): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(
                oldItem: FolderViewModel.FileItem,
                newItem: FolderViewModel.FileItem
            ): Boolean {
                return oldItem == newItem
            }
        }) {
        class ViewHolder(val binding: FileItemBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(FileItemBinding.inflate(LayoutInflater.from(parent.context))).also { holder ->
                holder.itemView.setOnClickListener {
                    holder.binding.viewModel?.let {
                        onClick(it)
                    }
                }
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.viewModel = getItem(position)
        }
    }
}