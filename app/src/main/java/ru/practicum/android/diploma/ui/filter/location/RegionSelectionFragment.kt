package ru.practicum.android.diploma.ui.filter.location

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentRegionSelectionBinding

class RegionSelectionFragment : Fragment() {

    private var _binding: FragmentRegionSelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegionSelectionViewModel by viewModel()
    private var adapter: RegionAdapter? = null
    private var selectedCountryId: Int = -1
    private var selectedCountryName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegionSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Получаем переданные аргументы
        selectedCountryId = arguments?.getInt("selected_country_id", -1) ?: -1
        selectedCountryName = arguments?.getString("selected_country_name", "") ?: ""

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        observeViewModel()

        // Загружаем регионы с учетом выбранной страны
        viewModel.loadRegions(selectedCountryId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = RegionAdapter { region ->
            // Находим страну, к которой относится регион
            val countryId = viewModel.getCountryIdForRegion(region.id)
            val countryName = viewModel.getCountryNameForRegion(region.id)

            val savedStateHandle = findNavController().previousBackStackEntry?.savedStateHandle

            // Если страны не было выбрана, то автоматически выбираем её
            if (selectedCountryId == -1 && countryId != -1) {
                savedStateHandle?.set("auto_selected_country_id", countryId)
                savedStateHandle?.set("auto_selected_country_name", countryName)
            }

            // Сохраняем выбранный регион
            savedStateHandle?.set(WorkLocationFragment.KEY_SELECTED_REGION_ID, region.id)
            savedStateHandle?.set(WorkLocationFragment.KEY_SELECTED_REGION_NAME, region.name)

            findNavController().popBackStack()
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RegionSelectionFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                updateSearchIcon(query)
                viewModel.filterRegions(query)
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        binding.ivSearchClear.setOnClickListener {
            if (binding.etSearch.text.isNotEmpty()) {
                binding.etSearch.text.clear()
            }
        }
    }

    private fun updateSearchIcon(query: String) {
        if (query.isEmpty()) {
            binding.ivSearchClear.setImageResource(R.drawable.search_24px)
            binding.ivSearchClear.isClickable = false
        } else {
            binding.ivSearchClear.setImageResource(R.drawable.close_24px)
            binding.ivSearchClear.isClickable = true
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: RegionSelectionState) {
        binding.progressBar.isVisible = state is RegionSelectionState.Loading
        binding.recyclerView.isVisible = state is RegionSelectionState.Content
        binding.layoutError.isVisible = state is RegionSelectionState.Error
        binding.layoutEmpty.isVisible = state is RegionSelectionState.Empty

        if (state is RegionSelectionState.Content) {
            adapter?.submitList(state.regions)
        }
    }

    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        adapter = null
        super.onDestroyView()
        _binding = null
    }
}
