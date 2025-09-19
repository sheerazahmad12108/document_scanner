package com.example.pdfscanner.adapters

import android.R.attr.path
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfscanner.R
import com.example.pdfscanner.db.ImageData
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class ScannedDataAdapter : RecyclerView.Adapter<ScannedDataAdapter.ViewHolder>() {

    private var dataList = ArrayList<ImageData>()
    private lateinit var onItemClick: OnItemClick
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
            onItemClick.onItemClick(position)
        }
        holder.rlMore.setOnClickListener {
            onItemClick.onItemClickMore(position,dataList[position])
        }
    }

    override fun getItemCount() = dataList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivHistoryIcon: ImageView = view.findViewById<ImageView>(R.id.ivHistoryIcon)
        val rlMore: RelativeLayout = view.findViewById<RelativeLayout>(R.id.rlMore)
        val tvHistoryName: TextView = view.findViewById<TextView>(R.id.tvHistoryName)
        val tvHistoryDate: TextView = view.findViewById<TextView>(R.id.tvHistoryDate)
        val llItemClick: LinearLayout = view.findViewById<LinearLayout>(R.id.llItemClick)

        fun bind(data: ImageData) {
            tvHistoryName.text = data.name
            val date = Date(data.dateCreated.toLong())
            val format = SimpleDateFormat("yyyy/MM/dd HH:mm")
            val finalDate = format.format(date)
            tvHistoryDate.text = finalDate

            val imgFile: File = File(data.path)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath())
                ivHistoryIcon.setImageBitmap(bitmap)
            } else {
                Log.e("Image", "Image file does not exist at: " + path)
            }

//            val bitmap = BitmapFactory.decodeByteArray(data.data, 0, data.data.size)
//            ivHistoryIcon.setImageBitmap(bitmap)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<ImageData>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    interface OnItemClick{
        fun onItemClick(position: Int)
        fun onItemClickMore(position: Int, item: ImageData)
    }

    fun onClick(onItemClick: OnItemClick){
        this.onItemClick = onItemClick
    }
}