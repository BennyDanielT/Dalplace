package com.example.dalplace.fragments

/*********************************************************************************************
 * Author: Benny Daniel Tharigopala
 *--- REFERENCES ---*

 *----1----*
 * Source: https://firebase.google.com/docs/firestore/query-data/get-data#kotlin+ktx
 * Owner: Firebase
 * Motivation for Reference: Cloud Firestore is used to read and write data to a cloud data store.
 * Modifications Performed: I have followed the documentation and have input relevant collection names(s)
and conditional clauses to retrieve data that is relevant to a Transaction.

 *----2----*
 * Source: https://stackoverflow.com/questions/38143624/how-to-get-image-from-url-and-store-it-in-bitmap-variable
 * Owner: Zaartha
 * Motivation for Reference: I have used the aforementioned approach to retrieve an image's URL from Firebase and apply the image to the image View in this Fragment.
 * Modifications Performed: This is a direct approach with no room for modification. First, I pass the Image's URL to
the openStream() method to open a connection to this URL and returns an InputStream for reading from that connection.
Subsequently I convert the input stream to a bitmap and set the image to the ImageView. I have implemented this approach inside a thread since, this is an asynchronous process.

 *----3----*
 * Source: https://github.com/F0RIS/sweet-alert-dialog
 * Owner: F0RIS Anton
 * Motivation for Reference: I have used this library to display visually appealing Pop-up messages as an alternative to Toast messages.
Sweet alerts are used to inform the users of what's going on in the system.
 * Modifications Performed: I have modified the Title Text, Content Text and Confirm Button Text for all Sweet Alerts.
 *********************************************************************************************/

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.dalplace.R
import com.example.dalplace.model.Product
import com.example.dalplace.model.User
import com.example.dalplace.utilities.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.net.URL


class ScannerDetailsFragment : Fragment() {
    private lateinit var buyerTextView: TextView
    private lateinit var sellerTextView: TextView
    private lateinit var productIdTextView: TextView
    private lateinit var productNameTextView: TextView
    private lateinit var deliverProduct: Button
    private lateinit var productImageView: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scanner_details, container, false)
        buyerTextView = view.findViewById<View>(R.id.scanner_details_buyer_id) as TextView
        sellerTextView = view.findViewById<View>(R.id.scanner_details_seller_name) as TextView
        productIdTextView = view.findViewById<View>(R.id.scanner_details_product_id) as TextView
        productNameTextView = view.findViewById<View>(R.id.scanner_details_product_name) as TextView
        deliverProduct = view.findViewById<Button>(R.id.completeTransaction) as Button
        productImageView = view.findViewById<ImageView>(R.id.ProductImage) as ImageView
        /*Obtain Transaction Details from the Fragment's arguments */
        val qrText =
            ScannerDetailsFragmentArgs.fromBundle(requireArguments()).transactionInformation
        val qrComponents = qrText?.split("+++")
        val buyer = qrComponents!![0]
        val seller = qrComponents!![1]
        val productId = qrComponents!![2]
        val productName = qrComponents!![3]
        val validationStatus = qrComponents!![4]
        val documentId = qrComponents!![5]

        val db = FirebaseFirestore.getInstance()
        val currentTransaction = db.collection("purchases").document(documentId)

        /*Obtain Product Image corresponding to the transaction */

//        db.collection("products").whereEqualTo("ownerId", seller).whereEqualTo("title", productName)
//            .get().addOnSuccessListener { documents ->
//            try {
//                println("Here !!!!!")
//                if (documents.size() != 0) {
//                    for (document in documents) {
//                        val productImageURL = document.getString("imageUrl")
//                        println("Here $productImageURL")
//                        var image: Bitmap? = null
//                        try {
//
//                            val thread = Thread {
//                                try {
//                                    val `in` = URL(productImageURL).openStream()
//                                    image = BitmapFactory.decodeStream(`in`)
////                                    productImageView.setImageBitmap(image)
//                                } catch (e: java.lang.Exception) {
//                                    e.printStackTrace()
//                                }
//                            }
//
//                            thread.start()
//
//                        } catch (e: Exception) {
//                            SweetAlertDialog(
//                                this.context,
//                                SweetAlertDialog.WARNING_TYPE
//                            ).setTitleText("Oops Product Image could not be retrieved...!")
//                                .setContentText(e.message.toString())
//                                .setConfirmText("Ok!").show()
//                            e.printStackTrace()
//                        }
//
//                    }
//                }
//
//            } catch (ex: Exception) {
//                Log.e("Error Message", ex.message.toString())
//            }
//        }

        /*Check if the customer is showing an expired QR Code, that is, the item has already been delivered */

        if (validationStatus == "true") {
            SweetAlertDialog(
                this.context,
                SweetAlertDialog.WARNING_TYPE
            ).setTitleText("Product has already been delivered!")
                .setContentText("Request Customer to display an active QR code!")
                .setConfirmText("Ok!").show()
            markButtonDisable(deliverProduct)
        }

        productIdTextView.text = productId
        productNameTextView.text = productName
        buyerTextView.text = buyer
        sellerTextView.text = seller
        getBuyerFromDb(buyer)
        getOwnerFromDb(seller)
        getProductFromDb(productId)

        /*Update the status of the transaction to delivered in Firebase */
        deliverProduct?.setOnClickListener {
            currentTransaction.update("qrvalidated", true).addOnSuccessListener {
                SweetAlertDialog(
                    this.context,
                    SweetAlertDialog.SUCCESS_TYPE
                )
                    .setTitleText("Delivery Successful!")
                    .setContentText("The transaction has been completed...").show()
                val action = ScannerDetailsFragmentDirections
                    .actionScannerDetailsFragmentToScannerHomeFragment()
                Navigation.findNavController(view).navigate(action)
            }.addOnFailureListener {
                SweetAlertDialog(
                    this.context,
                    SweetAlertDialog.ERROR_TYPE
                ).setTitleText("Oops... Something went Wrong")
                    .setContentText("Error Updating Transaction Status in the Database!").show()
            }

        }

        return view
    }

    private fun markButtonDisable(button: Button) {
        button?.isEnabled = false
        button?.setTextColor(ContextCompat.getColor(button.context, R.color.white))
        button?.setBackgroundColor(ContextCompat.getColor(button.context, R.color.grey))
    }

    private fun getOwnerFromDb(ownerId: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection(Firebase.COLLECTION_USERS)
            .document(ownerId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val owner = task.result.toObject(User::class.java)
                    sellerTextView?.text = owner?.name
                }
            }
    }

    private fun getBuyerFromDb(buyerId: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection(Firebase.COLLECTION_USERS)
            .document(buyerId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val buyer = task.result.toObject(User::class.java)
                    buyerTextView?.text = buyer?.name
                }
            }
    }

    private fun getProductFromDb(productId: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection(Firebase.COLLECTION_PRODUCTS)
            .document(productId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val product = task.result.toObject(Product::class.java)
                    // Load Image
                    Picasso.get()
                        .load(product?.imageUrl)
                        .placeholder(R.drawable.ic_baseline_image_24)
                        .error(R.drawable.ic_baseline_error_outline_24)
                        .into(productImageView)
                }
            }
    }
}