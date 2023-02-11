package com.example.dalplace.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dalplace.R
import com.example.dalplace.model.Purchase
import com.example.dalplace.utilities.Firebase
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class ConfirmCheckout : AppCompatActivity(), PaymentResultListener {

    private var db = FirebaseFirestore.getInstance()

    private var buyerId: String? = Firebase.userId
    private var ownerId: String? = null
    private var productId: String? = null
    private var productTitle: String? = null
    private var price: Double = 0.0
    private var sellerEmail:String? = null
    private var buyerPhoneNumber: String? = null
    private var buyerEmail: String? = Firebase.userEmail
    private var buyerName: String? = null
    private val CURRENCY = "CAD"
    private val adminEmail = "pr327862@dal.ca"
    private val adminPhoneNumber = "+19029832111"
    private var sellerPhoneNumber:String? = null
    private var isQrValidated:Boolean = false
    private var documentId:String? = null
    private var amount:String? = null
    private var edtEmail: TextInputLayout? = null
    private var edtName: TextInputLayout? = null
    private var edtPhone: TextInputLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_confirm_checkout)

         edtEmail= findViewById<TextInputLayout>(R.id.confirm_email_edt)
        // edtName = findViewById<TextInputLayout>(R.id.confirm_name_edt)
         edtPhone = findViewById<TextInputLayout>(R.id.confirm_phone_edt)
        var btnConfirm = findViewById<Button>(R.id.btn_confirm_pay)

        productId = intent.getStringExtra("productId")


        println("here1")
        println("initial qr value is $isQrValidated")
        db.collection("purchases").whereEqualTo("productId",productId.toString()).get().addOnCompleteListener {
            if (it.isSuccessful){
                for (document in it.result!!){
                    isQrValidated = document.getBoolean("qrvalidated")!!
                    documentId = document.id
                    sellerEmail = document.getString("sellerEmail")
                    sellerPhoneNumber = document.getString("sellerPhoneNumber")


                    if (isQrValidated){
                        // admin flow

                        edtEmail?.editText?.setText(sellerEmail)
                        edtPhone?.editText?.setText(sellerPhoneNumber)
                        productId = intent.getStringExtra("productId")


                    }else{
                        // user flow
                        edtEmail?.editText?.setText(Firebase.userEmail)
                    }
                }
            }

        }

        btnConfirm.setOnClickListener {
         getDataFromFirestore(productId)

        }
    }



    private fun getDataFromFirestore(productId: String?) {
    println("productid"+productId)
        if (productId != null) {
            db.collection("products").document(productId).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val document = it.result
                        if (document.exists()) {
                            ownerId = document.getString("ownerId")
                            productTitle = document.getString("title")

                            if(isQrValidated){
                                // admin flow
                                price = document.getDouble("price")!!
                                val commission:Double = price - (price/10)
                                amount =(commission.toString().toFloat()*100).toString()
                                println(amount)

                            }
                            else{
                                // user flow
                                price = document.getDouble("price")!!
                                amount =(price.toString().toFloat()*100).toString()
                                println(amount)
                            }


                        }

                    buyerEmail = edtEmail?.editText?.text.toString()
                  //  buyerName = edtName?.editText?.text.toString()
                    buyerPhoneNumber = edtPhone?.editText?.text.toString()


                    if (buyerEmail.isNullOrEmpty() || buyerPhoneNumber.isNullOrEmpty()) {
                     //   edtName?.error = "Please enter your name"
                        edtEmail?.error = "Please enter your email"
                        edtPhone?.error = "Please enter your phone number"
                        if (buyerPhoneNumber!!.length != 10) {
                            edtPhone?.error = "Please enter a valid phone number"
                        }

                    } else {
                        startPayment(adminEmail,adminPhoneNumber,productTitle,amount)
                    }


                    makeOwnerData(ownerId)
                    }
                }
            }

        }


    private fun makeOwnerData(ownerId: String?) {
        db.collection("users").document(ownerId!!).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val document = it.result
                if (document.exists()) {
                    sellerEmail = document.getString("email")
                    sellerPhoneNumber = document.getString("phoneNumber")
                }
            }
        }
    }
    private fun startPayment(sellerEmail:String?, sellerPhoneNumber:String?, productTitle:String?, amount:String?) {

        val co = Checkout()
        co.setKeyID(this.resources.getString(R.string.razorpay_api_key))
        try {
            val options = JSONObject()
            options.put("name","DalPlace")
            options.put("description",productTitle) // add item description
            options.put("image","https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg")
            options.put("theme.color", "#000000");
            options.put("currency", CURRENCY)

            options.put("amount",amount?.toFloat()?.roundToInt().toString())

            val retryObj = JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            val prefill = JSONObject()
            prefill.put("email", sellerEmail)
            prefill.put("contact", sellerPhoneNumber)

            options.put("prefill",prefill)
            co.open(this,options)
        }catch (e: Exception){
            Log.d("Error in payment",e.message.toString())
        }
    }

    private fun updateProduct() {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection(Firebase.COLLECTION_PRODUCTS)
            .document(productId!!)
            .update("sold", true)
    }

    private fun addPurchaseToFirestore() {
        var date = Calendar.getInstance()
        var dateFormat : SimpleDateFormat?= null
        var formattedDate = ""
        dateFormat = SimpleDateFormat(getString(R.string.date_format))
        formattedDate = dateFormat.format(date.time)


        val purchase = Purchase(
            buyerId = buyerId,
            ownerId = ownerId,
            productId = productId,
            productTitle = productTitle,
            price = price,
            isQRValidated = false ,
            date = formattedDate,
            isSold = true,
            buyerEmail = buyerEmail,
            buyerName = null,
            buyerPhoneNumber = "+1$buyerPhoneNumber",
            sellerPhoneNumber = sellerPhoneNumber,
            sellerEmail = sellerEmail,
            sellerPaid = false

        )

        db.collection("purchases")
            .add(purchase)
            .addOnSuccessListener {
             //   Toast.makeText(this,"Data added ", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                //Toast.makeText(this,"Data not added ", Toast.LENGTH_LONG).show()
            }


    }

    override fun onPaymentSuccess(p0: String?) {

        if(isQrValidated){
            // admin flow
           updateSellerPayment()
            startActivity(Intent(this, AdminMainActivity::class.java))
        }else{
            addPurchaseToFirestore()
            updateProduct()
            Toast.makeText(this, "Payment Success: $p0", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    private fun updateSellerPayment() {
        documentId?.let { db.collection("purchases").document(it).update("sellerPaid",true) }
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Log.d("Payment Error", "$p1")
        Toast.makeText(this, "Payment Failed: $p1", Toast.LENGTH_LONG).show()
    }

}
