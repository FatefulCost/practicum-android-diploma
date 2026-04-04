package ru.practicum.android.diploma.ui.filter.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.util.Resource

class CountrySelectionFragment : Fragment() {

    private val workLocationViewModel: WorkLocationViewModel by viewModel()
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.databinding.FragmentCountrySelectionBinding

class CountrySelectionFragment : Fragment() {

    private var _binding: FragmentCountrySelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CountrySelectionViewModel by viewModel()
    private var adapter: CountryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_country_selection, container, false)
        _binding = FragmentCountrySelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)
        observeCountries()
        workLocationViewModel.loadCountries()
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        adapter = CountryAdapter(emptyList()) { country ->
            val bundle = Bundle().apply {
                putInt("selectedCountryId", country.id)
                putString("selectedCountryName", country.name)
            }
            parentFragmentManager.setFragmentResult("country_selection", bundle)
            findNavController().popBackStack()
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeCountries() {
        workLocationViewModel.countries.onEach { resource ->
            when (resource) {
                is Resource.Success -> {
                    val countries = resource.data?.filter { it.parentId == null } ?: emptyList()
                    adapter?.updateData(countries)
                }
                is Resource.Error -> { }
                else -> { }
            }
        }.launchIn(lifecycleScope)
    }

    inner class CountryAdapter(
        private var countries: List<FilterAreaDto>,
        private val onItemClick: (FilterAreaDto) -> Unit
    ) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return CountryViewHolder(view, onItemClick)
        }

        override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
            holder.bind(countries[position])
        }

        override fun getItemCount(): Int = countries.size

        fun updateData(newCountries: List<FilterAreaDto>) {
            countries = newCountries
            notifyDataSetChanged()
        }

        inner class CountryViewHolder(
            itemView: View,
            private val onItemClick: (FilterAreaDto) -> Unit
        ) : RecyclerView.ViewHolder(itemView) {
            private val textView: TextView = itemView.findViewById(android.R.id.text1)

            fun bind(country: FilterAreaDto) {
                textView.text = country.name
                itemView.setOnClickListener { onItemClick(country) }
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = CountryAdapter { country ->
            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set(WorkLocationFragment.KEY_SELECTED_COUNTRY_ID, country.id)
            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set(WorkLocationFragment.KEY_SELECTED_COUNTRY_NAME, country.name)
            findNavController().popBackStack()
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CountrySelectionFragment.adapter
            addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    requireContext(),
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        adapter = null
        super.onDestroyView()
        _binding = null
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

    private fun renderState(state: CountrySelectionState) {
        when (state) {
            is CountrySelectionState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.layoutError.visibility = View.GONE
                binding.layoutEmpty.visibility = View.GONE
            }
            is CountrySelectionState.Content -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.layoutError.visibility = View.GONE
                binding.layoutEmpty.visibility = View.GONE
                adapter?.submitList(state.countries)
            }
            is CountrySelectionState.Empty -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.layoutError.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            }
            is CountrySelectionState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.layoutError.visibility = View.VISIBLE
                binding.layoutEmpty.visibility = View.GONE
            }
        }
    }
}
