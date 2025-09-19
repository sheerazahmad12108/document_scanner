package com.example.pdfscanner.fragments.subfragments

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emreesen.sntoast.SnToast
import com.emreesen.sntoast.Type
import com.example.pdfscanner.R
import com.example.pdfscanner.databinding.FragmentSignatureBinding
import com.example.pdfscanner.db.DatabaseViewModel
import com.example.pdfscanner.db.ImageData
import com.example.pdfscanner.utils.Utils.isPremiumUser
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class SignatureFragment : Fragment() {

    private lateinit var binding: FragmentSignatureBinding
    private var interstitialAd: InterstitialAd? = null
    private val viewModel: DatabaseViewModel by viewModels {
        DatabaseViewModel.DatabaseViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        binding = FragmentSignatureBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window: Window? = requireActivity().window
        window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.status_bar)

        WindowInsetsControllerCompat(window!!, window.decorView).isAppearanceLightStatusBars = true

        if(requireContext().isPremiumUser()){
            binding.adView.visibility = View.GONE
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
            binding.adView.loadAd(adRequest)

            binding.adView.visibility = View.VISIBLE
        }

        binding.ivBack.setOnClickListener {
//            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            findNavController().popBackStack()
        }

        binding.btnClearPad.setOnClickListener {
            binding.signaturePad.clear()
        }

        binding.btnSaveSignature.setOnClickListener {
            showDialog()
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                findNavController().popBackStack()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }

    private fun showDialog() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.signature_bottom_sheet_dialog)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val edtName: EditText = dialog.findViewById<EditText>(R.id.edtPDFFileName)

        edtName.setText("signature_" + System.currentTimeMillis().toString())
        val btnSave: Button = dialog.findViewById<Button>(R.id.btnSave)
        val btnCancel: Button = dialog.findViewById<Button>(R.id.btnCancel)
        val bitmapList = ArrayList<Bitmap>()
        btnSave.setOnClickListener {
//            if (cbTransient.isChecked && cbWhite.isChecked) {
//                bitmapList.clear()
//                val bitmap = binding.signaturePad.transparentSignatureBitmap
//                val bitmapNew = binding.signaturePad.signatureBitmap
//                bitmapList.add(bitmap)
//                bitmapList.add(bitmapNew)
//                saveImage(bitmapList, edtName.text.toString())
//                dialog.dismiss()
//            } else if (cbTransient.isChecked) {
//                bitmapList.clear()
//                val bitmap = binding.signaturePad.transparentSignatureBitmap
//                bitmapList.add(bitmap)
//                saveImage(bitmapList, edtName.text.toString())
//                dialog.dismiss()
//            } else if (cbWhite.isChecked) {
                bitmapList.clear()
                val bitmap = binding.signaturePad.signatureBitmap
                bitmapList.add(bitmap)
                saveImage(bitmapList, edtName.text.toString())
                dialog.dismiss()
//            } else {
//                SnToast.Builder()
//                    .context(requireContext())
//                    .type(Type.ERROR)
//                    .message("Please Select Type to Save") //.cancelable(false or true) Optional Default: False
//                    .build()
//            }

        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun saveImage(bitmapList: ArrayList<Bitmap>, name: String) {

        val bundle = Bundle()
        val listFile = ArrayList<String>()
        val listName = ArrayList<String>()
        for (bitmap in bitmapList) {
            val fileName = "$name.jpg"
            val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"/pdfscanner/images/")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(
                directory,
                fileName
            )
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            try {
                outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            outputStream.close()
            listName.add(fileName)
            listFile.add(file.toString())
            val outputStreamNew = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStreamNew)
            val imageData =
                ImageData(
                    "jpg",
                    System.currentTimeMillis().toString(),
                    fileName,
                    file.toString(),false
                )
            imageData.let { viewModel.insetRecord(it) }
        }
        bundle.putStringArrayList("uri", listFile)
        bundle.putStringArrayList("fileName", listName)
        SnToast.Builder()
            .context(requireContext())
            .type(Type.SUCCESS)
            .duration(1500)
            .message("Signature Saved Successfully !").build()
        lifecycleScope.launch {
            delay(2000)
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            if(requireContext().isPremiumUser()){
                findNavController().navigate(R.id.action_signatureFragment_to_actionFragment, bundle)
            } else {
                showInterstitial(bundle, R.id.action_signatureFragment_to_actionFragment)
            }
        }
    }

    private fun showInterstitial(bundle: Bundle, action: Int) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null

                findNavController().navigate(
                    action,
                    bundle
                )
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                findNavController().navigate(
                    action,
                    bundle
                )

            }
        }

        if (interstitialAd != null) {
            interstitialAd?.show(requireActivity())
        } else {
            findNavController().navigate(
                action,
                bundle
            )
        }
    }
}