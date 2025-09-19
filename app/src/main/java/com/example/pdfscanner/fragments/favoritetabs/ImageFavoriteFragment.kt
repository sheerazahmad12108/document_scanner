package com.example.pdfscanner.fragments.favoritetabs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.CursorWindow
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emreesen.sntoast.SnToast
import com.emreesen.sntoast.Type
import com.example.pdfscanner.R
import com.example.pdfscanner.adapters.FavoriteImageAdapter
import com.example.pdfscanner.databinding.DialogDeleteFilesBinding
import com.example.pdfscanner.databinding.DialogInfoFilesBinding
import com.example.pdfscanner.databinding.FragmentImageFavoriteBinding
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.lang.reflect.Field
import java.util.ArrayList
import kotlin.getValue

class ImageFavoriteFragment : Fragment() {

    private lateinit var binding: FragmentImageFavoriteBinding
    private lateinit var adapter: FavoriteImageAdapter
    private var interstitialAd: InterstitialAd? = null
    private val viewModel: DatabaseViewModel by viewModels {
        DatabaseViewModel.DatabaseViewModelFactory(requireContext())
    }
    private lateinit var dataList: ArrayList<ImageData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentImageFavoriteBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 100 * 1024 * 1024) //the 100MB is the new size
        } catch (e: Exception) {
            e.printStackTrace()
        }

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

        binding.spinKit.visibility = View.VISIBLE
        dataList = ArrayList()
        adapter = FavoriteImageAdapter()

        binding.rvFavoriteImage.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavoriteImage.adapter = adapter
        viewModel.allFavoriteImageRecords.observe(viewLifecycleOwner) { data ->
            data?.let {
                dataList.addAll(data)
                adapter.updateList(data)
                if (data.isNotEmpty()) {
                    binding.ivEmptyFile.visibility = View.GONE
                    binding.rvFavoriteImage.visibility = View.VISIBLE
                } else {
                    binding.ivEmptyFile.visibility = View.VISIBLE
                    binding.rvFavoriteImage.visibility = View.GONE
                }
                binding.spinKit.visibility = View.GONE
            }
        }

        adapter.onClick(object : FavoriteImageAdapter.OnItemClick {
            override fun onItemClick(position: Int) {
                val bundle = Bundle()
                bundle.putString("fileName", dataList[position].name)
                bundle.putString("uri", dataList[position].path)
                if(requireContext().isPremiumUser()){
                    findNavController().navigate(
                        R.id.action_navigation_history_to_fileViewerFragment,
                        bundle
                    )
                } else {
                    showInterstitial()
                    findNavController().navigate(
                        R.id.action_navigation_history_to_fileViewerFragment,
                        bundle
                    )
                }
            }

            override fun onItemClickMore(
                position: Int,
                item: ImageData
            ) {
                openMoreBottomSheet(item)
            }
        })
    }

    @SuppressLint("MissingInflatedId")
    private fun openMoreBottomSheet(item: ImageData) {
        // on below line we are creating a new bottom sheet dialog.
        val dialog = BottomSheetDialog(requireContext())

        // on below line we are inflating a layout file which we have created.
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_more, null)
        dialog.setCancelable(true)
        dialog.setContentView(view)

        (view.parent as View).setBackgroundResource(android.R.color.transparent)

        dialog.show()
        val ivFavorite: ImageView = view.findViewById<ImageView>(R.id.ivFavorite)
        val rlFavorite: RelativeLayout = view.findViewById<RelativeLayout>(R.id.rlFavorite)
        val rlDelete: RelativeLayout = view.findViewById<RelativeLayout>(R.id.rlDelete)
        val rlShare: RelativeLayout = view.findViewById<RelativeLayout>(R.id.rlShare)

        if (item.favorite) {
            ivFavorite.setImageResource(R.drawable.icon_favorite_new)
        } else {
            ivFavorite.setImageResource(R.drawable.blank_favorite_icon)
        }

        rlFavorite.setOnClickListener {
            item.favorite = !item.favorite
            viewModel.updateImageRecord(item)
            if (item.favorite) {
                ivFavorite.setImageResource(R.drawable.icon_favorite_new)
                SnToast.Builder()
                    .context(requireContext())
                    .type(Type.SUCCESS)
                    .message("File added to favorite Successfully!") //.cancelable(false or true) Optional Default: False
                    .build()
            } else {
                ivFavorite.setImageResource(R.drawable.blank_favorite_icon)
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
            val filePath = item.path
            val imageFile = File(filePath)
            val imageUri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                imageFile
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, imageUri)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            requireContext().startActivity(Intent.createChooser(shareIntent, "Share Image"))
        }

        dialog.show()


    }

    private fun showInfoDialog(context: Context, data: ImageData) {
        val dialog = Dialog(context)
        val dialogBinding: DialogInfoFilesBinding = DialogInfoFilesBinding.inflate(
            LayoutInflater.from(context)
        )
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.fileName.text = data.name
        dialogBinding.filePath.text = data.path.substringBeforeLast("/")


        dialogBinding.dismiss.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteDialog(context: Context, data: ImageData) {
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
            viewModel.deleteImageRecord(data)
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

    private fun showInterstitial() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null

//                findNavController().navigate(
//                    action,
//                    bundle
//                )
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
//                findNavController().navigate(
//                    action,
//                    bundle
//                )

            }
        }

        if (interstitialAd != null) {
            interstitialAd?.show(requireActivity())
        } else {
//            findNavController().navigate(
//                action,
//                bundle
//            )
        }
    }

}