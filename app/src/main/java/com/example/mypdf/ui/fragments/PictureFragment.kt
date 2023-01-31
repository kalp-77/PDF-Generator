package com.example.mypdf.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mypdf.R
import com.example.mypdf.data.PictureModel
import com.example.mypdf.databinding.FragmentPictureBinding
import com.example.mypdf.ui.adapters.PictureAdapter
import com.example.mypdf.utils.Constants
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class PictureFragment : Fragment() {

    companion object {
        private const val TAG = "PICTURE_LIST_TAG"
        private const val STORAGE_REQUEST_CODE = 100
        private const val CAMERA_REQUEST_CODE = 101
    }

    private lateinit var mContext : Context
    private lateinit var cameraPermission: Array<String>
    private lateinit var storagePermission: Array<String>
    private lateinit var allPictureArrayList: ArrayList<PictureModel>
    private lateinit var pictureAdapter : PictureAdapter
    private lateinit var progressDialog: ProgressDialog

    private var job = Job()
    private var scopeForSaving = CoroutineScope(job + Dispatchers.Main)

    // picked image uri
    private var pictureUri : Uri? = null
    // ui
    private lateinit var binding: FragmentPictureBinding


    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPictureBinding.inflate(inflater,container,false)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraPermission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        // init ui views
        binding.addPictureFab.setOnClickListener{
            showInputPictureDialog()
        }
        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)
        loadPictures()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_images, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        // handle delete of images
        when(item.itemId) {
            R.id.picture_delete -> {
                val builder = AlertDialog.Builder(mContext)
                builder.setTitle("Delete Images")
                    .setMessage("Delete All/Selected Images?")
                    .setPositiveButton("DELETE ALL") { dialog, which ->
                        deletePictures(true)
                    }
                    .setNeutralButton("DELETE SELECTED") { dialog, which ->
                        deletePictures(false)
                    }
                    .setNegativeButton("CANCEL") { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
            R.id.picture_pdf -> {
                val builder = AlertDialog.Builder(mContext)
                builder.setTitle("Convert To Pdf")
                    .setMessage("Convert All/Selected Images to Pdf")
                    .setPositiveButton("Convert All") { dialog, which ->
                        convertPicturesToPdf(true)
                    }
                    .setNeutralButton("Convert Selected") { dialog, which ->
                        convertPicturesToPdf(false)
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }

        }
        return super.onOptionsItemSelected(item)
    }
    private fun convertPicturesToPdf(convertAll : Boolean) {
        progressDialog.setMessage("Converting To Pdf...")
        progressDialog.show()

        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executorService.execute {
            var picturesToPdfList = ArrayList<PictureModel>()
            if(convertAll){
                picturesToPdfList = allPictureArrayList
            }
            else {
                for (picture in allPictureArrayList) {
                    if(picture.checked) {
                        picturesToPdfList.add(picture)
                    }
                }
            }
            try {
                val root = File(mContext.getExternalFilesDir(null), Constants.PDF_FOLDER)
                root.mkdirs()

                val timestamp = System.currentTimeMillis()
                val fileName = "PDF_$timestamp.pdf"
                val file = File(root, fileName)
                val fileOutputStream = FileOutputStream(file)
                val pdfDocument = PdfDocument()
                var cnt = 0
                for(picture in picturesToPdfList) {
                    val pictureToPdfUri = picture.pictureUri
                    try{
                        var bitmap : Bitmap
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(mContext.contentResolver, pictureToPdfUri))
                        }
                        else {
                            bitmap = MediaStore.Images.Media.getBitmap(mContext.contentResolver, pictureToPdfUri)
                        }
                        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
                        cnt += 1
                        val pageInfo = PageInfo.Builder(bitmap.width, bitmap.height,cnt+1).create()
                        val page = pdfDocument.startPage(pageInfo)
                        val paint = Paint()
                        paint.color = Color.WHITE
                        pdfDocument.finishPage(page)
                        bitmap.recycle()
                    }
                    catch (e:Exception) {

                    }

                    pdfDocument.writeTo(fileOutputStream)
                    pdfDocument.close()
                }
            }
            catch(e : Exception) {

            }
            handler.post {
                progressDialog.dismiss()
                toast("converted to Pdf")
            }

        }
    }

    private fun deletePictures(deleteAll: Boolean) {
            if (deleteAll) {
                val folder = File(mContext.getExternalFilesDir(null), Constants.PICTURE_FOLDER)
                if (folder.exists()) {
                    Log.d(TAG, "folder size: ${folder.list()?.size}")
                    folder.deleteRecursively()
                }
            } else {
                Log.d(TAG, "inside of selected")
                for (picture in allPictureArrayList) {
                    if (picture.checked) {
                        Log.d(TAG, "inside of check")
                        try {
                            val pathOfPictureToDelete = "${picture.pictureUri.path}"
                            val file = File(pathOfPictureToDelete)
                            Log.d(TAG, "File deleted: ${file.exists()}")
                            if (file.exists()) {
                                Log.d(TAG, "File exist: $file")
                                val isDeleted = file.delete()
                                Log.d(TAG, "File deleted: $isDeleted")
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "deletePictures: ", e)
                        }
                    }
                }
            }
        //}.onJoin
        Log.d(TAG, "inside of load")

        loadPictures()
        if(allPictureArrayList.isNotEmpty()){
            toast("Deleted")
        }
    }

    private fun loadPictures() {
        allPictureArrayList = ArrayList()
        pictureAdapter = PictureAdapter(mContext, allPictureArrayList)
        binding.imageRv.adapter = pictureAdapter

        val folder = File(mContext.getExternalFilesDir(null), Constants.PICTURE_FOLDER)
        if(folder.exists()) {
            val files = folder.listFiles()
            if(files != null) {
                for(file in files) {
                    val pictureUri = Uri.fromFile(file)
                    val pictureModel = PictureModel(pictureUri,false)
                    allPictureArrayList.add(pictureModel)
                    pictureAdapter.notifyItemInserted(allPictureArrayList.size)
                }
            }
            else {

            }
        }
        else {

        }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    private fun savePictureToAppLevelDir(pictureUriToBeSaved: Uri) {
        Log.d(TAG, "savePictureToAppLevelDir: pictureUriToBeSaved :$pictureUriToBeSaved")
        try {
            // getting bitmap from image uri
            val bitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        mContext.contentResolver,
                        pictureUriToBeSaved
                    )
                )
            } else {
                MediaStore.Images.Media.getBitmap(mContext.contentResolver, pictureUriToBeSaved)
            }
            // create folder where we will save the images, no storage permission required
            val dir = File(mContext.getExternalFilesDir(null),Constants.PICTURE_FOLDER )
            dir.mkdirs()

            // sub folder and name to be saved
            val timestamp = System.currentTimeMillis()
            val fileName = "$timestamp.jpeg"
            val file = File(mContext.getExternalFilesDir(null), "${Constants.PICTURE_FOLDER}/$fileName")

            // save picture
            try{
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos)
                fos.flush()
                fos.close()
                Log.d(TAG, "savePictureToAppLevelDir: Saved")
                toast("Image Saved")

            }
            catch(e:Exception) {
                Log.d(TAG, "savePictureToAppLevelDir: ",e)
                Log.d(TAG, "savePictureToAppLevelDir: ${e.message}")
                toast("Failed to save due to ${e.message}")
            }
        }
        catch(e: Exception) {
            Log.d(TAG, "savePictureToAppLevelDir: ",e)
            Log.d(TAG, "savePictureToAppLevelDir: Failed to prepare image due to ${e.message}")
            toast("Failed to prepare image due to ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun showInputPictureDialog() {
        Log.d(TAG, "showInputImageDialog: ")
        val popMenu = PopupMenu(mContext, binding.addPictureFab)
        popMenu.menu.add(Menu.NONE,1,1,"Camera")
        popMenu.menu.add(Menu.NONE,2,2,"Gallery")
        popMenu.show()
        popMenu.setOnMenuItemClickListener {
            when(it.itemId){
                1->{
                    // camera is clicked, check camera permission granted or not
                    Log.d(TAG, "showInputImageDialog: Camera is clicked check if camera permission granted or not")
                    if(checkCameraPermission()) {
                        pickPictureCamera()
                    }
                    else {
                        requestCameraPermission()
                    }
                }
                2->{
                    Log.d(TAG, "showInputImageDialog: Gallery is clicked check if storage permission granted or not")
                    if (checkStoragePermission()) {
                        pickPictureGallery()
                    }
                    else {
                        requestStoragePermission()
                    }
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun pickPictureGallery() {
        Log.d(TAG, "pickPictureGallery: ")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
//        intent.action = Intent.ACTION_GET_CONTENT
        galleryActivityResultLauncher.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private val galleryActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ){ result->
        // here we will recieve the image, if picked
        if(result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            pictureUri = data!!.data
            Log.d(TAG, "galleryActivityResultLauncher: Gallery Image: $pictureUri")
            savePictureToAppLevelDir(pictureUri!!)

            // notify adapter that a new image inserted
            val pictureModel = PictureModel(pictureUri!!, false)
            allPictureArrayList.add(pictureModel)
            pictureAdapter.notifyItemInserted(allPictureArrayList.size)
        }
        else {
            // cancelled
            Log.d(TAG, "galleryActivityResultLauncher : Cancelled")
            toast("Cancelled")
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun pickPictureCamera() {
        Log.d(TAG, "pickimageCamera: ")
        val contentValue = ContentValues()
        contentValue.put(MediaStore.Images.Media.TITLE, "TEMP IMAGE TITLE")
        contentValue.put(MediaStore.Images.Media.DESCRIPTION, "TEMP IMAGE DECRIPTION")
        pictureUri = mContext.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValue)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)
        cameraActivityResultLauncher.launch(intent)
    }
    @RequiresApi(Build.VERSION_CODES.P)
    private val cameraActivityResultLauncher = registerForActivityResult<Intent,ActivityResult> (
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "CameraActivityResultLauncher : Camera Image: $pictureUri")
             // save pictures
            savePictureToAppLevelDir(pictureUri!!)

            //notify the adapter that a new image is inserted
            val pictureModel = PictureModel(pictureUri!!, false)
            allPictureArrayList.add(pictureModel)
            pictureAdapter.notifyItemInserted(allPictureArrayList.size)
        }
        else {
            Log.d(TAG, "CameraActivityResultLauncher: Cancelled ")
            toast("Cancelled")
        }
    }

    // permissions
    private fun checkStoragePermission() : Boolean {
        Log.d(TAG, "checkStoragePermission: ")
        return ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestStoragePermission() {
        Log.d(TAG, "requestStoragePermission: ")
        requestPermissions(storagePermission, STORAGE_REQUEST_CODE)
    }
    private fun checkCameraPermission() : Boolean {
        Log.d(TAG, "checkCameraPermission: ")
        val cameraResult = ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val storageResult = ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return cameraResult && storageResult
    }
    private fun requestCameraPermission(){
        Log.d(TAG, "requestCameraPermission: ")
        requestPermissions(cameraPermission, CAMERA_REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult: ")

        when(requestCode) {
            CAMERA_REQUEST_CODE -> {
                if(grantResults.isNotEmpty()) {
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if(cameraAccepted && storageAccepted) {
                        Log.d(TAG, "onRequestPermissionResult: both permissions are granted (camera & storage), launch camera")
                        pickPictureCamera()
                    }
                    else {
                        Log.d(TAG, "onRequestPermissionResult: permissions are granted (camera & storage), permissions are required ")
                        toast("Camera & Storage permissions are required")
                    }
                }
                else {
                    toast("cancelled")
                }
            }
            STORAGE_REQUEST_CODE -> {
                if(grantResults.isNotEmpty()) {
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (storageAccepted) {
                        Log.d(TAG, "onRequestPermissionResult: storage permission granted, launch gallery intent")
                        pickPictureGallery()
                    } else {
                        Log.d(TAG, "onRequestPermissionResult: gallery permission not granted by the user, can't launch gallery intent ")
                        toast("Storage permission required")
                    }
                }
                else {
                    Log.d(TAG, "onRequestPermissionResult: neither allowed nor denied, Cancelled ")
                    toast("Cancelled")
                }
            }
        }
    }
    private fun toast(message:String) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
    }

}