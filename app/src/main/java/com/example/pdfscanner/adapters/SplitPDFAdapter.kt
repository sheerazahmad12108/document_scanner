package com.example.pdfscanner.adapters

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfscanner.R
import java.io.File
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible

class SplitPDFAdapter(private var list: ArrayList<String>) :
    RecyclerView.Adapter<SplitPDFAdapter.SplitViewHolder>() {
    private lateinit var itemClick: SplitItemClick
    val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    val cacheSize = maxMemory / 8
    val thumbnailCache: LruCache<String, Bitmap> = LruCache(cacheSize)

    inner class SplitViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val ivSplitPDFIcon: ImageView = view.findViewById(R.id.ivSplitIcon)
        val ivSplitPDFIconCheck: ImageView = view.findViewById(R.id.ivSplitSelect)

        fun bind(path: String) {
            try {
                var thumbnail = thumbnailCache.get(path)
                if (thumbnail == null) {
                    thumbnail = generateThumbnail(path)
                    if (true) {
                        thumbnailCache.put(path, thumbnail)
                    }
                }

                if (true) {
                    ivSplitPDFIcon.setImageBitmap(thumbnail)
                } else {
                    ivSplitPDFIcon.setImageResource(R.drawable.pdf_icon_history)
                }


            } catch (_: Exception) {
                ivSplitPDFIcon.setImageResource(R.drawable.pdf_icon_history)
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
    ): SplitViewHolder {
        val view = SplitViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.split_pdf_item, parent, false)
        )
        return view
    }

    override fun onBindViewHolder(
        holder: SplitViewHolder,
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

    interface SplitItemClick {
        fun onItemClick(position: Int)
    }

    fun onSplitItemClick(onItemClick: SplitItemClick) {
        this.itemClick = onItemClick
    }
}