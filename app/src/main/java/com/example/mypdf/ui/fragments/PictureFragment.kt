package com.example.mypdf.ui.fragments

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mypdf.data.PictureModel
import com.example.mypdf.databinding.FragmentPictureBinding
import com.example.mypdf.ui.adapters.PictureAdapter
import com.example.mypdf.utils.Constants
import java.io.File
import java.io.FileOutputStream


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
        loadPictures()
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
                    val pictureModel = PictureModel(pictureUri)
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
            val pictureModel = PictureModel(pictureUri!!)
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
            val pictureModel = PictureModel(pictureUri!!)
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