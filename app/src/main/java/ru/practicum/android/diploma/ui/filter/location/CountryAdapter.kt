package ru.practicum.android.diploma.ui.filter.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.databinding.ItemCountryBinding

class CountryAdapter(
    private val onCountryClick: (FilterAreaDto) -> Unit
) : ListAdapter<FilterAreaDto, CountryAdapter.CountryViewHolder>(CountryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val binding = ItemCountryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CountryViewHolder(binding, onCountryClick)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CountryViewHolder(
        private val binding: ItemCountryBinding,
        private val onCountryClick: (FilterAreaDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(country: FilterAreaDto) {
            binding.tvCountryName.text = country.name
            binding.root.setOnClickListener { onCountryClick(country) }
        }
    }

    private class CountryDiffCallback : DiffUtil.ItemCallback<FilterAreaDto>() {
        override fun areItemsTheSame(oldItem: FilterAreaDto, newItem: FilterAreaDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FilterAreaDto, newItem: FilterAreaDto): Boolean {
            return oldItem == newItem
        }
    }
}
