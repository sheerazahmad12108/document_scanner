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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfscanner.R
import com.example.pdfscanner.model.PdfFile
import java.io.File

class MergedFilesAdapter(private var list: ArrayList<PdfFile>) :
    RecyclerView.Adapter<MergedFilesAdapter.MergedViewHolder>() {

    private lateinit var itemClick: MergeItemClick
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    val thumbnailCache: LruCache<String, Bitmap> = LruCache(cacheSize)

    inner class MergedViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val ivMergedFile: ImageView = view.findViewById(R.id.ivMergeIcon)
        private val tvMergedFile: TextView = view.findViewById(R.id.tvMergeFiles)
        val ivSplitPDFIconCheck: ImageView = view.findViewById(R.id.ivSplitSelect)

        fun bind(item: PdfFile) {
            tvMergedFile.text = item.name
            try {
                var thumbnail = thumbnailCache.get(item.path)
                if (thumbnail == null) {
                    thumbnail = generateThumbnail(item.path)
                    if (true) {
                        thumbnailCache.put(item.path, thumbnail)
                    }
                }

                if (true) {
                    ivMergedFile.setImageBitmap(thumbnail)
                } else {
                    ivMergedFile.setImageResource(R.drawable.pdf_icon_history)
                }


            } catch (_: Exception) {
                ivMergedFile.setImageResource(R.drawable.pdf_icon_history)
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
                .inflate(R.layout.merged_file_selection_item, parent, false)
        )
        return view
    }

    override fun onBindViewHolder(
        holder: MergedViewHolder,
        position: Int
    ) {
        holder.bind(list[position])
        holder.itemView.setOnClickListener {
            if (holder.ivSplitPDFIconCheck.isVisible) {
                holder.ivSplitPDFIconCheck.visibility = View.GONE
            } else {
                holder.ivSplitPDFIconCheck.visibility = View.VISIBLE
            }
            itemClick.onItemClick(position)
        }
    }

    override fun getItemCount() = list.size

    interface MergeItemClick {
        fun onItemClick(position: Int)
    }

    fun onMergeItemClick(onItemClick: MergeItemClick) {
        this.itemClick = onItemClick
    }
}