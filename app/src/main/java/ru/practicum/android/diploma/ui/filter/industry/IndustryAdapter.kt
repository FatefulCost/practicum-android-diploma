package ru.practicum.android.diploma.ui.filter.industry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.databinding.ItemIndustryBinding

class IndustryAdapter(
    private val onIndustryClick: (FilterIndustryDto) -> Unit,
    private val onSelectionChanged: () -> Unit // Добавляем callback для изменения выбора
) : ListAdapter<FilterIndustryDto, IndustryAdapter.IndustryViewHolder>(IndustryDiffCallback()) {

    private var selectedIndustryId: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndustryViewHolder {
        val binding = ItemIndustryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IndustryViewHolder(binding, onIndustryClick, onSelectionChanged)
    }

    override fun onBindViewHolder(holder: IndustryViewHolder, position: Int) {
        val industry = getItem(position)
        val isSelected = selectedIndustryId == industry.id
        holder.bind(industry, isSelected, selectedIndustryId)
    }

    /**
     * Устанавливает выбранную отрасль и обновляет список
     */
    fun setSelectedIndustry(industryId: Int?) {
        selectedIndustryId = industryId
        notifyDataSetChanged()
        onSelectionChanged() // Уведомляем о изменении выбора
    }

    /**
     * Возвращает выбранную отрасль
     */
    fun getSelectedIndustry(): FilterIndustryDto? {
        return if (selectedIndustryId != null) {
            currentList.find { it.id == selectedIndustryId }
        } else {
            null
        }
    }

    class IndustryViewHolder(
        private val binding: ItemIndustryBinding,
        private val onIndustryClick: (FilterIndustryDto) -> Unit,
        private val onSelectionChanged: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(industry: FilterIndustryDto, isSelected: Boolean, selectedIndustryId: Int?) {
            binding.tvIndustryName.text = industry.name

            // Меняем иконку в зависимости от состояния
            val iconRes = if (isSelected) {
                R.drawable.radio_button_on__24px
            } else {
                R.drawable.radio_button_off__24px
            }
            binding.ivCheck.setImageResource(iconRes)

            // Обработка клика
            binding.root.setOnClickListener {
                // Если нажали на уже выбранную отрасль, то снимаем выбор
                if (selectedIndustryId == industry.id) {
                    onIndustryClick(industry)
                } else {
                    onIndustryClick(industry)
                }
            }
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
