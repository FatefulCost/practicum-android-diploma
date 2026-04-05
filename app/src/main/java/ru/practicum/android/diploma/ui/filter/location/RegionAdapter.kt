package ru.practicum.android.diploma.ui.filter.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.databinding.ItemCountryBinding

class RegionAdapter(
    private val onRegionClick: (FilterAreaDto) -> Unit
) : ListAdapter<FilterAreaDto, RegionAdapter.RegionViewHolder>(RegionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegionViewHolder {
        val binding = ItemCountryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RegionViewHolder(binding, onRegionClick)
    }

    override fun onBindViewHolder(holder: RegionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RegionViewHolder(
        private val binding: ItemCountryBinding,
        private val onRegionClick: (FilterAreaDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(region: FilterAreaDto) {
            binding.tvCountryName.text = region.name
            binding.root.setOnClickListener { onRegionClick(region) }
        }
    }

    private class RegionDiffCallback : DiffUtil.ItemCallback<FilterAreaDto>() {
        override fun areItemsTheSame(oldItem: FilterAreaDto, newItem: FilterAreaDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FilterAreaDto, newItem: FilterAreaDto): Boolean {
            return oldItem == newItem
        }
    }
}
