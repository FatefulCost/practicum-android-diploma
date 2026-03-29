package ru.practicum.android.diploma.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFavoritesBinding

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModel()
    private lateinit var adapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites()
    }

    private fun setupRecyclerView() {
        adapter = FavoritesAdapter { vacancy ->
            val bundle = Bundle().apply { putString("vacancyId", vacancy.id) }
            findNavController().navigate(
                R.id.action_favoritesFragment_to_vacancyDetailFragment,
                bundle
            )
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FavoritesFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.favoritesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoritesState.Loading -> showLoading()
                is FavoritesState.Content -> showContent(state)
                is FavoritesState.Empty -> showEmpty()
                is FavoritesState.Error -> showError()
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.placeholderEmpty.visibility = View.GONE
        binding.placeholderError.visibility = View.GONE
    }

    private fun showContent(state: FavoritesState.Content) {
        binding.progressBar.visibility = View.GONE
        binding.placeholderEmpty.visibility = View.GONE
        binding.placeholderError.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        adapter.updateVacancies(state.vacancies)
    }

    private fun showEmpty() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.placeholderError.visibility = View.GONE
        binding.placeholderEmpty.visibility = View.VISIBLE
    }

    private fun showError() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.placeholderEmpty.visibility = View.GONE
        binding.placeholderError.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
