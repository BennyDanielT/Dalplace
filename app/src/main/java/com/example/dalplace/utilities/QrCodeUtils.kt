package com.example.dalplace.utilities

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

class QrCodeUtils {
    companion object {

        fun  generateQrCode(str: String): Bitmap {

            val multiFormatWriter = MultiFormatWriter()
            try {
                val bitMatrix = multiFormatWriter.encode(str, BarcodeFormat.QR_CODE, 400, 400)
                val barcodeEncoder = BarcodeEncoder()
                return barcodeEncoder.createBitmap(bitMatrix)

            } catch (e: WriterException) {
                e.printStackTrace()

            }
            return Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888)
        }
    }
}