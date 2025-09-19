package com.example.pdfscanner.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.pdfscanner.R
import com.google.android.material.bottomsheet.BottomSheetDialog

object BottomSheetDialog {

    @SuppressLint("MissingInflatedId")
    fun openCameraGallery(context: Context): ArrayList<RelativeLayout> {
        // on below line we are creating a new bottom sheet dialog.
        val dialog = BottomSheetDialog(context)

        // on below line we are inflating a layout file which we have created.
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dialog, null)
        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()

        val rlBottomSheetCamera: RelativeLayout = view.findViewById<RelativeLayout>(R.id.rlBottomSheetCamera)
        val rlBottomSheetGallery: RelativeLayout = view.findViewById<RelativeLayout>(R.id.rlBottomSheetGallery)
        var btnList = ArrayList<RelativeLayout>()
        rlBottomSheetCamera.setOnClickListener {
            btnList.add(rlBottomSheetCamera)
            dialog.dismiss()
        }
        rlBottomSheetGallery.setOnClickListener {
            btnList.add(rlBottomSheetGallery)
        }
        btnList.add(rlBottomSheetGallery)
        return btnList
    }

}