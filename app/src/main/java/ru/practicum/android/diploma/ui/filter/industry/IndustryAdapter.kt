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
    private val onItemClick: (FilterIndustryDto) -> Unit,
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
        return IndustryViewHolder(binding, onItemClick, onItemSelected, ::setSelectedId, ::getIndustries)
    }

    override fun onBindViewHolder(holder: IndustryViewHolder, position: Int) {
        holder.bind(industries[position], selectedId)
    }

    override fun getItemCount() = industries.size

    fun updateIndustries(newIndustries: List<FilterIndustryDto>) {
        industries = newIndustries
        notifyDataSetChanged()
    }

    fun getIndustries(): List<FilterIndustryDto> = industries

    fun setSelectedId(id: Int?) {
        val oldSelectedId = selectedId
        selectedId = id
        if (oldSelectedId != null) {
            val oldPos = industries.indexOfFirst { it.id == oldSelectedId }
            if (oldPos >= 0) notifyItemChanged(oldPos)
        }
        if (id != null) {
            val newPos = industries.indexOfFirst { it.id == id }
            if (newPos >= 0) notifyItemChanged(newPos)
        }
    }

    fun selectItem(id: Int?) {
        setSelectedId(id)
    }

    fun getSelectedItem(): FilterIndustryDto? {
        return if (selectedId != null) {
            industries.find { it.id == selectedId }
        } else {
            null
        }
    }

    fun toggleSelection(id: Int?) {
        selectedId = if (selectedId == id) null else id
        notifyDataSetChanged()
    }

    class IndustryViewHolder(
        private val binding: ItemIndustryBinding,
        private val onItemClick: (FilterIndustryDto) -> Unit,
        private val onItemSelected: (FilterIndustryDto?) -> Unit,
        private val setSelectedIdFunc: (Int?) -> Unit,
        private val getIndustries: () -> List<FilterIndustryDto>
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(industry: FilterIndustryDto, selectedId: Int?) {
            binding.tvIndustryName.text = industry.name
            val isSelected = industry.id == selectedId
            binding.rbSelected.isChecked = isSelected
            binding.rbSelected.buttonTintList = ColorStateList.valueOf(
                ContextCompat.getColor(binding.root.context, R.color.blue)
            )
            binding.root.setOnClickListener {
                val newId = if (selectedId == industry.id) null else industry.id
                setSelectedIdFunc(newId)
                onItemSelected(newId?.let { ind -> getIndustries().find { it.id == ind } })
            }
            binding.rbSelected.setOnClickListener {
                val newId = if (selectedId == industry.id) null else industry.id
                setSelectedIdFunc(newId)
                onItemSelected(newId?.let { ind -> getIndustries().find { it.id == ind } })
            }
        }
    }
}