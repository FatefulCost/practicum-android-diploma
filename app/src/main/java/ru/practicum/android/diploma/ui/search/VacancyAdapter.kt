package ru.practicum.android.diploma.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.dto.VacancyDetailDto

class VacancyAdapter(
    private val vacancies: List<VacancyDetailDto>,
    private val onItemClick: (VacancyDetailDto) -> Unit
) : RecyclerView.Adapter<VacancyAdapter.VacancyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacancyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vacancy, parent, false)
        return VacancyViewHolder(view)
    }

    override fun onBindViewHolder(holder: VacancyViewHolder, position: Int) {
        holder.bind(vacancies[position])
    }

    override fun getItemCount(): Int = vacancies.size

    inner class VacancyViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val ivLogo: ImageView = itemView.findViewById(R.id.ivLogo)
        private val tvVacancyName: TextView = itemView.findViewById(R.id.tvVacancyName)
        private val tvVacancyRegion: TextView = itemView.findViewById(R.id.tvVacancyRegion)
        private val tvCompanyName: TextView = itemView.findViewById(R.id.tvCompanyName)
        private val tvSalary: TextView = itemView.findViewById(R.id.tvSalary)

        fun bind(vacancy: VacancyDetailDto) {
            // Название вакансии
            tvVacancyName.text = vacancy.name

            // Регион
            tvVacancyRegion.text = vacancy.area?.name ?: ""

            // Название компании
            tvCompanyName.text = vacancy.employer.name ?: ""

            // Зарплата
            tvSalary.text = formatSalary(vacancy.salary)

            // Логотип компании
            val logoUrls = vacancy.employer.logoUrls
            val logoUrl = logoUrls?.logo240 ?: logoUrls?.logo90

            Glide.with(itemView.context)
                .load(logoUrl)
                .placeholder(R.drawable.ic_logo)
                .error(R.drawable.ic_logo)
                .into(ivLogo)

            // Обработчик клика
            itemView.setOnClickListener {
                onItemClick(vacancy)
            }
        }

        private fun formatSalary(salary: ru.practicum.android.diploma.data.dto.SalaryDto?): String {
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
