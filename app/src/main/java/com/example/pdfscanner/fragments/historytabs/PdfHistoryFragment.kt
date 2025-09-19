package com.example.pdfscanner.fragments.historytabs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.LruCache
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emreesen.sntoast.SnToast
import com.emreesen.sntoast.Type
import com.example.pdfscanner.R
import com.example.pdfscanner.SharedViewModel
import com.example.pdfscanner.adapters.ScannedPDFAdapter
import com.example.pdfscanner.databinding.DialogDeleteFilesBinding
import com.example.pdfscanner.databinding.DialogInfoFilesBinding
import com.example.pdfscanner.databinding.FragmentPdfHistoryBinding
import com.example.pdfscanner.db.DatabaseViewModel
import com.example.pdfscanner.db.PDFData
import com.example.pdfscanner.model.PdfFile
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
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import kotlin.collections.isNotEmpty
import kotlin.getValue

class PdfHistoryFragment : Fragment() {

    private lateinit var binding: FragmentPdfHistoryBinding
    private val viewModel: DatabaseViewModel by viewModels {
        DatabaseViewModel.DatabaseViewModelFactory(requireContext())
    }
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var adapter: ScannedPDFAdapter
    private lateinit var dataList: ArrayList<PDFData>
    private var interstitialAd: InterstitialAd? = null
    private var sort = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPdfHistoryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

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
        adapter = ScannedPDFAdapter()
        binding.rvHistoryPDF.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistoryPDF.adapter = adapter
        sort = 2
        viewModel.allPDFRecords.observe(viewLifecycleOwner) { data ->
            data?.let {
//                val sortedList = viewModel.allPDFRecords.value?.sortedWith(PDFData.SortByDate)
//                sortedList?.let {
//                    val updatedList = it
//                    adapter.updateList(updatedList.toList())
////            adapter.notifyDataSetChanged()
//                }
                dataList.addAll(data)
                adapter.updateList(data)
                if (data.isNotEmpty()) {
                    binding.ivEmptyFile.visibility = View.GONE
                    binding.rvHistoryPDF.visibility = View.VISIBLE
                } else {
                    binding.ivEmptyFile.visibility = View.VISIBLE
                    binding.rvHistoryPDF.visibility = View.GONE
                }
                binding.spinKit.visibility = View.GONE
            }
        }

        adapter.onClick(object : ScannedPDFAdapter.OnItemClick {
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

            override fun onItemClickMore(position: Int, item: PDFData) {
                openMoreBottomSheet(item)
            }
        })

        sharedViewModel.filterClick.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                openFilterBottomSheet()
            }
        }

        sharedViewModel.sortClick.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                openSortBottomSheet()
            }
        }
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
                    if (true) {
                        thumbnailCache.put(item.path, thumbnail)
                    }
                }

                if (true) {
                    icon.setImageBitmap(thumbnail)
                } else {
                    icon.setImageResource(R.drawable.pdf_icon_history)
                }


            } catch (_: Exception) {
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
            var filePath = ""
            filePath = item.path
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
        dialog.show()
    }

    private fun showInfoDialog(context: Context, data: PDFData) {
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

    private fun showInterstitial() {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    // maybe load the next level or screen
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                }
                // optional: handle onAdClicked, onAdImpression, onAdShowedFullScreenContent...
            }
            ad.show(requireActivity())
        } ?: run {
            Log.d("TAG", "Interstitial ad wasn't ready")
            // fallback: proceed without ad
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun openSortBottomSheet() {
        // on below line we are creating a new bottom sheet dialog.
        val dialog = BottomSheetDialog(requireContext())

        // on below line we are inflating a layout file which we have created.
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_sort, null)
//        dialog.setCancelable(false)
        dialog.setContentView(view)

        dialog.show()

        (view.parent as View).setBackgroundResource(android.R.color.transparent)

        val name = view.findViewById<TextView>(R.id.sort_name)
        val size = view.findViewById<TextView>(R.id.sort_file_size)

        name.setOnClickListener {
            sort = 1
            sortList(PDFData.SortByName)
            dialog.dismiss()
        }
        size.setOnClickListener {

        }

    }

    @SuppressLint("MissingInflatedId")
    private fun openFilterBottomSheet() {
        // on below line we are creating a new bottom sheet dialog.
        val dialog = BottomSheetDialog(requireContext())

        // on below line we are inflating a layout file which we have created.
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_filter, null)
//        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()
        (view.parent as View).setBackgroundResource(android.R.color.transparent)
//        val name = view.findViewById<TextView>(R.id.sort_name)
//        val size = view.findViewById<TextView>(R.id.sort_file_size)
//
//        name.setOnClickListener {
//            sort = 1
//            sortList(PDFData.SortByName)
//            dialog.dismiss()
//        }
//        size.setOnClickListener {
//
//        }

    }

    private fun sortList(comparator: Comparator<PDFData>) {
        Toast.makeText(requireContext(),"Sorted",Toast.LENGTH_SHORT).show()
//        dataList.sortBy { it.name }
        val newList = ArrayList(dataList)
        newList.sortBy { it.name }
        adapter.updateList(newList)
        binding.rvHistoryPDF.invalidateItemDecorations()
        binding.rvHistoryPDF.requestLayout()
//        val sortedList = viewModel.allPDFRecords.value?.sortedWith(comparator)
//        sortedList?.let {
//            val updatedList = it
////            adapter.updateList(updatedList.toList())
////            adapter.notifyDataSetChanged()
//        }

//        if (sortedList != null) {
////            if (sortedList.isEmpty()) {
////                binding.ivNoDataFound.visibility = View.VISIBLE
////                binding.rvDocuments.visibility = View.GONE
////            } else {
////                binding.ivNoDataFound.visibility = View.GONE
////                binding.rvDocuments.visibility = View.VISIBLE
////            }
//        }
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