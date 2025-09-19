package com.example.pdfscanner

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pdfscanner.utils.Event

class SharedViewModel: ViewModel() {
    private val _filterClick = MutableLiveData<Event<Unit>>()
    val filterClick: LiveData<Event<Unit>> get() = _filterClick

    fun onFilterClicked() {
        _filterClick.value = Event(Unit)
    }

    private val _sortClick = MutableLiveData<Event<Unit>>()
    val sortClick: LiveData<Event<Unit>> get() = _sortClick

    fun onSortClicked() {
        _sortClick.value = Event(Unit)
    }

}