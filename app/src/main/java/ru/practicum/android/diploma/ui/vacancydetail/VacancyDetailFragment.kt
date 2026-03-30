package ru.practicum.android.diploma.ui.vacancydetail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.dto.AddressDto
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

    companion object {
        private const val LOG_TAG = "VacancyDetail"
        private const val LOADING_MESSAGE = "Загрузка..."
        private const val ERROR_LOADING_MESSAGE = "Не удалось загрузить"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vacancyId = arguments?.getString("vacancyId") ?: ""
        setupToolbar()
        setupObservers()
        loadVacancyDetails()
    }

    private fun loadVacancyDetails() {
        if (vacancyId.isNotEmpty()) {
            viewModel.loadVacancyDetails(vacancyId)
        } else {
            showError(getString(R.string.error_vacancy_not_found))
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
                is Resource.Loading -> Unit
                is Resource.Success -> resource.data?.let(::displayVacancyDetails)
                is Resource.Error -> showError(resource.message ?: getString(R.string.error_loading_data))
            }
        }

        viewModel.isFavorite.observe(viewLifecycleOwner, ::updateFavoriteIcon)
        viewModel.shareUrl.observe(viewLifecycleOwner) { url ->
            url?.let {
                shareVacancy(it)
                viewModel.onShareCompleted()
            }
        }
    }

    private fun displayVacancyDetails(vacancy: VacancyDetailDto) {
        Log.d(LOG_TAG, "=== DISPLAYING VACANCY ===")
        Log.d(LOG_TAG, "Name: ${vacancy.name}")
        Log.d(LOG_TAG, "Employer: ${vacancy.employer.name}")
        Log.d(LOG_TAG, "Logo URL: ${vacancy.employer.logoUrls?.logo240}")
        Log.d(LOG_TAG, "Contacts email: ${vacancy.contacts?.email}")
        Log.d(LOG_TAG, "Contacts phone: ${vacancy.contacts?.phone}")

        binding.tvVacancyTitle.text = vacancy.name
        binding.tvSalary.text = VacancyFormatter.formatSalary(vacancy.salary)
        displayCompanyInfo(vacancy)
        displayExperienceAndWorkFormat(vacancy)
        displayDescriptionSections(vacancy)
        displaySkills(vacancy)
        displayContacts(vacancy)
    }

    private fun displayCompanyInfo(vacancy: VacancyDetailDto) {
        binding.tvCompanyName.text = vacancy.employer.name ?: "Компания не указана"

        val location = buildLocationString(vacancy.address, vacancy.area?.name)
        binding.tvCompanyLocation.text = location ?: "Локация не указана"

        val logoUrl = vacancy.employer.logoUrls?.logo240 ?: vacancy.employer.logoUrls?.logo90
        Log.d("VacancyDetail", "Logo URL: $logoUrl")

        if (!logoUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(logoUrl)
                .placeholder(R.drawable.ic_logo)
                .error(R.drawable.ic_logo)
                .into(binding.ivCompanyLogo)
        } else {
            binding.ivCompanyLogo.setImageResource(R.drawable.ic_logo)
        }
    }

    private fun buildLocationString(address: AddressDto?, areaName: String?): String? {
        if (address == null && areaName.isNullOrEmpty()) return null

        val addressParts = buildAddressParts(address)
        val locationParts = mutableListOf<String>()

        areaName?.let { locationParts.add(it) }
        locationParts.addAll(addressParts)

        return locationParts.joinToString(", ").takeIfNotEmpty()
    }

    private fun buildAddressParts(address: AddressDto?): List<String> {
        val parts = mutableListOf<String>()
        address?.city?.let { parts.add(it) }
        address?.street?.let { parts.add(it) }
        address?.building?.let { parts.add("здание $it") }
        return parts
    }

    private fun String?.takeIfNotEmpty(): String? = if (this.isNullOrBlank()) null else this

    private fun displayExperienceAndWorkFormat(vacancy: VacancyDetailDto) {
        binding.tvExperience.text = vacancy.experience?.name ?: "Не указан"
        binding.tvWorkFormat.text = buildWorkFormatString(vacancy.employment?.name, vacancy.schedule?.name)
        binding.layoutExperience.visibility = View.VISIBLE
    }

    private fun buildWorkFormatString(employment: String?, schedule: String?): String {
        return listOfNotNull(employment, schedule)
            .joinToString(", ")
            .ifEmpty { "Не указан" }
    }

    private fun displayDescriptionSections(vacancy: VacancyDetailDto) {
        val sections = DescriptionParser.parseDescription(vacancy.description)
        val hasAnySection = sections.responsibilities != null ||
            sections.requirements != null ||
            sections.conditions != null

        binding.tvDescriptionTitle.visibility = if (hasAnySection) View.VISIBLE else View.GONE
        showSection(binding.layoutResponsibilities, sections.responsibilities, binding.tvResponsibilities)
        showSection(binding.layoutRequirements, sections.requirements, binding.tvRequirements)
        showSection(binding.layoutConditions, sections.conditions, binding.tvConditions)
    }

    private fun showSection(layout: View, text: String?, textView: android.widget.TextView) {
        if (!text.isNullOrBlank()) {
            layout.visibility = View.VISIBLE
            textView.text = text
        } else {
            layout.visibility = View.GONE
        }
    }

    private fun displaySkills(vacancy: VacancyDetailDto) {
        val skills = vacancy.skills
        if (skills != null && skills.isNotEmpty()) {
            binding.layoutSkillsTitle.visibility = View.VISIBLE
            binding.tvSkillsTitle.text = skills.joinToString("\n") { "• $it" }
        } else {
            binding.layoutSkillsTitle.visibility = View.GONE
        }
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        val icon = if (isFavorite) R.drawable.ic_favorites_filled else R.drawable.ic_favorites_outline
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

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun displayContacts(vacancy: VacancyDetailDto) {
        val contacts = vacancy.contacts
        if (contacts == null) {
            binding.layoutContacts.visibility = View.GONE
            return
        }

        val hasEmail = !contacts.email.isNullOrEmpty()
        val hasPhone = contacts.phone != null && contacts.phone.isNotEmpty()

        if (!hasEmail && !hasPhone) {
            binding.layoutContacts.visibility = View.GONE
            return
        }

        binding.layoutContacts.visibility = View.VISIBLE

        // Email
        if (hasEmail) {
            binding.layoutEmail.visibility = View.VISIBLE
            binding.tvEmail.text = contacts.email
            binding.tvEmail.setOnClickListener {
                openEmail(contacts.email!!)
            }
        } else {
            binding.layoutEmail.visibility = View.GONE
        }

        // Телефон
        if (hasPhone) {
            binding.layoutPhone.visibility = View.VISIBLE
            val firstPhone = contacts.phone!!.first()
            binding.tvPhone.text = firstPhone
            binding.tvPhone.setOnClickListener {
                openPhone(firstPhone)
            }

            // Комментарий к телефону (имя контакта)
            if (!contacts.name.isNullOrEmpty()) {
                binding.tvPhoneComment.text = contacts.name
                binding.tvPhoneComment.visibility = View.VISIBLE
            } else {
                binding.tvPhoneComment.visibility = View.GONE
            }
        } else {
            binding.layoutPhone.visibility = View.GONE
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
