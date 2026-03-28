package ru.practicum.android.diploma.ui.vacancy_detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.dto.SalaryDto
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.databinding.FragmentVacancyDetailBinding
import ru.practicum.android.diploma.util.Resource

class VacancyDetailFragment : Fragment() {

    private var _binding: FragmentVacancyDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VacancyDetailViewModel by viewModel()

    private var vacancyId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVacancyDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            vacancyId = it.getString("vacancyId", "")
        }

        setupToolbar()
        setupObservers()

        if (vacancyId.isNotEmpty()) {
            viewModel.loadVacancyDetails(vacancyId)
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_vacancy_not_found), Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_favorite -> {
                    viewModel.toggleFavorite(vacancyId)
                    true
                }
                R.id.action_share -> {
                    viewModel.shareVacancy()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupObservers() {
        viewModel.vacancyDetails.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    resource.data?.let { vacancy ->
                        displayVacancyDetails(vacancy)
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), resource.message ?: getString(R.string.error_loading_data), Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            updateFavoriteIcon(isFavorite)
        }

        viewModel.shareUrl.observe(viewLifecycleOwner) { url ->
            url?.let {
                shareVacancy(it)
                viewModel.onShareCompleted()
            }
        }
    }

    private fun displayVacancyDetails(vacancy: VacancyDetailDto) {

        binding.tvVacancyTitle.text = vacancy.name

        binding.tvSalary.text = formatSalary(vacancy.salary)

        displayCompanyInfo(vacancy)

        displayExperienceAndWorkFormat(vacancy)

        displayDescriptionSections(vacancy)

        displaySkills(vacancy)
    }

    private fun displayCompanyInfo(vacancy: VacancyDetailDto) {

        val companyName = vacancy.employer.name ?: "Компания не указана"
        binding.tvCompanyName.text = companyName

        val address = vacancy.address
        val area = vacancy.area

        val addressText = buildString {
            if (!address?.city.isNullOrEmpty()) {
                append(address?.city)
            }
            if (!address?.street.isNullOrEmpty()) {
                if (isNotEmpty()) append(", ")
                append(address?.street)
            }
            if (!address?.building.isNullOrEmpty()) {
                if (isNotEmpty()) append(", ")
                append(address?.building)
            }
        }

        val fullAddressText = if (!address?.fullAddress.isNullOrEmpty()) {
            address?.fullAddress
        } else if (addressText.isNotEmpty()) {
            addressText
        } else {
            null
        }

        val location = fullAddressText ?: area?.name
        binding.tvCompanyLocation.text = location ?: "Локация не указана"

        val logoUrls = vacancy.employer.logoUrls
        val logoUrl = logoUrls?.logo240 ?: logoUrls?.logo90

        Glide.with(this)
            .load(logoUrl)
            .placeholder(R.drawable.ic_logo)
            .error(R.drawable.ic_logo)
            .into(binding.ivCompanyLogo)
    }

    private fun displayExperienceAndWorkFormat(vacancy: VacancyDetailDto) {
        val experience = vacancy.experience?.name
        val employment = vacancy.employment?.name
        val schedule = vacancy.schedule?.name

        val workFormatText = buildString {
            if (!employment.isNullOrEmpty()) {
                append(employment)
            }
            if (!schedule.isNullOrEmpty()) {
                if (isNotEmpty()) append(", ")
                append(schedule)
            }
        }

        if (!experience.isNullOrEmpty()) {
            binding.tvExperience.text = experience
        } else {
            binding.tvExperience.text = "Не указан"
        }

        if (workFormatText.isNotEmpty()) {
            binding.tvWorkFormat.text = workFormatText
        } else {
            binding.tvWorkFormat.text = "Не указан"
        }

        binding.layoutExperience.visibility = View.VISIBLE
    }

    private fun displayDescriptionSections(vacancy: VacancyDetailDto) {
        val description = vacancy.description ?: ""

        val responsibilities = extractSection(description, "Обязанности", "Требования")
        val requirements = extractSection(description, "Требования", "Условия")
        val conditions = extractSection(description, "Условия", null)

        val hasAnySection = !responsibilities.isNullOrBlank() ||
            !requirements.isNullOrBlank() ||
            !conditions.isNullOrBlank()

        binding.tvDescriptionTitle.visibility = if (hasAnySection) View.VISIBLE else View.GONE

        if (!responsibilities.isNullOrBlank()) {
            binding.layoutResponsibilities.visibility = View.VISIBLE
            binding.tvResponsibilities.text = responsibilities
        } else {
            binding.layoutResponsibilities.visibility = View.GONE
        }

        if (!requirements.isNullOrBlank()) {
            binding.layoutRequirements.visibility = View.VISIBLE
            binding.tvRequirements.text = requirements
        } else {
            binding.layoutRequirements.visibility = View.GONE
        }

        if (!conditions.isNullOrBlank()) {
            binding.layoutConditions.visibility = View.VISIBLE
            binding.tvConditions.text = conditions
        } else {
            binding.layoutConditions.visibility = View.GONE
        }
    }

    private fun extractSection(text: String, startMarker: String, endMarker: String?): String? {
        val startIndex = text.indexOf(startMarker)
        if (startIndex == -1) return null

        val contentStart = startIndex + startMarker.length
        val endIndex = if (endMarker != null) {
            text.indexOf(endMarker, contentStart)
        } else {
            text.length
        }

        if (endIndex == -1) return null

        return text.substring(contentStart, endIndex).trim()
    }

    private fun displaySkills(vacancy: VacancyDetailDto) {
        val skills = vacancy.skills

        if (skills != null && skills.isNotEmpty()) {
            binding.layoutSkillsTitle.visibility = View.VISIBLE
            val skillsText = skills.joinToString("\n") { "• $it" }
            binding.tvSkillsTitle.text = skillsText
        } else {
            binding.layoutSkillsTitle.visibility = View.GONE
        }
    }

    private fun formatSalary(salary: SalaryDto?): String {
        return when {
            salary == null -> getString(R.string.salary_not_specified)
            salary.from != null && salary.to != null -> {
                "от ${formatNumber(salary.from)} до ${formatNumber(salary.to)} ${salary.currency ?: "₽"}"
            }
            salary.from != null -> {
                "от ${formatNumber(salary.from)} ${salary.currency ?: "₽"}"
            }
            salary.to != null -> {
                "до ${formatNumber(salary.to)} ${salary.currency ?: "₽"}"
            }
            else -> getString(R.string.salary_not_specified)
        }
    }

    private fun formatNumber(number: Int): String {
        return number.toString()
            .reversed()
            .chunked(3)
            .joinToString(" ")
            .reversed()
    }

    private fun openEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        startActivity(Intent.createChooser(intent, "Отправить письмо"))
    }

    private fun openPhone(phone: String) {
        val cleanedPhone = phone.replace(Regex("[^\\d+]"), "")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanedPhone")
        }
        startActivity(Intent.createChooser(intent, "Выберите приложение для звонка"))
    }

    private fun showLoading(show: Boolean) {
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        val icon = if (isFavorite) {
            R.drawable.ic_favorites_filled
        } else {
            R.drawable.ic_favorites_outline
        }
        binding.toolbar.menu.findItem(R.id.action_favorite).icon =
            resources.getDrawable(icon, requireContext().theme)
    }

    private fun shareVacancy(url: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
