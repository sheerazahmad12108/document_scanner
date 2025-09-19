package com.example.pdfscanner.db

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class DatabaseViewModel(private val context: Context) : ViewModel() {

    private lateinit var repository: DatabaseRepository
    private var dao: MyDao = AppDatabase.getDatabase(context).myDao()

    init {
        repository = DatabaseRepository(dao)
    }

    val allRecords: LiveData<MutableList<ImageData>> = repository.allRecords.asLiveData()
    val allPDFRecords: LiveData<MutableList<PDFData>> = repository.allPDFRecords.asLiveData()
    val allFavoritePDFRecords: LiveData<MutableList<PDFData>> =
        repository.allFavoritePDFRecords.asLiveData()
    val allFavoriteImageRecords: LiveData<MutableList<ImageData>> =
        repository.allFavoriteImageRecords.asLiveData()

    fun insetRecord(imageData: ImageData) = viewModelScope.launch {
        repository.insertRecord(imageData)
    }

    fun insertPDFRecord(pdfData: PDFData) = viewModelScope.launch {
        repository.insertPDFRecord(pdfData)
    }

    fun updatePDFRecord(pdfData: PDFData) = viewModelScope.launch {
        repository.updatePDFRecord(pdfData)
    }

    fun deletePDFRecord(pdfData: PDFData) = viewModelScope.launch {
        repository.deletePDFRecord(pdfData)
    }

    fun updateImageRecord(imageData: ImageData) = viewModelScope.launch {
        repository.updateImageRecord(imageData)
    }

    fun deleteImageRecord(imageData: ImageData) = viewModelScope.launch {
        repository.deleteImageRecord(imageData)
    }

    class DatabaseViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DatabaseViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DatabaseViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown Mdel Class")
        }
    }

}