package com.example.pdfscanner.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.canhub.cropper.CropImageView
import com.emreesen.sntoast.SnToast
import com.emreesen.sntoast.Type
import com.example.pdfscanner.R
import com.example.pdfscanner.adapters.MergedFilesAdapter
import com.example.pdfscanner.adapters.ScannedPDFAdapter
import com.example.pdfscanner.adapters.SplitPDFAdapter
import com.example.pdfscanner.databinding.DialogDeleteFilesBinding
import com.example.pdfscanner.databinding.FragmentHomeBinding
import com.example.pdfscanner.db.DatabaseViewModel
import com.example.pdfscanner.db.ImageData
import com.example.pdfscanner.db.PDFData
import com.example.pdfscanner.model.PdfFile
import com.example.pdfscanner.utils.Utils
import com.example.pdfscanner.utils.Utils.isPremiumUser
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfEncryptor
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    private lateinit var photoUri: Uri
    private var bitmapImageToPdf: Bitmap? = null
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var selectPictureLauncher: ActivityResultLauncher<String>
    private lateinit var recognizer: TextRecognizer
    private var mSelectedPdfs = ArrayList<Uri>()
    private var mSelectedPdfsSplit = ArrayList<Uri>()
    private var mSelectedPdfLock = ArrayList<Uri>()
    private var scannedImages = ArrayList<String>()
    private lateinit var scannedPdf: Uri

    private val pdfList = mutableListOf<File>()
    private var encodedBitmap: String = ""
    private var visionText: String = ""
    private lateinit var loader: Dialog

    private val REQUEST_IMAGE_CAPTURE = 100
    private var imageToPdf: Boolean = false

    private var menuOpen = false
    private lateinit var adapter: ScannedPDFAdapter
    private lateinit var dataList: java.util.ArrayList<PDFData>

    private var interstitialAd: InterstitialAd? = null
    private var reviewShown = false

    private val pdfFiles = mutableListOf<PdfFile>()
    private lateinit var mergedFilesAdapter: MergedFilesAdapter

    private val viewModel: DatabaseViewModel by viewModels {
        DatabaseViewModel.DatabaseViewModelFactory(requireContext())
    }

    private val scannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val result = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                scannedImages.clear()
                result?.pages?.let { pages ->
                    for (page in pages) {
                        scannedImages.add(page.imageUri.toString())
                    }
                }
                result?.pdf?.let { pdf ->
                    scannedPdf = pdf.uri
                }
                showDialog()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window: Window? = requireActivity().window
        window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.status_bar)

        WindowInsetsControllerCompat(window!!, window.decorView).isAppearanceLightStatusBars = true

        loader = Utils.showLoader(requireContext())
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        if (requireContext().isPremiumUser()) {
            binding.adViewFirst.visibility = View.GONE
        } else {
            InterstitialAd.load(
                requireContext(),
                resources.getString(R.string.interstitial_id),
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d("AdMobsLogs", adError.toString())
                        interstitialAd = null
                    }
                }
            )

            MobileAds.initialize(requireContext()) {}
            val adRequest = AdRequest.Builder().build()
            binding.adViewFirst.loadAd(adRequest)
            binding.adViewFirst.visibility = View.VISIBLE
        }

        binding.llScanDocument.setOnClickListener {
            scannerDocument(10)
        }

        binding.llExtractText.setOnClickListener {
//            openCameraGallery()
            if(requireContext().isPremiumUser()){
                openCameraGalleryNew()
            } else {
                findNavController().navigate(R.id.action_navigation_home_to_purchaseFragment)
            }
        }

        binding.ivSettings.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_settings)
        }

        binding.fabMain.setOnClickListener {
            toggleFabMenu()
        }

        binding.llPdfMerge.setOnClickListener {
            loader.show()
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    searchPDFToMerge()
                    if (pdfList.isNotEmpty()) {
                        for (file in pdfList) {
                            pdfFiles.add(
                                PdfFile(
                                    file.name, file.path, 123, file.lastModified(), file.extension
                                )
                            )
                        }
                    }
                    mergedFilesAdapter = MergedFilesAdapter(pdfFiles as ArrayList)
                }
                withContext(Dispatchers.Main) {
                    openMergeDialog()
                    delay(5000)
                    loader.dismiss()
                }
            }
        }

        binding.llSplitPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.setType("application/pdf")
            startActivityForResult(intent, 202)
        }

        binding.llPdfLock.setOnClickListener {
            if (requireContext().isPremiumUser()) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.setType("application/pdf")
                startActivityForResult(intent, 303)
            } else {
                findNavController().navigate(R.id.action_navigation_home_to_purchaseFragment)
            }

        }

        binding.llSignature.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_signatureFragment)
        }

        binding.llScanIdCard.setOnClickListener {
            scannerDocument(2)
        }

        setupActivityResultLaunchers()
        setAdapter()
        viewModel.allPDFRecords.observe(viewLifecycleOwner) { data ->
            data?.let {
                dataList.clear()
                dataList.addAll(data)

                val fullList = dataList // your full list
                val limitedList = if (fullList.size >= 2) fullList.subList(0, 2) else fullList

                adapter.updateList(limitedList)

//                adapter.updateList(data)
                if (data.isNotEmpty()) {
                    binding.ivEmpty.visibility = View.GONE
                    binding.rvRecent.visibility = View.VISIBLE
                } else {
                    binding.ivEmpty.visibility = View.VISIBLE
                    binding.rvRecent.visibility = View.GONE
                }
                binding.spinKit.visibility = View.GONE
            }
        }
        binding.tvAllFiles.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_history)
