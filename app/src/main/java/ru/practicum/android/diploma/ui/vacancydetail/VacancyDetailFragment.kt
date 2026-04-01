package ru.practicum.android.diploma.ui.vacancydetail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.dto.ContactsDto
import ru.practicum.android.diploma.data.dto.VacancyDetailDto
import ru.practicum.android.diploma.databinding.FragmentVacancyDetailBinding
import ru.practicum.android.diploma.ui.detail.FavoriteErrorEvent
import ru.practicum.android.diploma.ui.detail.NavigationEvent
import ru.practicum.android.diploma.ui.detail.VacancyDetailErrorType
import ru.practicum.android.diploma.ui.detail.VacancyDetailState
import ru.practicum.android.diploma.ui.detail.VacancyDetailViewModel
import ru.practicum.android.diploma.util.VacancyDescriptionFormatter

class VacancyDetailFragment : Fragment() {

    private var _binding: FragmentVacancyDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VacancyDetailViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVacancyDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        observeViewModel()
        val vacancyId = arguments?.getString("vacancyId").orEmpty()
        if (vacancyId.isNotEmpty()) viewModel.loadVacancy(vacancyId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)

        // цвет иконки
        val upArrow = binding.toolbar.navigationIcon
        upArrow?.setTint(ContextCompat.getColor(requireContext(), R.color.title_color))
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.action_share -> { viewModel.openShareDialog(); true }
                R.id.action_favorite -> { viewModel.toggleFavorite(); true }
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
        viewModel.favoriteErrorEvent.observe(viewLifecycleOwner) { event ->
            if (event != null) {
                val message = when (event) {
                    is FavoriteErrorEvent.AddError -> getString(R.string.error_add_to_favorites)
                    is FavoriteErrorEvent.RemoveError -> getString(R.string.error_remove_from_favorites)
                }
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                viewModel.clearFavoriteErrorEvent()
            }
        }
        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            if (event != null) handleNavigationEvent(event)
        }
    }

    private fun handleNavigationEvent(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.ShareLink -> shareLink(event.url)
            is NavigationEvent.OpenEmail -> openEmail(event.email)
            is NavigationEvent.OpenPhone -> openPhone(event.phone)
        }
        viewModel.clearNavigationEvent()
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
        binding.tvSalary.text = formatSalary(vacancy, getString(R.string.salary_not_specified))
        bindEmployerSection(vacancy)
        bindExperienceSection(vacancy)
        bindDescriptionSection(vacancy)
        bindSkillsSection(vacancy)
        bindContacts(vacancy.contacts)
    }

    private fun bindEmployerSection(vacancy: VacancyDetailDto) {
        binding.tvEmployerName.text = vacancy.employer.name
        binding.tvEmployerCity.text = vacancy.area.name
        val logoUrl = vacancy.employer.logo
        if (!logoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(logoUrl)
                .placeholder(R.drawable.placeholder_32px)
                .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.logo_corner_radius)))
                .into(binding.ivEmployerLogo)
        } else {
            binding.ivEmployerLogo.setImageResource(R.drawable.placeholder_32px)
        }
    }

    private fun bindExperienceSection(vacancy: VacancyDetailDto) {
        val experience = vacancy.experience?.name
        if (!experience.isNullOrEmpty()) {
            binding.tvExperienceValue.text = experience
            binding.layoutExperience.visibility = View.VISIBLE
        } else {
            binding.layoutExperience.visibility = View.GONE
        }
        val schedule = buildEmploymentSchedule(vacancy)
        binding.tvEmploymentSchedule.text = schedule
        binding.tvEmploymentSchedule.visibility = if (schedule.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun bindDescriptionSection(vacancy: VacancyDetailDto) {
        val description = vacancy.description
        if (!description.isNullOrEmpty()) {
            val formattedDescription = VacancyDescriptionFormatter.formatDescription(description)
            binding.tvDescription.text = formattedDescription ?: description
            binding.layoutDescription.visibility = View.VISIBLE
        } else {
            binding.layoutDescription.visibility = View.GONE
        }
    }

    private fun bindSkillsSection(vacancy: VacancyDetailDto) {
        val skills = vacancy.skills
        if (!skills.isNullOrEmpty()) {
            binding.tvSkillsValue.text = skills.joinToString("\n") { "• $it" }
            binding.layoutSkills.visibility = View.VISIBLE
        } else {
            binding.layoutSkills.visibility = View.GONE
        }
    }

    private fun bindContacts(contacts: ContactsDto?) {
        if (contacts == null) {
            binding.layoutContacts.visibility = View.GONE
            return
        }
        val hasName = setContactField(binding.tvContactPerson, contacts.name)
        val hasEmail = setContactField(binding.tvEmail, contacts.email) { viewModel.openEmail() }
        val hasPhone = setContactField(binding.tvPhone, contacts.phone?.firstOrNull()) { viewModel.openPhone() }
        binding.layoutContacts.visibility = if (hasName || hasEmail || hasPhone) View.VISIBLE else View.GONE
    }

    private fun setContactField(textView: TextView, value: String?, onClick: (() -> Unit)? = null): Boolean {
        val hasValue = !value.isNullOrEmpty()
        textView.visibility = if (hasValue) View.VISIBLE else View.GONE
        if (hasValue) {
            textView.text = value
            onClick?.let { listener -> textView.setOnClickListener { listener() } }
        }
        return hasValue
    }

    private fun shareLink(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    private fun openEmail(email: String) {
        startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")))
    }

    private fun openPhone(phone: String) {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
