package ru.practicum.android.diploma.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.dto.SalaryDto
import ru.practicum.android.diploma.data.dto.VacancyDetailDto

class VacancyAdapter(
    private var vacancies: List<VacancyDetailDto>,
    private val onItemClick: (VacancyDetailDto) -> Unit
) : RecyclerView.Adapter<VacancyAdapter.VacancyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacancyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vacancy, parent, false)
        return VacancyViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: VacancyViewHolder, position: Int) {
        holder.bind(vacancies[position])
    }

    override fun getItemCount(): Int = vacancies.size

    fun updateData(newVacancies: List<VacancyDetailDto>) {
        vacancies = newVacancies
        notifyDataSetChanged()
    }

    inner class VacancyViewHolder(
        itemView: View,
        private val onItemClick: (VacancyDetailDto) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivLogo: ImageView = itemView.findViewById(R.id.ivLogo)
        private val tvVacancyName: TextView = itemView.findViewById(R.id.tvVacancyName)
        private val tvVacancyRegion: TextView = itemView.findViewById(R.id.tvVacancyRegion)
        private val tvCompanyName: TextView = itemView.findViewById(R.id.tvCompanyName)
        private val tvSalary: TextView = itemView.findViewById(R.id.tvSalary)

        fun bind(vacancy: VacancyDetailDto) {
            // Заполнение названия вакансии
            tvVacancyName.text = vacancy.name

            // Заполнение региона: area.name из FilterAreaDto
            tvVacancyRegion.text = vacancy.area.name

            // Заполнение компании: employer.name из EmployerDto
            tvCompanyName.text = vacancy.employer.name

            // Форматирование зарплаты
            tvSalary.text = formatSalary(vacancy.salary)

            // Загрузка логотипа компании
            val logoUrl = vacancy.employer.logoUrls?.logo240
                ?: vacancy.employer.logoUrls?.logo90

            if (!logoUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(logoUrl)
                    .placeholder(R.drawable.ic_logo)
                    .error(R.drawable.ic_logo)
                    .into(ivLogo)
            } else {
                // Если URL логотипа нет, показываем placeholder
                Glide.with(itemView.context)
                    .load(R.drawable.ic_logo)
                    .into(ivLogo)
            }

            // Обработчик клика на элемент списка
            itemView.setOnClickListener {
                onItemClick(vacancy)
            }
        }

        private fun formatSalary(salary: SalaryDto?): String {
            return when {
                salary == null -> "Зарплата не указана"
                salary.from != null && salary.to != null -> {
                    "от ${formatNumber(salary.from)} до ${formatNumber(salary.to)} ${salary.currency ?: "₽"}"
                }
                salary.from != null -> {
                    "от ${formatNumber(salary.from)} ${salary.currency ?: "₽"}"
                }
                salary.to != null -> {
                    "до ${formatNumber(salary.to)} ${salary.currency ?: "₽"}"
                }
                else -> "Зарплата не указана"
            }
        }

        private fun formatNumber(number: Int): String {
            return number.toString()
                .reversed()
                .chunked(3)
                .joinToString(" ")
                .reversed()
        }
    }
}
