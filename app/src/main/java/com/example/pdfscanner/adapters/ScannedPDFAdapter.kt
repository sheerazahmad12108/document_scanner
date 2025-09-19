package com.example.pdfscanner.adapters

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.graphics.createBitmap
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfscanner.R
import com.example.pdfscanner.db.PDFData
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class ScannedPDFAdapter : RecyclerView.Adapter<ScannedPDFAdapter.ViewHolder>() {

    private var dataList = ArrayList<PDFData>()
    private lateinit var onClick: OnItemClick

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    val thumbnailCache: LruCache<String, Bitmap> = LruCache(cacheSize)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.history_item_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(dataList[position])
        holder.llItemClick.setOnClickListener {
            onClick.onItemClick(position)
        }
        holder.rlMore.setOnClickListener {
            onClick.onItemClickMore(position, dataList[position])
        }
    }

    override fun getItemCount() = dataList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivHistoryIcon: ImageView = view.findViewById<ImageView>(R.id.ivHistoryIcon)
        val rlMore: RelativeLayout = view.findViewById<RelativeLayout>(R.id.rlMore)
        val llItemClick: LinearLayout = view.findViewById<LinearLayout>(R.id.llItemClick)
        private val tvHistoryName: TextView = view.findViewById<TextView>(R.id.tvHistoryName)
        private val tvHistoryDate: TextView = view.findViewById<TextView>(R.id.tvHistoryDate)

        fun bind(data: PDFData) {
            tvHistoryName.text = data.name
            val date = Date(data.dateCreated.toLong())
            val format = SimpleDateFormat("yyyy/MM/dd HH:mm")
            val finalDate = format.format(date)
            tvHistoryDate.text = finalDate
            if (data.name.substringAfter(".") == "pdf") {
                try {
                    var thumbnail = thumbnailCache.get(data.path)
                    if (thumbnail == null) {
                        thumbnail = generateThumbnail(data.path)
                        if (true) {
                            thumbnailCache.put(data.path, thumbnail)
                        }
                    }

                    if (true) {
                        ivHistoryIcon.setImageBitmap(thumbnail)
                    } else {
                        ivHistoryIcon.setImageResource(R.drawable.pdf_icon_history)
                    }


                } catch (_: Exception) {
                    ivHistoryIcon.setImageResource(R.drawable.pdf_icon_history)
                }
            } else {
                ivHistoryIcon.setImageResource(R.drawable.text_icon)
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

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<PDFData>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    interface OnItemClick {
        fun onItemClick(position: Int)
        fun onItemClickMore(position: Int, item: PDFData)
    }

    fun onClick(onItemClick: OnItemClick) {
        this.onClick = onItemClick
    }
}