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
import com.example.jciclient.database.FileEntity
import com.example.jciclient.databinding.FileItemBinding
import com.example.jciclient.databinding.FolderFragmentBinding
import java.io.File

class FolderFragment : BaseFragment() {

    private val args by navArgs<FolderFragmentArgs>()

    private val viewModel by viewModels<FolderViewModel> {
        FolderViewModel.Factory(
            args.remoteId,
            args.path
        )
    }

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
                            "video/mp4",
                            "image/jpeg" -> {
                                viewModel.downloadFile(
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
                it?.let { filePath ->
                    logger.info("filePath=$filePath")
                    val ext = File(filePath).extension
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)?.let { mimeType ->
                        logger.info("mimeType=$mimeType")
                        when (mimeType) {
                            "image/jpeg" -> {
                                findNavController().navigate(
                                    FolderFragmentDirections.actionFolderFragmentToImageViewerFragment(
                                        filePath
                                    )
                                )
                            }
                            "video/mp4" -> {
                                findNavController().navigate(
                                    FolderFragmentDirections.actionFolderFragmentToVideoViewerFragment(
                                        args.remoteId,
                                        filePath
                                    )
                                )
                            }
                            else -> {
                                logger.error("no action.")
                            }
                        }
                    }
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
        if (savedInstanceState == null) {
            viewModel.getFiles()
        }
    }

    override fun onResume() {
        super.onResume()
        val words = args.path.split("/")
        val index = words.size - 2
        val title = words.getOrNull(index)
        (activity as? AppCompatActivity)?.supportActionBar?.title = title
    }

    class FileAdapter(private val onClick: (item: FileEntity) -> Unit) :
        ListAdapter<FileEntity, FileAdapter.ViewHolder>(object :
            DiffUtil.ItemCallback<FileEntity>() {
            override fun areItemsTheSame(
                oldItem: FileEntity,
                newItem: FileEntity
            ): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(
                oldItem: FileEntity,
                newItem: FileEntity
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