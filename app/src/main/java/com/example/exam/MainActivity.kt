package com.example.exam

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var imageView: ImageView
    lateinit var takePhotoButton: Button
    lateinit var processButton: Button

    val REQUEST_TAKE_PHOTO = 2001
    var currentPhotoPath: String? = null
    var image: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.galleryView)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        processButton = findViewById(R.id.processPhotoButton)
    }

    fun onTakePhotoClick(v: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(applicationContext, "com.example.exam.fileprovider", photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    fun onProcessPhotoClick(v: View) {

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyymmdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        currentPhotoPath = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            val originalBitmap = BitmapFactory.decodeFile(currentPhotoPath)

            val m = Matrix()
            m.postRotate(90F)
            val image = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, m, true)

            imageView.setImageBitmap(image)
            processButton.visibility = View.VISIBLE

        }
    }

}