//            showInterstitial()
        }

        binding.fabCamera.setOnClickListener {
            scannerDocument(10)
        }

        binding.fabGallery.setOnClickListener {
//            scannerDocument(10)
            imageToPdf = true
            dispatchSelectPictureIntent()
        }

        binding.llImageToPDF.setOnClickListener {
            imageToPdf = true
            openCameraGalleryNew()
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!reviewShown) {
                    showInAppReview()
                } else {
//                    requireActivity().finish()  // Exit app if already shown
                    showExitPopup()  // Exit app if already shown
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }

    private fun dispatchTakePictureIntent() {
        val imageFile = File.createTempFile("IMG_", ".jpg", requireContext().cacheDir)
        photoUri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.provider", imageFile
        )
        takePictureLauncher.launch(photoUri)
    }

    private fun dispatchSelectPictureIntent() {
        selectPictureLauncher.launch("image/*")
    }

    private fun processImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val b = outputStream.toByteArray()
        encodedBitmap = Base64.encodeToString(b, Base64.DEFAULT)
        recognizer.process(image).addOnSuccessListener { visionText ->
            this.visionText = visionText.text
            Log.d("vision_text", visionText.text)
//            val bundle = Bundle()
//            bundle.putString("extractedText", visionText.text)
//            bundle.putString("image", encodedBitmap)
//            findNavController().navigate(
//                R.id.action_navigation_home_to_extractTextFragment, bundle
//            )
        }.addOnFailureListener { e ->
            // Hide at launch
//                binding.progressBar.visibility = View.GONE
//            binding.spinKit.visibility = View.GONE
            if (e is MlKitException && e.errorCode == MlKitException.UNAVAILABLE) {
                // The model is still downloading. Inform the user accordingly.
//                    showMessage("Text recognition model is still downloading")
                Toast.makeText(
                    requireContext(),
                    "Text recognition model is still downloading",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
//                    showMessage("Text recognition failed: ${e.message}")
                Toast.makeText(
                    requireContext(), "Text recognition failed", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupActivityResultLaunchers() {
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {

                    requireContext().contentResolver.openInputStream(photoUri)?.use { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        bitmapImageToPdf = bitmap
//                        processImage(bitmap)
                        if (imageToPdf) {
                            showFileNameDialog(2)
                        } else {
                            showCropDialog(bitmap)
                        }
                    }
                }
            }

        selectPictureLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    requireContext().contentResolver.openInputStream(it)?.use { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        bitmapImageToPdf = bitmap
                        if (imageToPdf) {
                            showFileNameDialog(2)
                        } else {

                            showCropDialog(bitmap)
                        }
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK) {
//            binding.spinKit.visibility = View.VISIBLE
            loader.show()
            if (data?.data != null) {
                mSelectedPdfs.add(data.data!!)
            } else if (data?.clipData != null) {
                for (i in 0..data.clipData!!.itemCount - 1) {
                    mSelectedPdfs.add(data.clipData!!.getItemAt(i).uri)
                }
            }
            showFileNameDialog(1)
        }

        if (requestCode == 202 && resultCode == RESULT_OK) {
            mSelectedPdfsSplit.clear()
            if (data?.data != null) {
                mSelectedPdfsSplit.add(data.data!!)
            } else if (data?.clipData != null) {
                for (i in 0..data.clipData!!.itemCount - 1) {
                    mSelectedPdfsSplit.add(data.clipData!!.getItemAt(i).uri)
                }
            }
//            showFileNameDialog(2)
            splitPdf()
        }

        if (requestCode == 303 && resultCode == RESULT_OK) {
            mSelectedPdfLock.clear()
            if (data?.data != null) {
                mSelectedPdfLock.add(data.data!!)
            } else if (data?.clipData != null) {
                for (i in 0..data.clipData!!.itemCount - 1) {
                    mSelectedPdfLock.add(data.clipData!!.getItemAt(i).uri)
                }
            }
            showPasswordSetDialog()
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val extras = data?.extras
            val imageBitmap = extras?.get("data") as? Bitmap
            showCropDialog(imageBitmap!!)
//            processImage(imageBitmap!!)
////            imageView.setImageBitmap(imageBitmap)
        }
    }

    private fun scannerDocument(pageLimit: Int) {
        val options = GmsDocumentScannerOptions.Builder().setGalleryImportAllowed(true)
            .setPageLimit(pageLimit).setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL).build()

        val scanner = GmsDocumentScanning.getClient(options)
        scanner.getStartScanIntent(requireActivity()).addOnSuccessListener { intentSender ->
            scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pdfMerge(name: String) {
        if (mSelectedPdfs.isEmpty()) {
            SnToast.Builder().context(requireContext()).type(Type.ERROR)
                .message("Please Select Pdf !").build()
        } else {
            val bundle = Bundle()
            val listFile = ArrayList<String>()
            val listName = ArrayList<String>()
            try {

                val fileName: String = name + "_merged.pdf"
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "/pdfscanner/documents/merged/"
                )
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val mergedPdfFiles = File(
                    directory, fileName
                )
                val outputStream = FileOutputStream(mergedPdfFiles)
                val document = Document()
                val copy = PdfCopy(document, outputStream)
                document.open()

                for (pdf in mSelectedPdfs) {
                    val reader = PdfReader(requireContext().contentResolver.openInputStream(pdf))
                    copy.addDocument(reader)
                    reader.close()
                }

                document.close()
                outputStream.close()
                val pdfData = PDFData(
                    "pdf",
                    System.currentTimeMillis().toString(),
                    fileName,
                    mergedPdfFiles.toString(),
                    false
                )
                viewModel.insertPDFRecord(pdfData)
//                binding.spinKit.visibility = View.GONE
                loader.dismiss()

                listName.add(fileName)
                listFile.add(mergedPdfFiles.toString())
                bundle.putStringArrayList("uri", listFile)
                bundle.putStringArrayList("fileName", listName)
                SnToast.Builder().context(requireContext()).type(Type.SUCCESS)
                    .message("Pdf Files Merged Successfully !") //.cancelable(false or true) Optional Default: False
                    .build()
                lifecycleScope.launch {
                    delay(2000)
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
//                    showInterstitial(R.id.action_navigation_home_to_actionFragment, bundle)
//                    showInterstitial()
                    findNavController().navigate(
                        R.id.action_navigation_home_to_actionFragment, bundle
                    )
                }

            } catch (_: Exception) {
//                binding.spinKit.visibility = View.GONE
                loader.dismiss()
            }
        }
    }

    private fun openCameraGalleryNew() {
        val view = layoutInflater.inflate(R.layout.bottom_sheet_camera_gallery, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)

        // Ensure rounded corners are respected
        (view.parent as View).setBackgroundResource(android.R.color.transparent)
        view.findViewById<LinearLayout>(R.id.btnCamera).setOnClickListener {
            // handle camera click
            dispatchTakePictureIntent()
            dialog.dismiss()
        }

        view.findViewById<LinearLayout>(R.id.btnGallery).setOnClickListener {
            // handle gallery click
            dispatchSelectPictureIntent()
            dialog.dismiss()
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

        val radioGroupOptions: RadioGroup = dialog.findViewById(R.id.radioGroupOptions)
        val btnSave: Button = dialog.findViewById(R.id.btnSave)
        val btnCancel: Button = dialog.findViewById(R.id.btnCancel)

        btnSave.setOnClickListener {
            val selectedId = radioGroupOptions.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedRadioButton = dialog.findViewById<RadioButton>(selectedId)
//                Toast.makeText(
//                    requireContext(),
//                    "Selected: ${selectedRadioButton.text}",
//                    Toast.LENGTH_SHORT
//                ).show()
                if (selectedRadioButton.text.toString() == resources.getString(R.string.both)) {
                    saveFile(edtFileName.text.toString(), 3)
                    dialog.dismiss()
                } else if (selectedRadioButton.text.toString() == resources.getString(R.string.jpg)) {
                    saveFile(edtFileName.text.toString(), 2)
                    dialog.dismiss()
                } else if (selectedRadioButton.text.toString() == resources.getString(R.string.pdf)) {
                    saveFile(edtFileName.text.toString(), 1)
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(requireContext(), "Please select an option", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun splitPdf() {

        try {
            val reader =
                PdfReader(requireContext().contentResolver.openInputStream(mSelectedPdfsSplit[0]))
            val count = reader.numberOfPages
            val pdfList = ArrayList<ByteArray?>()
            for (currentPage in 1..count) {
                ByteArrayOutputStream().use { byteArrayOutputStream ->
                    try {
                        val document = Document(reader.getPageSizeWithRotation(currentPage))
                        val pdfCopy = PdfCopy(document, byteArrayOutputStream)
                        document.open()
                        pdfCopy.addPage(pdfCopy.getImportedPage(reader, currentPage))
                        document.close()
                        pdfCopy.close()
                        pdfList.add(byteArrayOutputStream.toByteArray())
                    } catch (_: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Selected File is Password Protected",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            if (pdfList.size > 1) {
                showSplitFileDialog(pdfList)
            } else {
                Toast.makeText(requireContext(), "File contains only 1 page", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "Bad File", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setPassword(password: String, name: String) {

        val fileName: String = name + "_protected.pdf"
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "/pdfscanner/documents/locked/"
        )
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val passwordPdfFiles = File(
            directory, fileName
        )

        val reader =
            PdfReader(requireContext().contentResolver.openInputStream(mSelectedPdfLock[0]))
        PdfEncryptor.encrypt(
            reader,
            passwordPdfFiles.outputStream(),
            true,
            password,
            password,
            PdfWriter.ALLOW_SCREENREADERS
        )

        if (requireContext().isPremiumUser()) {
            SnToast.Builder().context(requireContext()).type(Type.SUCCESS)
                .message("Password Set Successfully !") //.cancelable(false or true) Optional Default: False
                .build()
        } else {
            showInterstitial()
        }
//        SnToast.Builder().context(requireContext()).type(Type.SUCCESS)
//            .message("Password Set Successfully !") //.cancelable(false or true) Optional Default: False
//            .build()

    }

    @SuppressLint("MissingInflatedId")
    private fun showPasswordSetDialog() {
        val dialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_password, null)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val edtName: EditText = view.findViewById(R.id.edtName)
        val edtPassword: EditText = view.findViewById(R.id.edtPassword)

        edtName.setText(System.currentTimeMillis().toString())

        btnSave.setOnClickListener {
            dialog.dismiss()
            setPassword(edtPassword.text.toString().trim(), edtName.text.toString().trim())
        }
        btnCancel.setOnClickListener {
            mSelectedPdfLock.clear()
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun saveFile(fileName: String, type: Int) {
//        binding.spinKit.visibility = View.VISIBLE
        loader.show()
        val fileNameFinal = "$fileName.pdf"
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "/pdfscanner/documents/scanned/"
        )
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(
            directory, fileNameFinal
        )

        var count = 0
        val bundle = Bundle()
        val listFile = ArrayList<String>()
        val listName = ArrayList<String>()
        when (type) {
            1 -> {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        delay(6000)
                        val outputStream = FileOutputStream(file)
                        val document = Document()
                        val copy = PdfCopy(document, outputStream)
                        document.open()
                        val reader =
                            PdfReader(requireContext().contentResolver.openInputStream(scannedPdf))
                        copy.addDocument(reader)
                        reader.close()
                        document.close()
                        outputStream.close()

                        val pdfData = PDFData(
                            "pdf",
                            System.currentTimeMillis().toString(),
                            fileNameFinal,
                            file.toString(),
                            false
                        )
                        viewModel.insertPDFRecord(pdfData)

                        listFile.add(file.toString())
                        listName.add(fileNameFinal)
                        bundle.putStringArrayList("uri", listFile)
                        bundle.putStringArrayList("fileName", listName)
                    }
                    withContext(Dispatchers.Main) {
//                        binding.spinKit.visibility = View.GONE
                        loader.dismiss()
                        bundle.putStringArrayList("uri", listFile)
                        bundle.putStringArrayList("fileName", listName)
//                        showInterstitial(R.id.action_navigation_home_to_actionFragment, bundle)
//                        showInterstitial()
                        findNavController().navigate(
                            R.id.action_navigation_home_to_actionFragment,
                            bundle
                        )
                    }
                }
//                savePDFFile(scannedPdf)
            }

            2 -> {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        delay(6000)
                        for (image in scannedImages) {
                            val imageFileName = "${fileName + "_" + count}.jpg"
                            val directory = File(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                "/pdfscanner/images/"
                            )
                            if (!directory.exists()) {
                                directory.mkdirs()
                            }
                            val file = File(
                                directory, imageFileName
                            )
                            val outputStream = FileOutputStream(file)
                            val outputStreamSave = ByteArrayOutputStream()
                            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                                context?.contentResolver,
                                image.toUri()
                            )
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                            try {
                                outputStream.flush()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            outputStream.close()
                            listFile.add(file.toString())
                            listName.add(imageFileName)
                            bundle.putStringArrayList("uri", listFile)
                            bundle.putStringArrayList("fileName", listName)

                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStreamSave)
                            outputStreamSave.toByteArray()
                            val imageData = ImageData(
                                "jpg",
                                System.currentTimeMillis().toString(),
                                imageFileName,
                                file.toString(),
                                false
                            )
                            imageData.let { viewModel.insetRecord(it) }
                            count++
                        }
                    }
                    withContext(Dispatchers.Main) {
//                        binding.spinKit.visibility = View.GONE
                        loader.dismiss()
                        bundle.putStringArrayList("uri", listFile)
                        bundle.putStringArrayList("fileName", listName)
//                        showInterstitial()
                        findNavController().navigate(
                            R.id.action_navigation_home_to_actionFragment,
                            bundle
                        )
//                        showInterstitial(R.id.action_navigation_home_to_actionFragment, bundle)
                    }
                }

            }

            3 -> {
                lifecycleScope.launch {
//                    savingFilesOnDevice(file,fileName)
                    withContext(Dispatchers.IO) {
                        delay(6000)
                        val outputStream = FileOutputStream(file)
                        val document = Document()
                        val copy = PdfCopy(document, outputStream)
                        document.open()
                        val reader =
                            PdfReader(requireContext().contentResolver.openInputStream(scannedPdf))
                        copy.addDocument(reader)
                        reader.close()
                        document.close()
                        outputStream.close()
                        val pdfData = PDFData(
                            "pdf",
                            System.currentTimeMillis().toString(),
                            fileNameFinal,
                            file.toString(),
                            false
                        )
                        viewModel.insertPDFRecord(pdfData)
                        listName.add(fileNameFinal)
                        listFile.add(file.toString())
                        for (image in scannedImages) {
                            val imageFileName = "${fileName + "_" + count}.jpg"
                            val directory = File(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                "/pdfscanner/images/"
                            )
                            if (!directory.exists()) {
                                directory.mkdirs()
                            }
                            val file = File(
                                directory, imageFileName
                            )
                            val outputStream = FileOutputStream(file)
                            val outputStreamSave = ByteArrayOutputStream()
                            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                                context?.contentResolver, image.toUri()
                            )
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                            try {
                                outputStream.flush()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            outputStream.close()
                            listName.add(imageFileName)
                            listFile.add(file.toString())
                            bitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStreamSave)
                            outputStreamSave.toByteArray()
                            val imageData = ImageData(
                                "jpg",
                                System.currentTimeMillis().toString(),
                                imageFileName,
                                file.toString(),
                                false
                            )
                            imageData.let { viewModel.insetRecord(it) }
                            count++
                        }
                    }
                    withContext(Dispatchers.Main) {
//                        binding.spinKit.visibility = View.GONE
                        loader.dismiss()
                        bundle.putStringArrayList("uri", listFile)
                        bundle.putStringArrayList("fileName", listName)
//                        showInterstitial()
                        findNavController().navigate(
                            R.id.action_navigation_home_to_actionFragment,
                            bundle
                        )
//                        showInterstitial(R.id.action_navigation_home_to_actionFragment, bundle)
                    }
                }
            }
        }

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

        val btnSave: Button = dialog.findViewById(R.id.btnSave)
        val btnCancel: Button = dialog.findViewById(R.id.btnCancel)

        btnSave.setOnClickListener {
            when (pos) {
                1 -> {
                    pdfMerge(edtFileName.text.toString())
                }

                2 -> {
                    var bundle: Bundle
                    lifecycleScope.launch {
                        loader.show()
                        withContext(Dispatchers.IO) {
                            delay(6000)
                            bundle = imageToPDF(edtFileName.text.toString())
                        }
                        withContext(Dispatchers.Main) {
                            loader.dismiss()
//                            showInterstitial()
                            findNavController().navigate(
                                R.id.action_navigation_home_to_actionFragment,
                                bundle
                            )
//                            showInterstitial(R.id.action_navigation_home_to_actionFragment, bundle)
                        }
                    }
                }

                3 -> {

                }
            }

            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
//            binding.spinKit.visibility = View.GONE
            loader.dismiss()
        }
        dialog.show()
    }

    private fun showSplitFileDialog(list: ArrayList<ByteArray?>) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.split_pdf_dialog)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val rvSplit: RecyclerView = dialog.findViewById(R.id.rvSplitPdf)
        val edtFileName = dialog.findViewById<EditText>(R.id.edtPDFFileName)
        edtFileName.setText(System.currentTimeMillis().toString() + "_split")

        val pathList = ArrayList<String>()
        var count = 1
        for (pages in list) {
            val file = requireContext().cacheDir
            val outPutFile = File.createTempFile("${count}_temp", ".pdf", file)
            val os = FileOutputStream(outPutFile)
            os.write(pages)
            os.close()
            pathList.add(outPutFile.toString())
            count++
        }

        rvSplit.setHasFixedSize(true)
        rvSplit.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = SplitPDFAdapter(pathList)
        rvSplit.adapter = adapter
        val clickedItemList = ArrayList<Int>()
        var remove: Boolean
        adapter.onSplitItemClick(object : SplitPDFAdapter.SplitItemClick {
            override fun onItemClick(position: Int) {
                remove = false
                if (clickedItemList.isEmpty()) {
                    clickedItemList.add(position)
                } else {
                    for (x in 0..clickedItemList.size - 1) {
                        if (clickedItemList[x] == position) {
                            remove = true
                            break
//                            clickedItemList.remove(item)
                        }
                    }
                    if (remove) {
                        clickedItemList.remove(position)
                    } else {
                        clickedItemList.add(position)
                    }
                }
            }
        })
        dialog.show()

        val btnSave: Button = dialog.findViewById(R.id.btnSave)
        val btnCancel: Button = dialog.findViewById(R.id.btnCancel)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        btnSave.setOnClickListener {
            if (clickedItemList.isNotEmpty()) {
//                binding.spinKit.visibility = View.VISIBLE
                loader.show()
                val bundle = Bundle()
                val listFile = ArrayList<String>()
                val listName = ArrayList<String>()
                val fileName: String = edtFileName.text.toString() + "_.pdf"
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "/pdfscanner/documents/split/"
                )
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val mergedPdfFiles = File(
                    directory, fileName
                )
                val outputStream = FileOutputStream(mergedPdfFiles)
                val document = Document()
                val copy = PdfCopy(document, outputStream)
                document.open()
                for (x in 0..<clickedItemList.size) {
                    val page = clickedItemList[x]

                    val reader = PdfReader(
                        requireContext().contentResolver.openInputStream(
                            Uri.fromFile(
                                File(pathList[page])
                            )
                        )
                    )
                    copy.addDocument(reader)
                    reader.close()
                }
                document.close()
                outputStream.close()

                val pdfData = PDFData(
                    "pdf",
                    System.currentTimeMillis().toString(),
                    fileName,
                    mergedPdfFiles.toString(),
                    false
                )
                viewModel.insertPDFRecord(pdfData)
                listName.add(fileName.toString())
                listFile.add(mergedPdfFiles.toString())
                bundle.putStringArrayList("uri", listFile)
                bundle.putStringArrayList("fileName", listName)
                dialog.dismiss()
//                binding.spinKit.visibility = View.GONE
                loader.dismiss()
                SnToast.Builder().context(requireContext()).type(Type.SUCCESS)
                    .message("Pdf Files Split Successfully !") //.cancelable(false or true) Optional Default: False
                    .build()
                lifecycleScope.launch {
                    delay(2000)
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
//                    showInterstitial(R.id.action_navigation_home_to_actionFragment, bundle)
//                    showInterstitial()
                    findNavController().navigate(
                        R.id.action_navigation_home_to_actionFragment, bundle
                    )
                }
            } else {
                Toast.makeText(requireContext(), "please select page to split", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

    private fun searchPDFToMerge() {
//        val dialog = BottomSheetDialog(requireContext())
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setCancelable(true)
//        val view =
//            LayoutInflater.from(context).inflate(R.layout.bottom_sheet_merger_file_selection, null)
//        dialog.setContentView(view)
//        var rvMergeFiles: RecyclerView = view.findViewById(R.id.rvMergeFiles)
//        var btnSave: Button = view.findViewById(R.id.btnSave)
//        val btnCancel: Button = view.findViewById<Button>(R.id.btnCancel)

//        val pdfList = mutableListOf<File>()
        val pdfFiles = mutableListOf<PdfFile>()
        val directory = Environment.getExternalStorageDirectory()

        mSelectedPdfs.clear()
        searchForPdfs(directory, pdfList)

        if (pdfList.isNotEmpty()) {
            for (file in pdfList) {
                pdfFiles.add(
                    PdfFile(
                        file.name, file.path, 123, file.lastModified(), file.extension
                    )
                )
            }
        }
//        rvMergeFiles.setHasFixedSize(true)
//        rvMergeFiles.layoutManager = GridLayoutManager(requireContext(), 2)
//        val adapter = MergedFilesAdapter(pdfFiles as ArrayList)
//        rvMergeFiles.adapter = adapter
//
//        var clickedItemList = ArrayList<Int>()
//        var remove = false
//        adapter.onMergeItemClick(object : MergedFilesAdapter.MergeItemClick {
//            override fun onItemClick(position: Int) {
//                remove = false
//                if (clickedItemList.isEmpty()) {
//                    clickedItemList.add(position)
//                } else {
//                    for (x in 0..clickedItemList.size - 1) {
//                        if (clickedItemList[x] == position) {
//                            remove = true
//                            break
////                            clickedItemList.remove(item)
//                        }
//                    }
//                    if (remove) {
//                        clickedItemList.remove(position)
//                    } else {
//                        clickedItemList.add(position)
//                    }
//                }
//                Log.d("selected items size: \n", clickedItemList.size.toString())
//                for (x in 0..clickedItemList.size - 1) {
//                    Log.d("selected items: ", clickedItemList[x].toString())
//                }
//            }
//        })
//
//        btnSave.setOnClickListener {
//            Log.d("Merged Selected Files:", mSelectedPdfs.size.toString())
//            mSelectedPdfs.clear()
//            for (x in clickedItemList) {
//                mSelectedPdfs.add(Uri.fromFile(File(pdfList[x].path)))
//            }
//            if (mSelectedPdfs.size > 1) {
//                showFileNameDialog(1)
//                dialog.dismiss()
//            } else {
//                Toast.makeText(
//                    requireContext(),
//                    "Please select at least 2 files",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//        btnCancel.setOnClickListener {
//            dialog.dismiss()
//        }
//        dialog.show()
//        binding.spinKit.visibility = View.GONE
    }

    private fun openMergeDialog() {
        val dialog = BottomSheetDialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        val view = LayoutInflater.from(context)
            .inflate(R.layout.bottom_sheet_merger_file_selection, null)

        val rvMergeFiles: RecyclerView = view.findViewById(R.id.rvMergeFiles)
        val btnSave: Button = view.findViewById(R.id.btnSave)
        val btnCancel: Button = view.findViewById(R.id.btnCancel)

//        val pdfFiles = mutableListOf<PdfFile>()
        mSelectedPdfs.clear()
//        if (pdfList.isNotEmpty()) {
//            for (file in pdfList) {
//                pdfFiles.add(
//                    PdfFile(
//                        file.name, file.path, 123, file.lastModified(), file.extension
//                    )
//                )
//            }
//        }
        rvMergeFiles.setHasFixedSize(true)
        rvMergeFiles.layoutManager = GridLayoutManager(requireContext(), 2)
//        val adapter = MergedFilesAdapter(pdfFiles as ArrayList)
        rvMergeFiles.adapter = mergedFilesAdapter

        dialog.setContentView(view)
        dialog.show()
//
        val clickedItemList = ArrayList<Int>()
        var remove: Boolean
        mergedFilesAdapter.onMergeItemClick(object : MergedFilesAdapter.MergeItemClick {
            override fun onItemClick(position: Int) {
                remove = false
                if (clickedItemList.isEmpty()) {
                    clickedItemList.add(position)
                } else {
                    for (x in 0..clickedItemList.size - 1) {
                        if (clickedItemList[x] == position) {
                            remove = true
                            break
//                            clickedItemList.remove(item)
                        }
                    }
                    if (remove) {
                        clickedItemList.remove(position)
                    } else {
                        clickedItemList.add(position)
                    }
                }
                Log.d("selected items size: \n", clickedItemList.size.toString())
                for (x in 0..clickedItemList.size - 1) {
                    Log.d("selected items: ", clickedItemList[x].toString())
                }
            }
        })
//
        btnSave.setOnClickListener {
            Log.d("Merged Selected Files:", mSelectedPdfs.size.toString())
            mSelectedPdfs.clear()
            for (x in clickedItemList) {
                mSelectedPdfs.add(Uri.fromFile(File(pdfList[x].path)))
            }
            if (mSelectedPdfs.size > 1) {
                showFileNameDialog(1)
                dialog.dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select at least 2 files",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun searchForPdfs(directory: File, pdfList: MutableList<File>) {
        val files = directory.listFiles()
        files?.forEach { file ->
            if (file.isDirectory) {
                searchForPdfs(file, pdfList) // Recurse into subdirectories
            } else if (file.extension.lowercase() == "pdf") {
                pdfList.add(file)
            }
        }
    }

    private fun showCropDialog(bitmap: Bitmap) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.crop_image_dialog)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        val cropImageView: CropImageView = dialog.findViewById(R.id.cropImageView)

        cropImageView.setImageBitmap(bitmap)
        val btnSave: Button = dialog.findViewById(R.id.btnSave)
        val btnCancel: Button = dialog.findViewById(R.id.btnCancel)

        btnSave.setOnClickListener {
            val cropped: Bitmap? = cropImageView.getCroppedImage()
            dialog.dismiss()
            loader.show()
//            binding.spinKit.visibility = View.VISIBLE
            // Hide at launch
//            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
//                    delay(10000)
                    processImage(cropped!!)
                }

                withContext(Dispatchers.Main) {
                    delay(7000)
                    loader.dismiss()
//                    binding.spinKit.visibility = View.GONE
//                    Toast.makeText(requireContext(),"Image Cropped Successfully: $visionText",Toast.LENGTH_SHORT).show()
                    if (visionText.isEmpty()) {
                        Toast.makeText(requireContext(), "No text extracted", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        val bundle = Bundle()
                        bundle.putString("extractedText", visionText)
                        bundle.putString("image", encodedBitmap)
//                        showInterstitial(R.id.action_navigation_home_to_extractTextFragment, bundle)
//                        showInterstitial()
                        findNavController().navigate(
                            R.id.action_navigation_home_to_extractTextFragment, bundle
                        )
                    }

                }
            }
//
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun toggleFabMenu() {
        binding.fabGallery.alpha = 0.5f
        binding.fabCamera.alpha = 0.5f
        if (!menuOpen) {
            binding.fabGallery.visibility = View.VISIBLE
            binding.fabCamera.visibility = View.VISIBLE

            binding.fabGallery.animate().translationY(-getDimenDp(130f)).alpha(1f).setDuration(200)
            binding.fabCamera.animate().translationY(-getDimenDp(80f)).alpha(1f).setDuration(200)
            binding.fabMain.animate().rotation(45f).setDuration(200)
        } else {
            binding.fabGallery.animate().translationY(0f).alpha(0f).setDuration(200)
                .withEndAction { binding.fabGallery.visibility = View.GONE }
            binding.fabCamera.animate().translationY(0f).alpha(0f).setDuration(200)
                .withEndAction { binding.fabCamera.visibility = View.GONE }
            binding.fabMain.animate().rotation(0f).setDuration(200)
        }
        menuOpen = !menuOpen
    }

    private fun getDimenDp(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun setAdapter() {
        dataList = java.util.ArrayList()
        adapter = ScannedPDFAdapter()
        binding.rvRecent.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecent.adapter = adapter

        adapter.onClick(object : ScannedPDFAdapter.OnItemClick {
            override fun onItemClick(position: Int) {
                val bundle = Bundle()
                bundle.putString("fileName", dataList[position].name)
                bundle.putString("uri", dataList[position].path)
//
                if (requireContext().isPremiumUser()) {
                    findNavController().navigate(
                        R.id.action_navigation_home_to_fileViewerFragment,
                        bundle
                    )
                } else {
                    showInterstitial()

                    findNavController().navigate(
                        R.id.action_navigation_home_to_fileViewerFragment,
                        bundle
                    )
                }

//                showInterstitial(bundle, R.id.action_navigation_home_to_fileViewerFragment)
            }

            override fun onItemClickMore(position: Int, item: PDFData) {
                openMoreBottomSheet(item)
            }
        })
    }

    private fun showInterstitial() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
            }
        }

        if (interstitialAd != null) {
            interstitialAd?.show(requireActivity())
        } else {
            SnToast.Builder().context(requireContext()).type(Type.SUCCESS)
                .message("Password Set Successfully !") //.cancelable(false or true) Optional Default: False
                .build()
        }
    }

    private fun showInterstitial(action: Int, bundle: Bundle) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                findNavController().navigate(action, bundle)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                findNavController().navigate(action, bundle)
            }
        }

        if (interstitialAd != null) {
            interstitialAd?.show(requireActivity())
        } else {
            findNavController().navigate(action, bundle)
        }
    }

    private fun showInterstitial(action: Int) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                findNavController().navigate(action)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                findNavController().navigate(action)
            }
        }

        if (interstitialAd != null) {
            interstitialAd?.show(requireActivity())
        } else {
            findNavController().navigate(action)
        }
    }

    private fun imageToPDF(name: String): Bundle {
        val pdfDocument = PdfDocument()
        val myPageInfo =
            PageInfo.Builder(bitmapImageToPdf!!.width, bitmapImageToPdf!!.height, 1).create()
        val page = pdfDocument.startPage(myPageInfo)


        val canvas = page.canvas
        val paint = Paint()
        paint.setColor("#FFFFFF".toColorInt())
        canvas.drawPaint(paint)
        bitmapImageToPdf =
            bitmapImageToPdf!!.scale(bitmapImageToPdf!!.width, bitmapImageToPdf!!.height)
        paint.setColor(Color.BLUE)
        canvas.drawBitmap(bitmapImageToPdf!!, 0.toFloat(), 0.toFloat(), null)
        pdfDocument.finishPage(page)


        val fileName: String = name + "_image_to.pdf"
//        val directory = File(
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
//            "/pdfscanner/documents/merged/"
//        )
//        if (!directory.exists()) {
//            directory.mkdirs()
//        }
//        val mergedPdfFiles = File(
//            directory, fileName
//        )
//        val outputStream = FileOutputStream(mergedPdfFiles)
//        val document = Document()
//        val copy = PdfCopy(document, outputStream)
//        document.open()

//        val directoryNew =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "/pdfscanner/documents/imageToPdf/"
        )
        if (!directory.exists()) {
            directory.mkdirs()
        }
//        var mergedPdfFiles = File(
//            directory, fileName
//        )
//        val pdfFile = "$directory/$name.pdf"
        val myPDFFile = File(directory, fileName)

        pdfDocument.writeTo(FileOutputStream(myPDFFile))
        pdfDocument.close()
        val pdfData = PDFData(
            "pdf",
            System.currentTimeMillis().toString(),
            fileName,
            myPDFFile.toString(),
            false
        )
        viewModel.insertPDFRecord(pdfData)
        val bundle = Bundle()
        val listFile = ArrayList<String>()
        val listName = ArrayList<String>()
        listName.add(fileName)
        listFile.add(myPDFFile.toString())
        bundle.putStringArrayList("fileName", listName)
        bundle.putStringArrayList("uri", listFile)
        imageToPdf = false
        return bundle
//        findNavController().navigate(R.id.action_nav_tools_to_actionFragment, bundle)
    }

    @SuppressLint("MissingInflatedId")
    private fun openMoreBottomSheet(item: PDFData) {

        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        val thumbnailCache: LruCache<String, Bitmap> = LruCache(cacheSize)

        // on below line we are creating a new bottom sheet dialog.
        val dialog = BottomSheetDialog(requireContext())

        // on below line we are inflating a layout file which we have created.
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_more, null)
        dialog.setContentView(view)

        (view.parent as View).setBackgroundResource(android.R.color.transparent)

        dialog.show()
        val ivFavorite: ImageView = view.findViewById(R.id.ivFavorite)


        val name = view.findViewById<TextView>(R.id.tvHistoryName)
        val date = view.findViewById<TextView>(R.id.tvHistoryDate)
        val icon = view.findViewById<ImageView>(R.id.ivHistoryIcon)
        val rlFavorite: RelativeLayout = view.findViewById(R.id.rlFavorite)
        val rlDelete: RelativeLayout = view.findViewById(R.id.rlDelete)
        val rlShare: RelativeLayout = view.findViewById(R.id.rlShare)

        name.text = item.name
        val dateNew = Date(item.dateCreated.toLong())
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm")
        val finalDate = format.format(dateNew)
        date.text = finalDate

        if (item.name.substringAfter(".") == "pdf") {
            try {
                var thumbnail = thumbnailCache.get(item.path)
                if (thumbnail == null) {
                    thumbnail = generateThumbnail(item.path)
                    thumbnailCache.put(item.path, thumbnail)
                }

                icon.setImageBitmap(thumbnail)


            } catch (_: java.lang.Exception) {
                icon.setImageResource(R.drawable.pdf_icon_history)
            }
        } else {
            icon.setImageResource(R.drawable.text_icon)
        }

        if (item.favorite) {
            ivFavorite.setImageResource(R.drawable.favorite_filled_icon)
        } else {
            ivFavorite.setImageResource(R.drawable.favorite_empty_icon)
        }

        rlFavorite.setOnClickListener {
            item.favorite = !item.favorite
            viewModel.updatePDFRecord(item)
            if (item.favorite) {
                ivFavorite.setImageResource(R.drawable.favorite_filled_icon)
                SnToast.Builder()
                    .context(requireContext())
                    .type(Type.SUCCESS)
                    .message("File added to favorite Successfully!") //.cancelable(false or true) Optional Default: False
                    .build()
            } else {
                ivFavorite.setImageResource(R.drawable.favorite_empty_icon)
                SnToast.Builder()
                    .context(requireContext())
                    .type(Type.SUCCESS)
                    .message("File removed from favorite Successfully !") //.cancelable(false or true) Optional Default: False
                    .build()
            }
            dialog.dismiss()
        }

        rlDelete.setOnClickListener {
            showDeleteDialog(requireContext(), item)
            dialog.dismiss()
//            adapter.notifyDataSetChanged()
        }

        rlShare.setOnClickListener {
            val filePath: String = item.path
            try {
                val file = File(filePath)
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "application/pdf"
                val fileUri = FileProvider.getUriForFile(
                    requireContext(), requireContext().packageName + ".provider", file
                )
                shareIntent.clipData = ClipData.newRawUri("", fileUri)
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                requireContext().startActivity(Intent.createChooser(shareIntent, "Share File"))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        dialog.show()
    }

    private fun generateThumbnail(pdfFilePath: String): Bitmap {
        val pdfRenderer: PdfRenderer?
        val currentPage: PdfRenderer.Page?
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

    private fun showDeleteDialog(context: Context, data: PDFData) {
        val dialog = Dialog(context)
        val dialogBinding: DialogDeleteFilesBinding = DialogDeleteFilesBinding.inflate(
            LayoutInflater.from(context)
        )
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )


        dialogBinding.btnDelete.setOnClickListener {
            viewModel.deletePDFRecord(data)
            SnToast.Builder()
                .context(requireContext())
                .type(Type.SUCCESS)
                .message("File deleted Successfully!") //.cancelable(false or true) Optional Default: False
                .build()
            dialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showInAppReview() {
        val manager = ReviewManagerFactory.create(requireActivity())
        val request = manager.requestReviewFlow()

        request.addOnCompleteListener { task: Task<ReviewInfo> ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener {
                    // After review dialog is closed
                    reviewShown = true
//                    requireActivity().finish()  // Close the app
                    showExitPopup()  // Close the app
                }
            } else {
                // If failed to launch, fallback
                Toast.makeText(requireContext(), "Unable to show review popup", Toast.LENGTH_SHORT)
                    .show()
                reviewShown = true
                showExitPopup()
            }
        }
    }

    private fun showExitPopup() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.exit_popup)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        dialog.show()
        val exit = dialog.findViewById<Button>(R.id.btnExit)
        val cancel = dialog.findViewById<TextView>(R.id.btnCancel)
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        exit.setOnClickListener {
            dialog.dismiss()
            requireActivity().finish()
        }
    }

}


