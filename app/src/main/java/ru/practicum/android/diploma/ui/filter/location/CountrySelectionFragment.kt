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
    private var adapter: CountryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_country_selection, container, false)
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
            }
        }
    }
}
