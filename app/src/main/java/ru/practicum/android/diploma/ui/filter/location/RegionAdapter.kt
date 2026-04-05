package ru.practicum.android.diploma.ui.filter.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.databinding.ItemRegionBinding

class RegionAdapter(
    private val onItemClick: (FilterAreaDto) -> Unit
) : RecyclerView.Adapter<RegionAdapter.RegionViewHolder>() {

    private var regions: List<FilterAreaDto> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegionViewHolder {
        val binding = ItemRegionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RegionViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: RegionViewHolder, position: Int) {
        holder.bind(regions[position])
    }

    override fun getItemCount(): Int = regions.size

    fun submitList(newRegions: List<FilterAreaDto>) {
        regions = newRegions
        notifyDataSetChanged()
    }

    class RegionViewHolder(
        private val binding: ItemRegionBinding,
        private val onItemClick: (FilterAreaDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(region: FilterAreaDto) {
            binding.tvRegionName.text = region.name
            binding.root.setOnClickListener { onItemClick(region) }
        }
    }
}
