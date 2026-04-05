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
import ru.practicum.android.diploma.ui.filter.FilterViewModel
import ru.practicum.android.diploma.util.Resource
import android.util.Log
import android.widget.LinearLayout
import android.widget.ProgressBar

class CountrySelectionFragment : Fragment() {

    private val filterViewModel: FilterViewModel by viewModel()
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

        setupToolbar(view)
        setupRecyclerView(view)
        setupRetryButton(view)
        observeCountries()

        // Если данные уже загружены, показываем их
        val currentAreas = filterViewModel.areas.value
        if (currentAreas is Resource.Success) {
            val countries = currentAreas.data?.filter { it.parentId == null } ?: emptyList()
            updateUI(Resource.Success(countries))
        } else if (currentAreas is Resource.Error) {
            updateUI(Resource.Error(currentAreas.message))
        } else if (currentAreas is Resource.Loading) {
            updateUI(Resource.Loading())
        }
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRetryButton(view: View) {
        val btnRetry = view.findViewById<android.widget.Button>(R.id.btnRetry)
        btnRetry.setOnClickListener {
            // Повторно загружаем данные
            filterViewModel.loadAreas() // Добавьте этот метод в FilterViewModel
            updateUI(Resource.Loading())
        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        adapter = CountryAdapter(emptyList()) { country ->
            Log.d("CountrySelection", "Selected country: ${country.name}")
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
        filterViewModel.areas.onEach { resource ->
            Log.d("CountrySelection", "Resource: $resource")
            when (resource) {
                is Resource.Success -> {
                    val countries = resource.data?.filter { it.parentId == null } ?: emptyList()
                    if (countries.isEmpty()) {
                        updateUI(Resource.Success(emptyList()))
                    } else {
                        adapter?.updateData(countries)
                        updateUI(Resource.Success(countries))
                    }
                }
                is Resource.Error -> {
                    updateUI(Resource.Error(resource.message))
                }
                is Resource.Loading -> {
                    updateUI(Resource.Loading())
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun updateUI(resource: Resource<List<FilterAreaDto>>) {
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBar)
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
        val layoutError = view?.findViewById<LinearLayout>(R.id.layoutError)
        val layoutEmpty = view?.findViewById<LinearLayout>(R.id.layoutEmpty)

        when (resource) {
            is Resource.Loading -> {
                progressBar?.visibility = View.VISIBLE
                recyclerView?.visibility = View.GONE
                layoutError?.visibility = View.GONE
                layoutEmpty?.visibility = View.GONE
            }
            is Resource.Success -> {
                progressBar?.visibility = View.GONE
                if (resource.data.isNullOrEmpty()) {
                    recyclerView?.visibility = View.GONE
                    layoutError?.visibility = View.GONE
                    layoutEmpty?.visibility = View.VISIBLE
                } else {
                    recyclerView?.visibility = View.VISIBLE
                    layoutError?.visibility = View.GONE
                    layoutEmpty?.visibility = View.GONE
                }
            }
            is Resource.Error -> {
                progressBar?.visibility = View.GONE
                recyclerView?.visibility = View.GONE
                layoutError?.visibility = View.VISIBLE
                layoutEmpty?.visibility = View.GONE

                val tvErrorText = view?.findViewById<TextView>(R.id.tvErrorText)
                // Исправление: используем ?: для обработки null
                val errorMessage = resource.message ?: "Ошибка загрузки данных"
                tvErrorText?.text = errorMessage
            }
        }
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
