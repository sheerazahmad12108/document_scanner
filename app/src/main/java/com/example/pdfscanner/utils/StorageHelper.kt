package com.example.pdfscanner.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.InputStream

object StorageHelper {

    private lateinit var path: File

  val TAG = "STORAGE HELPER"


    fun getFromSdcard(context: Context,isWhatsapp:Boolean): Array<DocumentFile>? {
        if (isWhatsapp == true){


            val treeUri: String = ApplicationClass.preference?.getString(Constants.STATUS_FOLDER_URI).toString()
            val fromTreeUri = DocumentFile.fromTreeUri(context, Uri.parse(treeUri))
            return if (fromTreeUri != null && fromTreeUri.exists() && fromTreeUri.isDirectory
                && fromTreeUri.canRead() && fromTreeUri.canWrite()
            ) {
                fromTreeUri.listFiles()
            } else {
                null
            }
        }else{
            val treeUri: String = ApplicationClass.preference?.getString(Constants.STATUS_FOLDER_WHATSAPP_BUSINESS_URI).toString()
            val fromTreeUri = DocumentFile.fromTreeUri(context, Uri.parse(treeUri))
            return if (fromTreeUri != null && fromTreeUri.exists() && fromTreeUri.isDirectory
                && fromTreeUri.canRead() && fromTreeUri.canWrite()
            ) {
                fromTreeUri.listFiles()
            } else {
                null
            }

        }

    }
    fun savefile(context: Context, sourceUri: Uri) {
        try {
            val contentResolver = context.contentResolver
            val documentFile = DocumentFile.fromSingleUri(context, sourceUri)
            val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/CTWStatusSaver/downloads/"

            if (documentFile != null && documentFile.exists()) {
                val inputStream = contentResolver.openInputStream(sourceUri)


                val fileName = documentFile.name

                if (fileName?.let { isFileExistsInDirectory(context, relativePath, it) } == true){
//                    Toast.makeText(context, "File Already Saved", Toast.LENGTH_SHORT).show()
                    return
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    if (fileName != null) {
                        put(MediaStore.MediaColumns.MIME_TYPE, if (fileName.endsWith(".jpg")) "image/jpeg" else "video/mp4")
                    }
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }

                val targetUri = contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
                val outputStream = targetUri?.let { contentResolver.openOutputStream(it) }

                if (inputStream != null && outputStream != null) {
                    outputStream.use { out ->
                        inputStream.copyTo(out)
                    }
                    Toast.makeText(context, "Status Saved", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "Error occurred: Cannot access streams")
                }
            } else {
                Log.d(TAG, "Error occurred: File does not exist ")
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "savefile:${e.localizedMessage} ")
        }
    }

    fun saveFileWhats(context: Context, srcfile: File) {
        val file = File(Environment.getExternalStorageDirectory().path + File.separator+"CTWStatusSaver/downloads/")

        if (!file.exists()) {
            file.mkdir()
            if (!file.mkdirs()) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
        var name = ""
        if (srcfile.path.endsWith(".jpg")) {
            name = System.currentTimeMillis().toString() + ".jpg"
        } else
            name = System.currentTimeMillis().toString() + ".mp4"

        val destFile = File(file.toString()+ File.separator+name)


        srcfile.copyTo(destFile, true)



        Toast.makeText(context, "Status Downloaded", Toast.LENGTH_SHORT).show()

    }

    fun shareFile(context: Context, isVideo: Boolean, path: String?) {
        Log.d("CHECK_PATH", "showPhotoView:{$path} ")
        val share = Intent()
        share.setAction(Intent.ACTION_SEND)
        if (isVideo) share.setType("Video/*")
        else share.setType("image/*")
        val uri = if (path!!.startsWith("content")) {
            Uri.parse(path)
        } else {
            FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider", File(path)
            )
        }

        share.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(share)
    }

    fun repostWhatsApp(context: Context, isVideo: Boolean, path: String?) {
        val share = Intent()
        share.setAction(Intent.ACTION_SEND)
        if (isVideo) share.setType("Video/*")
        else share.setType("image/*")
        val uri = if (path!!.startsWith("content")) {
            Uri.parse(path)
        } else {
            FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider", File(path)
            )
        }
        share.putExtra(Intent.EXTRA_STREAM, uri)
        share.setPackage("com.whatsapp")
        context.startActivity(share)
    }


    fun getPathFromUri(context: Context, uri: Uri): String? {
        var filePath: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (it.moveToFirst()) {
                    val fileName = it.getString(nameIndex)
                    val tempFile = File(context.cacheDir, fileName)
                    context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
                        tempFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    filePath = tempFile.absolutePath
                } else {
                    filePath = null
                }
            }
        } else {
            filePath = uri.path
        }
        return filePath
    }

    fun isFileExistsInDirectory(context: Context, relativePath: String, fileName: String): Boolean {
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} = ? AND ${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(relativePath, fileName)

        val cursor = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            null
        )

        val fileExists = cursor?.use { it.count > 0 } ?: false
        cursor?.close()
        return fileExists
    }



    fun deleteImageFromExternalStorage(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    fun isFileExists(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists()
    }
}