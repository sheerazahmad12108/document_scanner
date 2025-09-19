package com.example.pdfscanner.adapters

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.createBitmap
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfscanner.R
import java.io.File

class FileActionAdapter(
    private var listName: ArrayList<String>,
    private var listPath: ArrayList<String>
) : RecyclerView.Adapter<FileActionAdapter.MergedViewHolder>() {

    private lateinit var itemClick: FileActionItemClick
    val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    val cacheSize = maxMemory / 8
    val thumbnailCache: LruCache<String, Bitmap> = LruCache(cacheSize)

    inner class MergedViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val ivFileIcon: ImageView = view.findViewById(R.id.ivFileIcon)
        val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        val tvFilePath: TextView = view.findViewById(R.id.tvFilePath)
//        val ivSplitPDFIconCheck: ImageView = view.findViewById(R.id.ivSplitSelect)

        fun bind(position: Int) {
            tvFileName.text = listName[position]
            tvFilePath.text = listPath[position]
            var extension = listName[position].substringAfter(".")
            if (extension == "pdf") {
                try {
                    var thumbnail = thumbnailCache.get(listPath[position])
                    if (thumbnail == null) {
                        thumbnail = generateThumbnail(listPath[position])
                        if (true) {
                            thumbnailCache.put(listPath[position], thumbnail)
                        }
                    }

                    if (true) {
                        ivFileIcon.setImageBitmap(thumbnail)
                    } else {
                        ivFileIcon.setImageResource(R.drawable.pdf_icon_history)
                    }


                } catch (_: Exception) {
                    ivFileIcon.setImageResource(R.drawable.pdf_icon_history)
                }
            } else if (extension == "jpg") {
                ivFileIcon.setImageResource(R.drawable.image_icon)
            } else {
                ivFileIcon.setImageResource(R.drawable.text_icon)
            }
        }
    }

    private fun generateThumbnail(pdfFilePath: String): Bitmap {
        var pdfRenderer: PdfRenderer? = null
        var currentPage: PdfRenderer.Page? = null
        val file = File(pdfFilePath)

        val fileDescriptor =
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(fileDescriptor)

        // Use the first page to generate the thumbnail
        currentPage = pdfRenderer.openPage(0)
        val bitmap: Bitmap = createBitmap(currentPage.width / 4, currentPage.height / 4)
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        return bitmap
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MergedViewHolder {
        val view = MergedViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.file_action_item_list, parent, false)
        )
        return view
    }

    override fun onBindViewHolder(
        holder: MergedViewHolder,
        position: Int
    ) {
        holder.bind(position)
        holder.itemView.setOnClickListener {
            itemClick.onItemClick(position)
        }
    }

    override fun getItemCount() = listName.size

    interface FileActionItemClick {
        fun onItemClick(position: Int)
    }

    fun onFileActionItemClick(onItemClick: FileActionItemClick) {
        this.itemClick = onItemClick
    }
}