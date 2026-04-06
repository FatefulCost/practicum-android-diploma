package ru.practicum.android.diploma.ui.filter.industry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.databinding.ItemIndustryBinding

class IndustryAdapter(
    private val onIndustryClick: (FilterIndustryDto) -> Unit
) : ListAdapter<FilterIndustryDto, IndustryAdapter.IndustryViewHolder>(IndustryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndustryViewHolder {
        val binding = ItemIndustryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IndustryViewHolder(binding, onIndustryClick)
    }

    override fun onBindViewHolder(holder: IndustryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class IndustryViewHolder(
        private val binding: ItemIndustryBinding,
        private val onIndustryClick: (FilterIndustryDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(industry: FilterIndustryDto) {
            binding.tvIndustryName.text = industry.name
            binding.root.setOnClickListener { onIndustryClick(industry) }
        }
    }

    private class IndustryDiffCallback : DiffUtil.ItemCallback<FilterIndustryDto>() {
        override fun areItemsTheSame(oldItem: FilterIndustryDto, newItem: FilterIndustryDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FilterIndustryDto, newItem: FilterIndustryDto): Boolean {
            return oldItem == newItem
        }
    }
}
