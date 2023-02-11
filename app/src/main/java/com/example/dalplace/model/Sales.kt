package com.example.dalplace.model

import android.content.ContentValues.TAG
import android.util.Log
import com.example.dalplace.utilities.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class Sales{
    val firestore = FirebaseFirestore.getInstance()

    var currentDate = Calendar.getInstance()
    var dateFormat : SimpleDateFormat?= null
    var formattedDate = ""


    fun getSales(function: (Boolean)-> Unit){

        dateFormat = SimpleDateFormat("dd-MM-yyyy")
        formattedDate = dateFormat!!.format(currentDate.time)

        var query = firestore.collection(Firebase.COLLECTION_PURCHASES).whereEqualTo("date", formattedDate.toString())
                query.whereEqualTo("sellerPaid", false)
            .get()
            .addOnSuccessListener {
                documents ->
                for (document in documents){
                    Log.d("list", "$document")
                    var salesObject = SalesData()
                    salesObject.id = document.id.toString()
                    salesObject.amount = document.get("price").toString()
                    salesObject.isDelivered = document.get("qrvalidated").toString().toBoolean()
                    salesObject.productId = document.get("productId").toString()
                    salesObject.ownerId = document.get("sellerEmail").toString()
                    salesObject.buyerId = document.get("buyerEmail").toString()
                    salesObject.productTitle = document.get("productTitle").toString()
                    salesObject.sellingDate = document.get("date").toString()
                    salesObject.productId = document.get("productId").toString()
                    salesObject.sellerPhoneNumber = document.get("sellerPhoneNumber").toString()
                    SalesList.add(salesObject)
                    Log.d(TAG, "${SalesList.getAllSales()}")
                }
                Log.d("Final List", "${SalesList.getAllSales()}")
                function(true)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                function(false)
            }
    }
}
