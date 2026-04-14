package ru.practicum.android.diploma.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.database.VacancyEntity
import ru.practicum.android.diploma.databinding.ItemVacancyBinding
import ru.practicum.android.diploma.util.SalaryFormatter

class FavoritesAdapter(
    private val onItemClick: (VacancyEntity) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    private var vacancies = listOf<VacancyEntity>()

    fun updateVacancies(newVacancies: List<VacancyEntity>) {
        vacancies = newVacancies
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemVacancyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(vacancies[position])
    }

    override fun getItemCount() = vacancies.size

    class FavoriteViewHolder(
        private val binding: ItemVacancyBinding,
        private val onItemClick: (VacancyEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(vacancy: VacancyEntity) {
            val nameWithRegion = if (!vacancy.areaName.isNullOrBlank()) {
                "${vacancy.name}, ${vacancy.areaName}"
            } else {
                vacancy.name
            }
            binding.tvVacancyName.text = nameWithRegion
            binding.tvCompanyName.text = vacancy.employerName
            binding.tvSalary.text = SalaryFormatter.format(
                from = vacancy.salaryFrom,
                to = vacancy.salaryTo,
                currency = vacancy.salaryCurrency
            )

            val cornerRadiusPx = binding.ivLogo.context.resources
                .getDimensionPixelSize(R.dimen.logo_corner_radius)

            Glide.with(binding.ivLogo.context)
                .load(vacancy.employerLogo)
                .placeholder(R.drawable.placeholder_32px)
                .error(R.drawable.placeholder_32px)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(cornerRadiusPx)))
                .into(binding.ivLogo)

            binding.root.setOnClickListener { onItemClick(vacancy) }
        }
    }
}
