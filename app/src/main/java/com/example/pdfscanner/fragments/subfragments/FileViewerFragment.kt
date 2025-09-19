package com.example.pdfscanner.fragments.subfragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pdfscanner.R
import com.example.pdfscanner.databinding.FragmentFileViewerBinding
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import kotlin.text.substringAfter


class FileViewerFragment : Fragment() {

    private lateinit var binding: FragmentFileViewerBinding
    private lateinit var fileName: String
    private lateinit var filePath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFileViewerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window: Window? = requireActivity().window
        window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        WindowInsetsControllerCompat(window!!, window.decorView).isAppearanceLightStatusBars = true

        fileName = arguments?.getString("fileName", "").toString()
        filePath = arguments?.getString("uri", "").toString()
        binding.tvTitle.text = fileName

        val extension = fileName.substringAfter(".")
        val file = File(filePath)
        if(file.exists()){
            if (extension == "pdf") {
                binding.customPdfView.visibility = View.VISIBLE
                binding.customPdfView
                    .fromFile(file)
//                    .enableSwipe(true)
                    .swipeHorizontal(false)
//                    .enableDoubletap(true)
                    .defaultPage(0)
                    .nightMode(false)
                    .enableAnnotationRendering(true)
                    .password(null)
                    .scrollHandle(DefaultScrollHandle(requireContext()))
                    .enableAntialiasing(true)
                    .pageFitPolicy(FitPolicy.HEIGHT)
                    .spacing(100)
                    .load()
            } else if (extension == "txt") {
                binding.tvFile.visibility = View.VISIBLE
                val text = StringBuilder()

                try {
                    val br = BufferedReader(FileReader(file))
                    var line: String?

                    //read line by line
                    while ((br.readLine().also { line = it }) != null) {
                        text.append(line)
                        text.append('\n')
                    }
                } catch (_: IOException) {
                    //You'll need to add proper error handling here
                }

                binding.tvFile.text = text
            } else {
                binding.ivImage.visibility = View.VISIBLE
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().contentResolver,
                    Uri.fromFile(File(filePath))
                )
                binding.ivImage.setImageBitmap(bitmap)
            }
        } else {
            Toast.makeText(requireContext(),"File Not Found", Toast.LENGTH_SHORT).show()
        }


        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}