package com.example.pdfscanner.fragments.subfragments

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.emreesen.sntoast.SnToast
import com.emreesen.sntoast.Type
import com.example.pdfscanner.databinding.FragmentExtractTextBinding
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.pdfscanner.R
import com.example.pdfscanner.db.DatabaseViewModel
import com.example.pdfscanner.db.ImageData
import com.example.pdfscanner.db.PDFData
import com.example.pdfscanner.utils.Utils.isPremiumUser
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.getValue


class ExtractTextFragment : Fragment() {

    private lateinit var binding: FragmentExtractTextBinding
    private lateinit var extractedText: String
    private lateinit var imageTExtract: String
    private lateinit var bitmap: Bitmap
    private var interstitialAd: InterstitialAd? = null
    private val viewModel: DatabaseViewModel by viewModels {
        DatabaseViewModel.DatabaseViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExtractTextBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(requireContext().isPremiumUser()){
            binding.adView.visibility = View.GONE
        } else {
            MobileAds.initialize(requireActivity())
            InterstitialAd.load(
                requireActivity(),
                resources.getString(R.string.interstitial_id),
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        interstitialAd = null
                    }
                }
            )
            MobileAds.initialize(requireContext()) {}
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
            binding.adView.visibility = View.VISIBLE
        }

        extractedText = arguments?.getString("extractedText").toString()
        imageTExtract = arguments?.getString("image").toString()
        val b = Base64.decode(imageTExtract, Base64.DEFAULT)
        bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
        binding.ivImageToExtract.setImageBitmap(bitmap)
        binding.tvExtractedText.text = extractedText
        binding.ivBack.setOnClickListener {
            if (requireContext().isPremiumUser()) {
                findNavController().popBackStack()
            } else {
                showInterstitial()
            }
//            findNavController().popBackStack()
        }
        binding.rlSwitch.setOnClickListener {
            if (binding.ivImageToExtract.isVisible) {
                binding.ivImageToExtract.visibility = View.GONE
                binding.svExtractedText.visibility = View.VISIBLE
            } else {
                binding.ivImageToExtract.visibility = View.VISIBLE
                binding.svExtractedText.visibility = View.GONE
            }
        }
        binding.rlCopy.setOnClickListener {
            copyToClipboard(extractedText)
        }
        binding.rlSave.setOnClickListener {
            showFileNameDialog(1)
        }
        binding.rlShare.setOnClickListener {
            shareText(extractedText)
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (requireContext().isPremiumUser()) {
                    findNavController().popBackStack()
                } else {
                    showInterstitial()
                }
//                findNavController().popBackStack()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }

    private fun showInterstitial() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                findNavController().popBackStack()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                findNavController().popBackStack()
            }
        }

        if (interstitialAd != null) {
            interstitialAd?.show(requireActivity())
        } else {
            findNavController().popBackStack()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard: ClipboardManager =
            requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("extractedText", text)
        clipboard.setPrimaryClip(clip)
        SnToast.Builder()
            .context(requireContext())
            .type(Type.SUCCESS)
            .message("Text Copied Successfully !") //.cancelable(false or true) Optional Default: False
            // .iconSize(int size) Optional Default: 34dp
            // .textSize(int size) Optional Default 18sp
            // .animation(false or true) Optional Default: True
            // .duration(int ms) Optional Default: 3000ms
            // .backgroundColor(R.color.example) Default: It is filled according to the toast type. If an assignment is made, the assigned value is used
            // .icon(R.drawable.example) Default: It is filled according to the toast type. If an assignment is made, the assigned value is used
            .build()
    }

    private fun showFileNameDialog(pos: Int) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.file_name_dialog)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        val edtFileName = dialog.findViewById<EditText>(R.id.edtPDFFileName)
        edtFileName.setText(System.currentTimeMillis().toString())

        val btnSave: Button = dialog.findViewById<Button>(R.id.btnSave)
        val btnCancel: Button = dialog.findViewById<Button>(R.id.btnCancel)

        btnSave.setOnClickListener {
            when (pos) {
                1 -> {
                    saveFile(edtFileName.text.toString(), 1)
                }

                2 -> {

                }

                3 -> {

                }
            }

            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
//            binding.spinKit.visibility = View.GONE
        }
        dialog.show()
    }

    private fun showDialog() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.scanned_document_save_dialog)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        val edtFileName = dialog.findViewById<EditText>(R.id.edtPDFFileName)
        edtFileName.setText(System.currentTimeMillis().toString())
        val rlJpg: RelativeLayout = dialog.findViewById<RelativeLayout>(R.id.rlJpg)
