package ru.practicum.android.diploma.ui.filter.location

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
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

class RegionSelectionFragment : Fragment() {

    private val filterViewModel: FilterViewModel by viewModel()
    private var adapter: RegionAdapter? = null
    private var countryId: Int = -1
    private var countryName: String = ""
    private var allRegions: List<FilterAreaDto> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_region_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            countryId = it.getInt("countryId", -1)
            countryName = it.getString("countryName", "")
        }

        Log.d("RegionSelection", "=== START ===")
        Log.d("RegionSelection", "Country ID: $countryId, Country Name: $countryName")

        setupToolbar(view)
        setupSearch(view)
        setupRecyclerView(view)
        setupRetryButton(view)
        observeRegions()

        val currentAreas = filterViewModel.areas.value
        Log.d("RegionSelection", "Current areas state: $currentAreas")

        when (currentAreas) {
            is Resource.Success -> {
                extractAndDisplayRegions(currentAreas.data)
            }
            is Resource.Error -> {
                val errorMessage = currentAreas.message ?: "Ошибка загрузки"
                updateUI(Resource.Error(errorMessage))
            }
            is Resource.Loading -> {
                updateUI(Resource.Loading())
            }
            else -> {
                Log.d("RegionSelection", "No data, forcing load")

                filterViewModel.loadAreas()
            }
        }
    }

    private fun extractAndDisplayRegions(areas: List<FilterAreaDto>?) {
        if (areas == null) {
            Log.e("RegionSelection", "Areas is null")
            updateUI(Resource.Success(emptyList()))
            return
        }

        // Ищем страну по ID
        val country = areas.find { it.id == countryId }
        Log.d("RegionSelection", "Found country: ${country?.name}")

        if (country != null) {
            // Регионы - это вложенные areas в стране
            val regions = country.areas ?: emptyList()
            Log.d("RegionSelection", "Regions found: ${regions.size}")
            regions.forEach { region ->
                Log.d("RegionSelection", "Region: id=${region.id}, name=${region.name}")
            }

            allRegions = regions
            if (regions.isNotEmpty()) {
                adapter?.updateData(regions)
                updateUI(Resource.Success(regions))
            } else {
                Log.d("RegionSelection", "No regions found for country $countryId")
                updateUI(Resource.Success(emptyList()))
            }
        } else {
            // Альтернативный способ: ищем по parentId
            val regions = areas.filter { it.parentId == countryId }
            Log.d("RegionSelection", "Regions by parentId: ${regions.size}")
            regions.forEach { region ->
                Log.d("RegionSelection", "Region: id=${region.id}, name=${region.name}, parentId=${region.parentId}")
            }

            allRegions = regions
            if (regions.isNotEmpty()) {
                adapter?.updateData(regions)
                updateUI(Resource.Success(regions))
            } else {
                Log.d("RegionSelection", "No regions found for country $countryId")
                updateUI(Resource.Success(emptyList()))
            }
        }
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupSearch(view: View) {
        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val ivSearchClear = view.findViewById<ImageView>(R.id.ivSearchClear)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterRegions(s?.toString() ?: "")
            }
        })

        ivSearchClear.setOnClickListener {
            etSearch.text.clear()
            filterRegions("")
        }
    }

    private fun filterRegions(query: String) {
        if (query.isEmpty()) {
            adapter?.updateData(allRegions)
            if (allRegions.isEmpty()) {
                updateUI(Resource.Success(emptyList()))
            } else {
                val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
                val layoutEmpty = view?.findViewById<LinearLayout>(R.id.layoutEmpty)
                recyclerView?.visibility = View.VISIBLE
                layoutEmpty?.visibility = View.GONE
            }
        } else {
            val filtered = allRegions.filter {
                it.name.contains(query, ignoreCase = true)
            }
            adapter?.updateData(filtered)
            if (filtered.isEmpty()) {
                val progressBar = view?.findViewById<ProgressBar>(R.id.progressBar)
                val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
                val layoutEmpty = view?.findViewById<LinearLayout>(R.id.layoutEmpty)

                progressBar?.visibility = View.GONE
                recyclerView?.visibility = View.GONE
                layoutEmpty?.visibility = View.VISIBLE
            } else {
                val layoutEmpty = view?.findViewById<LinearLayout>(R.id.layoutEmpty)
                layoutEmpty?.visibility = View.GONE
            }
        }
    }

    private fun setupRetryButton(view: View) {
        val btnRetry = view.findViewById<Button>(R.id.btnRetry)
        btnRetry.setOnClickListener {
            filterViewModel.loadAreas()
            updateUI(Resource.Loading())
        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        adapter = RegionAdapter(emptyList()) { region ->
            Log.d("RegionSelection", "Selected region: ${region.name}")
            val bundle = Bundle().apply {
                putInt("selectedRegionId", region.id)
                putString("selectedRegionName", region.name)
            }
            parentFragmentManager.setFragmentResult("region_selection", bundle)
            findNavController().popBackStack()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeRegions() {
        filterViewModel.areas.onEach { resource ->
            Log.d("RegionSelection", "Observe - Resource: $resource")
            when (resource) {
                is Resource.Success -> {
                    extractAndDisplayRegions(resource.data)
                }
                is Resource.Error -> {
                    val errorMessage = resource.message ?: "Ошибка загрузки"
                    updateUI(Resource.Error(errorMessage))
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
                val errorMessage = resource.message ?: "Ошибка загрузки регионов"
                tvErrorText?.text = errorMessage
            }
        }
    }

    inner class RegionAdapter(
        private var regions: List<FilterAreaDto>,
        private val onItemClick: (FilterAreaDto) -> Unit
    ) : RecyclerView.Adapter<RegionAdapter.RegionViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegionViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return RegionViewHolder(view, onItemClick)
        }

        override fun onBindViewHolder(holder: RegionViewHolder, position: Int) {
            holder.bind(regions[position])
        }

        override fun getItemCount(): Int = regions.size

        fun updateData(newRegions: List<FilterAreaDto>) {
            regions = newRegions
            notifyDataSetChanged()
        }

        inner class RegionViewHolder(
            itemView: View,
            private val onItemClick: (FilterAreaDto) -> Unit
        ) : RecyclerView.ViewHolder(itemView) {
            private val textView: TextView = itemView.findViewById(android.R.id.text1)

            fun bind(region: FilterAreaDto) {
                textView.text = region.name
                itemView.setOnClickListener { onItemClick(region) }
            }
        }
    }
}
