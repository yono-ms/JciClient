package com.example.jciclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jciclient.database.RemoteEntity
import com.example.jciclient.databinding.HomeFragmentBinding
import com.example.jciclient.databinding.RemoteItemBinding

class HomeFragment : BaseFragment() {

    enum class RequestKey {
        DELETE_ALL,
    }

    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return HomeFragmentBinding.inflate(inflater).also { binding ->
            binding.viewModel = viewModel
            binding.lifecycleOwner = viewLifecycleOwner

            binding.recyclerView.layoutManager = LinearLayoutManager(context)
            binding.recyclerView.adapter = RemoteAdapter({ entity ->
                logger.info("onClick $entity")
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToFolderFragment(
                        entity.id,
                        "smb://${entity.domainName}/${entity.shareName}/"
                    )
                )
            }, { entity ->
                logger.info("onLongClick $entity")
                findNavController().navigate(
                    HomeFragmentDirections.actionGlobalYesNoDialogFragment(
                        getString(R.string.dialog_message_delete_cache),
                        getString(R.string.button_ok),
                        getString(R.string.button_cancel),
                        RequestKey.DELETE_ALL.name
                    )
                )
            }).also { adapter ->
                viewModel.items.observe(viewLifecycleOwner) { items ->
                    logger.info("items.changed.")
                    adapter.submitList(items)
                }
            }

            binding.floatingActionButton.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddRemoteFragment())
            }

            viewModel.throwable.observe(viewLifecycleOwner) {
                findNavController().navigate(
                    HomeFragmentDirections.actionGlobalMessageDialogFragment(
                        it.message.toString()
                    )
                )
            }
        }.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(RequestKey.DELETE_ALL.name) { requestKey, bundle ->
            logger.info("$requestKey $bundle")
            viewModel.deleteAllFiles()
        }
    }

    class RemoteAdapter(
        private val onClick: (entity: RemoteEntity) -> Unit,
        private val onLongClick: (entity: RemoteEntity) -> Unit
    ) : ListAdapter<RemoteEntity, RemoteAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<RemoteEntity>() {
            override fun areItemsTheSame(
                oldItem: RemoteEntity,
                newItem: RemoteEntity
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: RemoteEntity,
                newItem: RemoteEntity
            ): Boolean {
                return oldItem == newItem
            }

        }
    ) {
        class ViewHolder(val binding: RemoteItemBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                RemoteItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ).apply {
                    cardView.setOnClickListener {
                        viewModel?.let(onClick)
                    }
                    cardView.setOnLongClickListener {
                        viewModel?.let(onLongClick)
                        true
                    }
                }
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.viewModel = getItem(position)
        }
    }
}