//        val tvImagePath: TextView = dialog.findViewById<TextView>(R.id.tvImagePath)
        val cbJpg: CheckBox = dialog.findViewById<CheckBox>(R.id.cbJpg)
        val cbPdf: CheckBox = dialog.findViewById<CheckBox>(R.id.cbPdf)
        cbPdf.text = resources.getString(R.string.text_extension)

        rlJpg.visibility = View.GONE
//        tvImagePath.visibility = View.GONE

        val btnSave: Button = dialog.findViewById<Button>(R.id.btnSave)
        val btnCancel: Button = dialog.findViewById<Button>(R.id.btnCancel)

        btnSave.setOnClickListener {
            if (cbJpg.isChecked && cbPdf.isChecked) {
                saveFile(edtFileName.text.toString(), 3)
                dialog.dismiss()
            } else if (cbJpg.isChecked) {
                saveFile(edtFileName.text.toString(), 2)
                dialog.dismiss()

            } else if (cbPdf.isChecked) {
                saveFile(edtFileName.text.toString(), 1)
                dialog.dismiss()
            } else {
                SnToast.Builder()
                    .context(requireContext())
                    .type(Type.ERROR)
                    .message("Please Select Type to Save") //.cancelable(false or true) Optional Default: False
                    .build()
            }

        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun shareText(text: String) {

        /*Create an ACTION_SEND Intent*/
        val intent = Intent(Intent.ACTION_SEND)

        /*The type of the content is text, obviously.*/
        intent.setType("text/plain")

        /*Applying information Subject and Body.*/
        intent.putExtra(Intent.EXTRA_TEXT, text)

        /*Fire!*/
        startActivity(Intent.createChooser(intent, "Share Via"))
    }

    private fun saveFile(fileName: String, type: Int) {

        val fileNameFinal = "$fileName.txt"
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "/pdfscanner/documents/extracted/"
        )
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(
            directory,
            fileNameFinal
        )

        val imageFileName = "$fileName.jpg"
        val image = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            imageFileName
        )

        val bundle = Bundle()
        val listFile = ArrayList<String>()
        var listName = ArrayList<String>()
        when (type) {
            1 -> {
                val fileWriter = FileWriter(file)
                val out = BufferedWriter(fileWriter)
                out.write(extractedText)
                out.close()

                listFile.add(file.toString())
                listName.add(fileNameFinal)
                bundle.putStringArrayList("uri", listFile)
                bundle.putStringArrayList("fileName", listName)
                val pdfData = PDFData(
                    "txt",
                    System.currentTimeMillis().toString(),
                    fileNameFinal,
                    file.toString(), false
                )
                viewModel.insertPDFRecord(pdfData)
            }

            2 -> {
                val outputStream = FileOutputStream(image)
                val outputStreamSave = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                try {
                    outputStream.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                outputStream.close()
                listFile.add(image.toString())
                listName.add(imageFileName)
                bundle.putStringArrayList("uri", listFile)
                bundle.putStringArrayList("fileName", listName)

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStreamSave)
                val b = outputStreamSave.toByteArray()
                val imageData =
                    ImageData(
                        "jpg",
                        System.currentTimeMillis().toString(),
                        imageFileName,
                        image.toString(),
                        false
                    )
                imageData.let { viewModel.insetRecord(it) }
            }

            3 -> {
                val fileWriter = FileWriter(file)
                val out = BufferedWriter(fileWriter)
                out.write(extractedText)
                out.close()

                val outputStream = FileOutputStream(image)
                val outputStreamSave = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                try {
                    outputStream.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                outputStream.close()
                listFile.add(file.toString())
                listFile.add(image.toString())
                listName.add(fileNameFinal)
                listName.add(imageFileName)
                bundle.putStringArrayList("uri", listFile)
                bundle.putStringArrayList("fileName", listName)

                val pdfData = PDFData(
                    "txt",
                    System.currentTimeMillis().toString(),
                    fileNameFinal,
                    file.toString(), false
                )
                viewModel.insertPDFRecord(pdfData)

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStreamSave)
                val b = outputStreamSave.toByteArray()
                val imageData =
                    ImageData(
                        "jpg",
                        System.currentTimeMillis().toString(),
                        imageFileName,
                        image.toString(),
                        false
                    )
                imageData.let { viewModel.insetRecord(it) }
            }
        }

        findNavController().navigate(R.id.action_extractTextFragment_to_actionFragment, bundle)

//        SnToast.Builder()
//            .context(requireContext())
//            .type(Type.ERROR)
//            .message("File Saved Successfully !") //.cancelable(false or true) Optional Default: False
//            .build()
    }

}