package com.example.pdfscanner.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pdfscanner.model.PdfFile

@Entity
data class PDFData(
    @ColumnInfo(name = "type")
    var type: String,
    @ColumnInfo(name = "dateCreated")
    var dateCreated: String,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "path")
    var path: String,
    @ColumnInfo(name = "favorite")
    var favorite: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    companion object {
        val SortByName = Comparator<PDFData> { o1, o2 -> o1.name.compareTo(o2.name, true) }
        val SortByDate =
            Comparator<PDFData> { o1, o2 -> o2.dateCreated.compareTo(o1.dateCreated) }
//        val SortBySize = Comparator<PDFData> { o1, o2 -> o2.size.compareTo(o1.size) }

    }
}
