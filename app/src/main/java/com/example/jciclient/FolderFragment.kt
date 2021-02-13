package com.example.jciclient

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import com.example.jciclient.database.FileEntity
import com.example.jciclient.databinding.FileItemBinding
import com.example.jciclient.databinding.FolderFragmentBinding

class FolderFragment : BaseFragment() {

    private val args by navArgs<FolderFragmentArgs>()

    private val viewModel by viewModels<FolderViewModel> {
        FolderViewModel.Factory(
            args.remoteId,
            args.path
        )
    }

    private val startExternalViewer = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        logger.info("startExternalViewer resultCode=${it.resultCode}")
        requireContext().unbindService(serviceConnection)
    }

    private val onStartWebServer: () -> Unit = {
        Intent(Intent.ACTION_VIEW, Uri.parse(binder.uriString)).also {
            startExternalViewer.launch(it)
        }
    }

    lateinit var binder: BridgeService.BridgeBinder

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            logger.info("onServiceConnected $name")
            binder = service as BridgeService.BridgeBinder
            binder.onStartWebServer = onStartWebServer
            binder.startWebServer()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            logger.info("onServiceConnected $name")
        }
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
                    when (ViewerType.fromPath(item.name)) {
                        ViewerType.IMAGE -> {
                            findNavController().navigate(
                                FolderFragmentDirections.actionFolderFragmentToImageViewerFragment(
                                    args.remoteId,
                                    item.path
                                )
                            )
                        }
                        ViewerType.VIDEO -> {
                            findNavController().navigate(
                                FolderFragmentDirections.actionFolderFragmentToVideoViewerFragment(
                                    args.remoteId,
                                    item.path
                                )
                            )
                        }
                        ViewerType.EXTERNAL -> {
                            Intent(context, BridgeService::class.java).apply {
                                putExtra(BridgeService.Key.REMOTE_ID.name, args.remoteId)
                                putExtra(BridgeService.Key.PATH.name, item.path)
                            }.also { intent ->
                                requireContext().bindService(
                                    intent,
                                    serviceConnection,
                                    Context.BIND_AUTO_CREATE
                                )
                            }
                        }
                        else -> {
                            logger.warn("no action.")
                        }
                    }
                }
            }.also { adapter ->
                viewModel.items.observe(viewLifecycleOwner) { items ->
                    adapter.submitList(items)
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