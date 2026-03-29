package ru.practicum.android.diploma.ui.vacancy_detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.dto.ContactsDto
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.databinding.FragmentVacancyDetailBinding
import ru.practicum.android.diploma.ui.detail.VacancyDetailErrorType
import ru.practicum.android.diploma.ui.detail.VacancyDetailState
import ru.practicum.android.diploma.ui.detail.VacancyDetailViewModel
import ru.practicum.android.diploma.ui.detail.NavigationEvent

class VacancyDetailFragment : Fragment() {

    private var _binding: FragmentVacancyDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VacancyDetailViewModel by viewModel()

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

        setupToolbar()
        observeViewModel()

        val vacancyId = arguments?.getString("vacancyId") ?: ""
        if (vacancyId.isNotEmpty()) {
            viewModel.loadVacancy(vacancyId)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.action_share -> {
                    viewModel.openShareDialog()
                    true
                }
                R.id.action_favorite -> {
                    viewModel.toggleFavorite()
                    true
                }
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.vacancyState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is VacancyDetailState.Loading -> showLoading()
                is VacancyDetailState.Success -> showContent(state.vacancy)
                is VacancyDetailState.Error -> showError(state.errorType)
            }
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            val icon = if (isFavorite) R.drawable.ic_favorites_filled else R.drawable.ic_favorites_outline
            binding.toolbar.menu.findItem(R.id.action_favorite)?.setIcon(icon)
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            event ?: return@observe
            when (event) {
                is NavigationEvent.ShareLink -> shareLink(event.url)
                is NavigationEvent.OpenEmail -> openEmail(event.email)
                is NavigationEvent.OpenPhone -> openPhone(event.phone)
            }
            viewModel.clearNavigationEvent()
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollViewContent.visibility = View.GONE
        binding.errorContainer.visibility = View.GONE
    }

    private fun showContent(vacancy: VacancyDetailDto) {
        binding.progressBar.visibility = View.GONE
        binding.errorContainer.visibility = View.GONE
        binding.scrollViewContent.visibility = View.VISIBLE

        bindVacancyData(vacancy)
    }

    private fun showError(errorType: VacancyDetailErrorType) {
        binding.progressBar.visibility = View.GONE
        binding.scrollViewContent.visibility = View.GONE
        binding.errorContainer.visibility = View.VISIBLE

        when (errorType) {
            VacancyDetailErrorType.NO_INTERNET -> {
                binding.errorImage.setImageResource(R.drawable.placeholder_no_internet)
                binding.errorText.text = getString(R.string.error_no_internet)
            }
            VacancyDetailErrorType.NOT_FOUND -> {
                binding.errorImage.setImageResource(R.drawable.placeholder_not_found)
                binding.errorText.text = getString(R.string.error_vacancy_not_found)
            }
            VacancyDetailErrorType.SERVER_ERROR -> {
                binding.errorImage.setImageResource(R.drawable.placeholder_server_error)
                binding.errorText.text = getString(R.string.error_loading_data)
            }
        }
    }

    private fun bindVacancyData(vacancy: VacancyDetailDto) {
        binding.tvVacancyTitle.text = vacancy.name

        binding.tvSalary.text = formatSalary(vacancy)

        binding.tvEmployerName.text = vacancy.employer.name
        binding.tvEmployerCity.text = vacancy.area.name

        val logoUrl = vacancy.employer.logo
        if (!logoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(logoUrl)
                .placeholder(R.drawable.ic_favorites)
                .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.logo_corner_radius)))
                .into(binding.ivEmployerLogo)
        } else {
            binding.ivEmployerLogo.setImageResource(R.drawable.ic_favorites)
        }

        val experience = vacancy.experience?.name
        if (!experience.isNullOrEmpty()) {
            binding.tvExperienceValue.text = experience
            binding.layoutExperience.visibility = View.VISIBLE
        } else {
            binding.layoutExperience.visibility = View.GONE
        }

        val employmentSchedule = buildEmploymentSchedule(vacancy)
        if (employmentSchedule.isNotEmpty()) {
            binding.tvEmploymentSchedule.text = employmentSchedule
            binding.tvEmploymentSchedule.visibility = View.VISIBLE
        } else {
            binding.tvEmploymentSchedule.visibility = View.GONE
        }

        val description = vacancy.description
        if (!description.isNullOrEmpty()) {
            @Suppress("DEPRECATION")
            binding.tvDescription.text = Html.fromHtml(description)
            binding.layoutDescription.visibility = View.VISIBLE
        } else {
            binding.layoutDescription.visibility = View.GONE
        }

        val skills = vacancy.skills
        if (!skills.isNullOrEmpty()) {
            binding.tvSkillsValue.text = skills.joinToString("\n") { "• $it" }
            binding.layoutSkills.visibility = View.VISIBLE
        } else {
            binding.layoutSkills.visibility = View.GONE
        }

        bindContacts(vacancy.contacts)
    }

    private fun bindContacts(contacts: ContactsDto?) {
        if (contacts == null) {
            binding.layoutContacts.visibility = View.GONE
            return
        }

        var hasContacts = false

        val personName = contacts.name
        if (!personName.isNullOrEmpty()) {
            binding.tvContactPerson.text = personName
            binding.tvContactPerson.visibility = View.VISIBLE
            hasContacts = true
        } else {
            binding.tvContactPerson.visibility = View.GONE
        }

        val email = contacts.email
        if (!email.isNullOrEmpty()) {
            binding.tvEmail.text = email
            binding.tvEmail.visibility = View.VISIBLE
            binding.tvEmail.setOnClickListener { viewModel.openEmail() }
            hasContacts = true
        } else {
            binding.tvEmail.visibility = View.GONE
        }

        val phone = contacts.phone?.firstOrNull()
        if (!phone.isNullOrEmpty()) {
            binding.tvPhone.text = phone
            binding.tvPhone.visibility = View.VISIBLE
            binding.tvPhone.setOnClickListener { viewModel.openPhone() }
            hasContacts = true
        } else {
            binding.tvPhone.visibility = View.GONE
        }

        binding.layoutContacts.visibility = if (hasContacts) View.VISIBLE else View.GONE
    }

    private fun formatSalary(vacancy: VacancyDetailDto): String {
        val salary = vacancy.salary
        if (salary == null) return getString(R.string.salary_not_specified)

        val from = salary.from
        val to = salary.to
        val currency = formatCurrency(salary.currency)

        return when {
            from != null && to != null -> "от $from до $to $currency"
            from != null -> "от $from $currency"
            to != null -> "до $to $currency"
            else -> getString(R.string.salary_not_specified)
        }
    }

    private fun formatCurrency(currency: String?): String {
        return when (currency) {
            "RUR", "RUB" -> "₽"
            "USD" -> "$"
            "EUR" -> "€"
            "KZT" -> "₸"
            "UAH" -> "₴"
            "BYR", "BYN" -> "Br"
            else -> currency ?: ""
        }
    }

    private fun buildEmploymentSchedule(vacancy: VacancyDetailDto): String {
        val parts = mutableListOf<String>()
        vacancy.employment?.name?.let { parts.add(it) }
        vacancy.schedule?.name?.let { parts.add(it) }
        return parts.joinToString(", ")
    }

    private fun shareLink(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    private fun openEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
        startActivity(intent)
    }

    private fun openPhone(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
