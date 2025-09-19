package com.example.pdfscanner.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(data: ImageData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPDF(data: PDFData)

    @Query("SELECT * FROM ImageData")
    fun getAllData(): Flow<MutableList<ImageData>>

    @Query("SELECT * FROM PDFData")
    fun getAllPDFData(): Flow<MutableList<PDFData>>

    @Query("SELECT * FROM PDFData WHERE favorite == 1")
    fun getAllFavoritePDFData(): Flow<MutableList<PDFData>>

    @Query("SELECT * FROM ImageData WHERE favorite == 1")
    fun getAllFavoriteImageData(): Flow<MutableList<ImageData>>

    @Update
    suspend fun updatePDFData(data: PDFData)

    @Update
    suspend fun updateImageData(data: ImageData)

    @Delete
    suspend fun deletePDFData(data: PDFData)

    @Delete
    suspend fun deleteImageData(data: ImageData)
}