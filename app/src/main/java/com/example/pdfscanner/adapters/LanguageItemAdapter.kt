package com.example.pdfscanner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfscanner.R
import com.example.pdfscanner.db.LanguageItem

class LanguageItemAdapter(
    private val languageItem: ArrayList<LanguageItem>
) :
    RecyclerView.Adapter<LanguageItemAdapter.LanguageItemViewHolder>() {

    private var selectedPosition = -1

    private var onClickListener: OnClickListener? = null

    inner class LanguageItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val languageIcon = view.findViewById<ImageView>(R.id.language_icon)
        private val languageName = view.findViewById<TextView>(R.id.language_name)
        val checkBtn = view.findViewById<ImageView>(R.id.checkBtn)!!

        fun bind(languageItem: LanguageItem) {
            languageIcon.setImageResource(languageItem.icon)
            languageName.text = languageItem.name
        }
    }

    fun setSingleSelection(adapterPosition: Int) {
        if (adapterPosition == RecyclerView.NO_POSITION) return
        notifyItemChanged(selectedPosition)
        selectedPosition = adapterPosition
        notifyItemChanged(selectedPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageItemViewHolder {
        return LanguageItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.language_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return languageItem.size
    }

    override fun onBindViewHolder(holder: LanguageItemViewHolder, position: Int) {
        val item = languageItem[position]
        holder.bind(languageItem[position])
        if (selectedPosition == position) {
            holder.checkBtn.setImageResource(R.drawable.radio_button_checked)
        } else {
            holder.checkBtn.setImageResource(R.drawable.radio_button_unchecked)
        }

        holder.itemView.setOnClickListener {
            onClickListener?.onClick(position, item)
            setSingleSelection(position)
        }
    }

    interface OnClickListener {
        fun onClick(position: Int, model: LanguageItem?)
    }

    // Setter for the click listener
    fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener!!
    }


}