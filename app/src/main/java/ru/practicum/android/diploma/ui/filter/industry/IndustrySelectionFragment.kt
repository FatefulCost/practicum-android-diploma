package ru.practicum.android.diploma.ui.filter.industry

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
import ru.practicum.android.diploma.databinding.FragmentIndustrySelectionBinding

class IndustrySelectionFragment : Fragment() {

    private var _binding: FragmentIndustrySelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IndustrySelectionViewModel by viewModel()
    private var adapter: IndustryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIndustrySelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        observeViewModel()
        setupButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = IndustryAdapter(
            onIndustryClick = { industry ->
                // При клике на отрасль обновляем выбранную
                val currentSelected = adapter?.getSelectedIndustry()
                if (currentSelected?.id == industry.id) {
                    // Если нажали на уже выбранную, то снимаем выбор
                    adapter?.setSelectedIndustry(null)
                } else {
                    // Иначе выбираем новую
                    adapter?.setSelectedIndustry(industry.id)
                }
            },
            onSelectionChanged = {
                // При изменении выбора обновляем видимость кнопки
                updateSelectButtonVisibility()
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@IndustrySelectionFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                updateSearchIcon(query)
                viewModel.filterIndustries(query)
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

    private fun setupButton() {
        binding.btnSelect.setOnClickListener {
            val selectedIndustry = adapter?.getSelectedIndustry()
            if (selectedIndustry != null) {
                // Возвращаем результат в FilterFragment
                parentFragmentManager.setFragmentResult(
                    "industry_selection",
                    Bundle().apply {
                        putInt("industry_id", selectedIndustry.id)
                        putString("industry_name", selectedIndustry.name)
                    }
                )
                findNavController().popBackStack()
            }
        }
    }

    /**
     * Показываем/скрываем кнопку "Выбрать" в зависимости от выбора
     */
    private fun updateSelectButtonVisibility() {
        val hasSelection = adapter?.getSelectedIndustry() != null
        binding.btnSelect.visibility = if (hasSelection) View.VISIBLE else View.GONE
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

    private fun renderState(state: IndustrySelectionState) {
        binding.progressBar.isVisible = state is IndustrySelectionState.Loading
        binding.recyclerView.isVisible = state is IndustrySelectionState.Content
        binding.layoutError.isVisible = state is IndustrySelectionState.Error
        binding.layoutEmpty.isVisible = state is IndustrySelectionState.Empty

        if (state is IndustrySelectionState.Content) {
            adapter?.submitList(state.industries)

            // Восстанавливаем выбранную отрасль из ViewModel
            val savedIndustryId = viewModel.getSelectedIndustryId()
            if (savedIndustryId != null && savedIndustryId != -1) {
                // Проверяем, есть ли эта отрасль в текущем списке
                val industryExists = state.industries.any { it.id == savedIndustryId }
                if (industryExists) {
                    adapter?.setSelectedIndustry(savedIndustryId)
                } else {
                    adapter?.setSelectedIndustry(null)
                }
            } else {
                adapter?.setSelectedIndustry(null)
            }

            updateSelectButtonVisibility()
        }
    }

    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        adapter = null
        super.onDestroyView()
        _binding = null
    }
}
