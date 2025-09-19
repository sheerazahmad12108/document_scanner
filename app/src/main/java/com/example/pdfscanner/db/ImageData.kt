package com.example.pdfscanner.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ImageData(
    @ColumnInfo(name = "type")
    var type: String,
    @ColumnInfo(name = "dateCreated")
    var dateCreated: String,
    @ColumnInfo(name = "name")
    var name: String,
//    @ColumnInfo(name = "data")
//    var data: ByteArray,
    @ColumnInfo(name = "path")
    var path: String,
    @ColumnInfo(name = "favorite")
    var favorite: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageData

        if (id != other.id) return false
        if (type != other.type) return false
        if (dateCreated != other.dateCreated) return false
//        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + type.hashCode()
        result = 31 * result + dateCreated.hashCode()
//        result = 31 * result + data.contentHashCode()
        return result
    }

}
