package com.example.pdfscanner.fragments.subfragments

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pdfscanner.R
import com.example.pdfscanner.adapters.FileActionAdapter
import com.example.pdfscanner.databinding.FragmentActionBinding
import com.example.pdfscanner.utils.Utils.isPremiumUser
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import java.io.File
import java.lang.Exception

class ActionFragment : Fragment() {

    private lateinit var binding: FragmentActionBinding
    private lateinit var fileNameList: ArrayList<String>
    private lateinit var filePathList: ArrayList<String>
    private var file = true
    private var interstitialAd: InterstitialAd? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentActionBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window: Window? = requireActivity().window
        window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.status_bar)

        WindowInsetsControllerCompat(window!!, window.decorView).isAppearanceLightStatusBars = true

        if (requireContext().isPremiumUser()) {
            binding.adView.visibility = View
                .GONE
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
            binding.adView.visibility = View
                .VISIBLE
        }

        fileNameList = arguments?.getStringArrayList("fileName")!!
        filePathList = arguments?.getStringArrayList("uri")!!

//        val preferences = activity?.getSharedPreferences("Settings", MODE_PRIVATE)
//        if (preferences!!.getBoolean("in_app_review", true)) {
//        showInAppReview(requireActivity())
//        }
        binding.rvActionFiles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActionFiles.setHasFixedSize(true)
        val adapter = FileActionAdapter(fileNameList, filePathList)
        binding.rvActionFiles.adapter = adapter

        adapter.onFileActionItemClick(object : FileActionAdapter.FileActionItemClick {
            override fun onItemClick(position: Int) {
                val bundle = Bundle()
                bundle.putString("uri", filePathList[position].toString())
                bundle.putString("fileName", fileNameList[position])
                findNavController().navigate(
                    R.id.action_actionFragment_to_fileViewerFragment,
                    bundle
                )
            }
        })

        binding.btnDone.setOnClickListener {
            if (requireContext().isPremiumUser()) {
                findNavController().popBackStack()
            } else {
                showInterstitial()
            }
//            findNavController().popBackStack()
        }

//        if (fileNameList.size > 1) {
//            binding.llActionTabs.visibility = View.VISIBLE
//            binding.tvFileName.text = fileNameList[0]
//            binding.tvFileLocation.text = filePathList[0]
//            checkExtension(0)
//            var countImage = 0
//            for(x in 0..fileNameList.size){
//                val extension = fileNameList[x].substringAfter(".")
//                if(extension=="pdf"){
//                    countImage++
//                }
//            }
//            if(countImage>1){
//
//            } else {
//
//            }
//            if (file) {
//                binding.rlActionImage.background = null
//                binding.rlActionFile.background =
//                    ResourcesCompat.getDrawable(resources, R.drawable.tab_bg, null)
//                checkExtension(0)
////                binding.tvFileName.text = fileNameList[0]
////                binding.tvFileLocation.text = filePathList[0]
//            } else {
//                binding.rlActionFile.background = null
//                binding.rlActionImage.background =
//                    ResourcesCompat.getDrawable(resources, R.drawable.tab_bg, null)
//                checkExtension(1)
////                binding.tvFileName.text = fileNameList[1]
////                binding.tvFileLocation.text = filePathList[1]
//            }
//            binding.rlActionFile.setOnClickListener {
//                file = true
//                binding.rlActionImage.background = null
//                binding.rlActionFile.background =
//                    ResourcesCompat.getDrawable(resources, R.drawable.tab_bg, null)
//                binding.tvFileName.text = fileNameList[0]
//                binding.tvFileLocation.text = filePathList[0]
//                checkExtension(0)
//            }
//            binding.rlActionImage.setOnClickListener {
//                file = false
//                binding.rlActionFile.background = null
//                binding.rlActionImage.background =
//                    ResourcesCompat.getDrawable(resources, R.drawable.tab_bg, null)
//                binding.tvFileName.text = fileNameList[1]
//                binding.tvFileLocation.text = filePathList[1]
//                checkExtension(1)
//            }
//
//        } else {
//            binding.llActionTabs.visibility = View.GONE
//            binding.tvFileName.text = fileNameList[0]
//            binding.tvFileLocation.text = filePathList[0]
//            checkExtension(0)
//
//        }


        binding.tvFileOpen.setOnClickListener {
            if (fileNameList.size > 1) {
                if (file) {
                    openFile(0)
                } else {
                    openFile(1)
                }
            } else {
                openFile(0)
            }
        }

        binding.rlShare.setOnClickListener {
            var filePath = ""
            filePath = if (fileNameList.size > 1) {
                if (file) {
                    filePathList[0]
                } else {
                    filePathList[1]
                }
            } else {
                filePathList[0]
            }

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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.rlFavorite.setOnClickListener {

        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (requireContext().isPremiumUser()) {
                    findNavController().popBackStack()
                } else {
                    showInterstitial()
                    findNavController().popBackStack()
                }

            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }

    private fun checkExtension(pos: Int) {
        val extension = fileNameList[pos].substringAfter(".")
        if (extension == "pdf") {
            binding.ivFileIcon.setImageResource(R.drawable.pdf_icon_history)

        } else if (extension == "txt") {
            binding.ivFileIcon.setImageResource(R.drawable.text_icon)
        } else {
            binding.ivFileIcon.setImageResource(R.drawable.image_icon)
        }
    }

    private fun openFile(pos: Int) {
        val bundle = Bundle()
        bundle.putString("uri", filePathList[pos])
        bundle.putString("fileName", fileNameList[pos])
        findNavController().navigate(R.id.action_actionFragment_to_fileViewerFragment, bundle)
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

}

