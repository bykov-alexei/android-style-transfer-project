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
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.squareup.picasso.Picasso
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    lateinit var imageView: ImageView
    lateinit var takePhotoButton: Button
    lateinit var processButton: Button
    lateinit var shareButton: Button
    lateinit var textView: TextView

    val REQUEST_TAKE_PHOTO = 2001
    var currentPhotoPath: String? = null
    var image: Bitmap? = null
    var f: File? = null

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .readTimeout(50000, TimeUnit.SECONDS)
        .build()

    val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(Queries.API_URL)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.galleryView)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        shareButton = findViewById(R.id.sharePhotoButton)
        processButton = findViewById(R.id.processPhotoButton)
        textView = findViewById(R.id.textView)

        Queries.context = this
        Queries.imageView = imageView
        Queries.textView = textView
        Queries.shareButton = shareButton
        Queries.processButton = processButton
    }

    fun onTakePhotoClick(v: View) {
        textView.visibility = View.INVISIBLE
        shareButton.visibility = View.INVISIBLE

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

    fun onSharePhotoClick(v: View) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, Queries.url)
        intent.type = "text/plain"
        startActivity(intent)
    }

    fun onProcessPhotoClick(v: View) {
        val api = retrofit.create(Queries.Transfer::class.java)

        val reqFile = RequestBody.create(MediaType.parse("image/*"), f)
        val body = MultipartBody.Part.createFormData("img", f!!.name, reqFile)

        val call = api.query(body)
        call.enqueue(QueryCallback())
    }

    class QueryCallback: Callback<String> {
        override fun onResponse(call: Call<String>, response: Response<String>) {
            if (response.isSuccessful) {
                val uuid_result = response.body()!!
                Queries.url = "http://93.94.183.99:8000/image/${uuid_result}"
                val pic = Picasso.Builder(Queries.context).build()
                pic.load(Queries.url).error(R.drawable.ic_launcher_background).into(Queries.imageView)
                Queries.processButton.visibility = View.INVISIBLE
                Queries.shareButton.visibility = View.VISIBLE
                Queries.textView.visibility = View.VISIBLE
            } else {
                Log.d("query", "error: ${response.code()}")
                Log.d("query", "error: ${response.raw()}")
            }
        }

        override fun onFailure(call: Call<String>, t: Throwable) {
            Toast.makeText(Queries.context, "Произошла Ошибка :(", Toast.LENGTH_SHORT).show()
            throw t
        }

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

            f = File(baseContext.cacheDir, "img.jpg")
            f!!.createNewFile()

            val bos = ByteArrayOutputStream()
            image!!.compress(Bitmap.CompressFormat.JPEG, 0, bos)
            val bitmapdata = bos.toByteArray()
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(f)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            try {
                fos!!.write(bitmapdata)
                fos!!.flush()
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

}