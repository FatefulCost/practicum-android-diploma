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

class RegionSelectionFragment : Fragment() {

    private val filterViewModel: FilterViewModel by viewModel()
    private val workLocationViewModel: WorkLocationViewModel by viewModel()
    private lateinit var adapter: RegionAdapter
    private var countryId: Int = -1
    private var countryName: String = ""

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

        setupRecyclerView(view)
        observeRegions()

        if (countryId != -1) {
            workLocationViewModel.loadRegions(countryId)
        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        adapter = RegionAdapter(emptyList()) { region ->
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
        workLocationViewModel.regions.onEach { resource ->
            when (resource) {
                is Resource.Success -> {
                    adapter.updateData(resource.data ?: emptyList())
                }
                is Resource.Error -> { }
                else -> { }
            }
        }.launchIn(lifecycleScope)
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
