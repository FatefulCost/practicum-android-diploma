package ru.practicum.android.diploma.ui.filter.industry

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.databinding.ItemIndustryBinding

class IndustryAdapter(
    private val onItemSelected: (FilterIndustryDto?) -> Unit
) : RecyclerView.Adapter<IndustryAdapter.IndustryViewHolder>() {

    private var industries = listOf<FilterIndustryDto>()
    private var selectedId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndustryViewHolder {
        val binding = ItemIndustryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IndustryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IndustryViewHolder, position: Int) {
        holder.bind(industries[position], selectedId)
    }

    override fun getItemCount() = industries.size

    fun updateIndustries(newIndustries: List<FilterIndustryDto>) {
        industries = newIndustries
        notifyDataSetChanged()
    }

    fun selectItem(id: Int?) {
        val oldId = selectedId
        selectedId = id

        if (oldId != null) {
            val oldPos = industries.indexOfFirst { it.id == oldId }
            if (oldPos >= 0) notifyItemChanged(oldPos)
        }

        if (id != null) {
            val newPos = industries.indexOfFirst { it.id == id }
            if (newPos >= 0) notifyItemChanged(newPos)
        }

        onItemSelected(getSelectedItem())
    }

    fun getSelectedItem(): FilterIndustryDto? {
        return industries.find { it.id == selectedId }
    }

    inner class IndustryViewHolder(
        private val binding: ItemIndustryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(industry: FilterIndustryDto, selectedId: Int?) {
            binding.tvIndustryName.text = industry.name

            val isSelected = industry.id == selectedId
            binding.rbSelected.isChecked = isSelected

            binding.rbSelected.buttonTintList = ColorStateList.valueOf(
                ContextCompat.getColor(binding.root.context, R.color.blue)
            )

            val clickListener = {
                val newId = if (isSelected) null else industry.id
                selectItem(newId)
            }

            binding.root.setOnClickListener { clickListener() }
            binding.rbSelected.setOnClickListener { clickListener() }
        }
    }
}
