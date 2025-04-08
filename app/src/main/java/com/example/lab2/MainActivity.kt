package com.example.lab2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private var photoURI: Uri? = null
    private var currentPhotoPath: String = ""

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoURI?.let {
                imageView.setImageURI(it)
                Toast.makeText(this, "Фото отримано!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Не вдалося зробити фото", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageViewSelfie)
        val btnTakeSelfie: Button = findViewById(R.id.btnTakeSelfie)
        val btnSendSelfie: Button = findViewById(R.id.btnSendSelfie)

        btnTakeSelfie.setOnClickListener { dispatchTakePicture() }
        btnSendSelfie.setOnClickListener {
            if (photoURI != null) {
                sendEmailWithAttachment(photoURI!!)
            } else {
                Toast.makeText(this, "Спочатку зробіть селфі!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dispatchTakePicture() {
        try {
            val photoFile = createImageFile()
            val photoUriLocal = FileProvider.getUriForFile(
                this,
                "com.example.lab2.fileprovider",
                photoFile
            )
            photoURI = photoUriLocal
            takePictureLauncher.launch(photoUriLocal)
        } catch (ex: IOException) {
            Toast.makeText(this, "Помилка при створеннi файла для фото", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = externalCacheDir ?: throw IOException("Неможливо отримати externalCacheDir")
        return File.createTempFile(imageFileName, ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun sendEmailWithAttachment(imageUri: Uri) {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            setPackage("com.google.android.gm")
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("hodovychenko@op.edu.ua"))
            putExtra(Intent.EXTRA_SUBJECT, "ANDROID Veles Ilya AI-221 Lab 2")
            putExtra(
                Intent.EXTRA_TEXT,
                "Проект на GitHub можна переглянути за наступним посиланням:\nhttps://github.com/whoisridze/Android-Lab2"
            )
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(emailIntent)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "Gmail не встановлено", Toast.LENGTH_SHORT).show()
        }
    }
}
