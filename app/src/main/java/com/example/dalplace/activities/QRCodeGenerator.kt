package com.example.dalplace.activities

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.dalplace.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

class QRCodeGenerator : AppCompatActivity() {
    lateinit var btnGenerate: Button
    lateinit var editText: EditText
    lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_generator)
        btnGenerate = findViewById(R.id.btnGenerateQRCode)
        editText = findViewById(R.id.edttextQRCode)
        imageView = findViewById(R.id.imgQRCode)

        btnGenerate.setOnClickListener {
            val text = editText.text.toString()
            val multiFormatWriter = MultiFormatWriter()
            try {
                val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 400, 400)
                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                imageView.setImageBitmap(bitmap)
                val manager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                manager.hideSoftInputFromWindow(editText.applicationWindowToken, 0)

            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }
    }
}