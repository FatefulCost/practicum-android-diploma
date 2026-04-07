package ru.practicum.android.diploma.ui.filter.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
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
        _binding = FragmentCountrySelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set(WorkLocationFragment.KEY_SELECTED_REGION_ID, -1)
            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set(WorkLocationFragment.KEY_SELECTED_REGION_NAME, "")
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
