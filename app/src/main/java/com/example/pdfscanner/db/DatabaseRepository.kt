package com.example.pdfscanner.db

import kotlinx.coroutines.flow.Flow

class DatabaseRepository(private val myDao: MyDao) {

    val allRecords: Flow<MutableList<ImageData>> = myDao.getAllData()
    val allPDFRecords: Flow<MutableList<PDFData>> = myDao.getAllPDFData()
    val allFavoritePDFRecords: Flow<MutableList<PDFData>> = myDao.getAllFavoritePDFData()
    val allFavoriteImageRecords: Flow<MutableList<ImageData>> = myDao.getAllFavoriteImageData()

    suspend fun insertRecord(imageData: ImageData){
        myDao.insertImage(imageData)
    }

    suspend fun insertPDFRecord(pdfData: PDFData){
        myDao.insertPDF(pdfData)
    }

    suspend fun updatePDFRecord(pdfData: PDFData){
        myDao.updatePDFData(pdfData)
    }

    suspend fun updateImageRecord(imageData: ImageData){
        myDao.updateImageData(imageData)
    }

    suspend fun deletePDFRecord(pdfData: PDFData){
        myDao.deletePDFData(pdfData)
    }

    suspend fun deleteImageRecord(imageData: ImageData){
        myDao.deleteImageData(imageData)
    }
}