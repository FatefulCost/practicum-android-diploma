package ru.practicum.android.diploma.ui.filter.industry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.dto.FilterIndustryDto

class IndustrySelectionViewModel(
    private val repository: IndustryRepository
) : ViewModel() {

    private val _industries = MutableLiveData<List<FilterIndustryDto>>()
    val industries: LiveData<List<FilterIndustryDto>> = _industries

    private val _selectedId = MutableLiveData<String?>()
    val selectedId: LiveData<String?> = _selectedId

    fun loadIndustries() {
        viewModelScope.launch {
            val items = repository.getIndustries().map {
                it.copy(isChecked = it.isChecked)
            }
            _industries.postValue(items)
        }
    }

    fun onItemChecked(id: String) {
        val updated = _industries.value?.map {
            it.copy(isChecked = (it.id == id))
        } ?: return

        _industries.value = updated
        _selectedId.value = id
    }

    fun applySelection(): FilterIndustryDto? {
        return _industries.value?.firstOrNull { it.isChecked }
    }
}
