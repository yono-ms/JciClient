package com.example.jciclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jciclient.database.ExternalEntity
import com.example.jciclient.databinding.ExternalItemBinding
import com.example.jciclient.databinding.ExternalSettingFragmentBinding

class ExternalSettingFragment : BaseFragment() {

    private val viewModel by viewModels<ExternalSettingViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ExternalSettingFragmentBinding.inflate(inflater).also { binding ->
            binding.viewModel = viewModel
            binding.lifecycleOwner = viewLifecycleOwner

            binding.recyclerView.layoutManager = LinearLayoutManager(context)
            binding.recyclerView.adapter = ExternalAdapter().also { adapter ->
                viewModel.items.observe(viewLifecycleOwner) { items ->
                    logger.info("items changed ${items.size}")
                    adapter.submitList(items)
                }
            }

            binding.buttonAdd.setOnClickListener {
                viewModel.addExt()
            }
        }.root
    }

    inner class ExternalAdapter : ListAdapter<ExternalEntity, ExternalAdapter.ViewHolder>(object :
        DiffUtil.ItemCallback<ExternalEntity>() {
        override fun areItemsTheSame(oldItem: ExternalEntity, newItem: ExternalEntity): Boolean {
            return oldItem.ext == newItem.ext
        }

        override fun areContentsTheSame(oldItem: ExternalEntity, newItem: ExternalEntity): Boolean {
            return oldItem.ext == newItem.ext
        }
    }) {
        inner class ViewHolder(val binding: ExternalItemBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ExternalItemBinding.inflate(LayoutInflater.from(parent.context)).also { binding ->
                    binding.constraintLayout.setOnLongClickListener {
                        logger.info("long click ${binding.viewModel?.ext}")
                        binding.viewModel?.let {
                            viewModel.deleteExt(it)
                        }
                        true
                    }
                })
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.viewModel = getItem(position)
        }
    }
}