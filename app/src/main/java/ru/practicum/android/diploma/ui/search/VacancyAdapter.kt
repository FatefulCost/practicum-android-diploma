package ru.practicum.android.diploma.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.databinding.ItemVacancyBinding
import ru.practicum.android.diploma.util.SalaryFormatter

/**
 * Адаптер для отображения списка вакансий
 */
class VacancyAdapter(
    private val onItemClick: (VacancyDetailDto) -> Unit
) : RecyclerView.Adapter<VacancyAdapter.VacancyViewHolder>() {

    private var vacancies = listOf<VacancyDetailDto>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacancyViewHolder {
        val binding = ItemVacancyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VacancyViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: VacancyViewHolder, position: Int) {
        holder.bind(vacancies[position])
    }

    override fun getItemCount() = vacancies.size

    /**
     * Обновить список вакансий
     */
    fun updateVacancies(newVacancies: List<VacancyDetailDto>) {
        vacancies = newVacancies
        notifyDataSetChanged()
    }

    /**
     * Добавить вакансии в конец списка (для пагинации)
     */
    fun addVacancies(newVacancies: List<VacancyDetailDto>) {
        val startPosition = vacancies.size
        vacancies = vacancies + newVacancies
        notifyItemRangeInserted(startPosition, newVacancies.size)
    }

    class VacancyViewHolder(
        private val binding: ItemVacancyBinding,
        private val onItemClick: (VacancyDetailDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(vacancy: VacancyDetailDto) {
            binding.tvVacancyName.text = vacancy.name
            binding.tvCompanyName.text = vacancy.employer.name
            binding.tvSalary.text = SalaryFormatter.format(vacancy.salary)
            binding.tvVacancyRegion.text = vacancy.area.name

            // Загрузка логотипа
            Glide.with(binding.ivLogo.context)
                .load(vacancy.employer.logo)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(binding.ivLogo)

            // Обработка клика
            binding.root.setOnClickListener {
                onItemClick(vacancy)
            }
        }
    }
}
