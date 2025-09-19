package com.example.pdfscanner.model

import kotlin.text.compareTo

data class PdfFile(
    val name: String,
    val path: String,
    val size: Long,
    val dateModified: Long,
    val type: String
) {
    companion object {
        val SortByName = Comparator<PdfFile> { o1, o2 -> o1.name.compareTo(o2.name, true) }
        val SortByDate =
            Comparator<PdfFile> { o1, o2 -> o2.dateModified.compareTo(o1.dateModified) }
        val SortBySize = Comparator<PdfFile> { o1, o2 -> o2.size.compareTo(o1.size) }

    }

}
