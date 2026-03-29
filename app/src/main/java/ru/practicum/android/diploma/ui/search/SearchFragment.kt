package ru.practicum.android.diploma.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModel()
    private lateinit var adapter: VacancyAdapter
    private var isLoadingMore = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeState()
    }

    private fun setupUI() {
        // Инициализация адаптера с пустым списком
        adapter = VacancyAdapter(emptyList()) { vacancy ->
            val bundle = Bundle().apply {
                putString("vacancyId", vacancy.id)
            }
            findNavController().navigate(
                R.id.action_searchFragment_to_vacancyDetailFragment,
                bundle
            )
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Кнопка фильтра
        binding.fabFilter.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_filterFragment)
        }

        // Обработка ввода текста
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateSearchQuery(s?.toString() ?: "")
            }
        })

        // Поиск по нажатию Enter
        binding.editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.editTextSearch.text.toString()
                if (query.isNotBlank()) {
                    viewModel.performSearch(query, 1)
                }
                true
            } else {
                false
            }
        }

        // Скролл-листенер для пагинации
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = adapter.itemCount

                // Если дошли до конца, загружаем следующую страницу
                if (!isLoadingMore && lastVisiblePosition >= totalItemCount - 3 && totalItemCount > 0) {
                    isLoadingMore = true
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun observeState() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.Empty -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.textViewEmpty.visibility = View.VISIBLE
                    binding.textViewEmpty.text = getString(R.string.search_hint)
                }
                is SearchState.Loading -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.textViewEmpty.visibility = View.VISIBLE
                    binding.textViewEmpty.text = "Загрузка..."
                }
                is SearchState.LoadingMore -> {
                    // Показываем индикатор загрузки следующих страниц
                    Toast.makeText(requireContext(), "Загрузка следующих вакансий...", Toast.LENGTH_SHORT).show()
                }
                is SearchState.EmptyResult -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.textViewEmpty.visibility = View.VISIBLE
                    binding.textViewEmpty.text = "Ничего не найдено"
                }
                is SearchState.Success -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.textViewEmpty.visibility = View.GONE
                    adapter.updateData(state.vacancies)
                }
                is SearchState.LoadMoreError -> {
                    // Показываем ошибку дозагрузки, но сохраняем текущий список
                    Toast.makeText(requireContext(), "Не удалось загрузить следующие вакансии", Toast.LENGTH_SHORT).show()
                    isLoadingMore = false  // Сбрасываем флаг при ошибке дозагрузки
                }
                is SearchState.Error -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.textViewEmpty.visibility = View.VISIBLE
                    binding.textViewEmpty.text = when (state.error) {
                        ErrorType.NO_INTERNET -> getString(R.string.error_no_internet)
                        ErrorType.SERVER_ERROR -> getString(R.string.error_loading_data)
                    }
                }
            }

            // Сбрасываем флаг, когда загрузка закончена
            if (state !is SearchState.Loading && state !is SearchState.LoadingMore) {
                isLoadingMore = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
