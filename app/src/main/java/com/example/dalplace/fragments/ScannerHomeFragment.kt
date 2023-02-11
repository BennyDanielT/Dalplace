package com.example.dalplace.fragments

/*********************************************************************************************
 * Author: Benny Daniel Tharigopala
 *--- REFERENCES ---*
 * Source: https://github.com/F0RIS/sweet-alert-dialog
 * Owner: F0RIS Anton
 * Motivation for Reference: I have used this library to display visually appealing Pop-up messages as an alternative to Toast messages.
Sweet alerts are used to inform the users of what's going on in the system.
 * Modifications Performed: I have modified the Title Text, Content Text and Confirm Button Text for all Sweet Alerts.
 *********************************************************************************************/

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import cn.pedant.SweetAlert.SweetAlertDialog

import com.example.dalplace.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore

class ScannerHomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*Assign Fragment components to variables */
        val view = inflater.inflate(R.layout.fragment_scanner_home, container, false)
        val qrScanner = view.findViewById<Button>(R.id.scannerButton)
        val orderIdEditText =
            view.findViewById<TextInputLayout>(R.id.order_number_text_input).editText

        /*Configure a listener for the button */
        qrScanner?.setOnClickListener {
            val orderId = orderIdEditText?.text.toString()
            val orderIdComponents = orderId?.split("+++")
            /*Check if the user has entered an order number, if yes, pass the order number to the Transaction Details fragment, navigate to the QR Scanner fragment, to scan a QR Code and obtain transaction details. */
            if (orderId.equals("")) {
                val action =
                    ScannerHomeFragmentDirections.actionScannerHomeFragmentToScannerCameraFragment()
                Navigation.findNavController(it).navigate(action)
            }
            /*Check if the QR Code is valid by checking if has 3 components, the buyerId, SellerId and ProductId */
            else if (orderIdComponents!!.size != 3) {
                SweetAlertDialog(
                    this.context,
                    SweetAlertDialog.ERROR_TYPE
                ).setTitleText("Invalid order Id!")
                    .setContentText("Request Customer to input a valid order Id!")
                    .setConfirmText("Ok!").show()
            } else {

                val buyer = orderIdComponents!![0]
                val seller = orderIdComponents!![1]
                val productId = orderIdComponents!![2]
                val db = FirebaseFirestore.getInstance()
                val transactions = db.collection("purchases")
                /*Retrieve the corresponding transaction information from Firebase */
                transactions.whereEqualTo("buyerId", buyer).whereEqualTo("ownerId", seller)
                    .whereEqualTo("productId", productId).get().addOnSuccessListener { documents ->
                    try {
                        if (documents.size() != 0) {
                            for (document in documents) {
                                /*Pop-up to inform user that the transaction details are being retrieved */
                                val pDialog =
                                    SweetAlertDialog(this.context, SweetAlertDialog.PROGRESS_TYPE)
                                pDialog.progressHelper.barColor = Color.parseColor("#4BB543")
                                pDialog.titleText = "Retrieving Transaction Details..."
                                pDialog.progressHelper.progress = 1F
                                pDialog.show()
                                val transactionInformation =
                                    orderId + "+++" + document.getString("productTitle") + "+++" + document.getBoolean(
                                        "qrvalidated"
                                    ).toString() + "+++" + document.id
                                val action = ScannerHomeFragmentDirections
                                    .actionScannerHomeFragmentToScannerDetailsFragment(
                                        transactionInformation
                                    )
                                findNavController().navigate(action)
                            }
                        } else {
                            /*Pop-up to inform user that the QR Code is invalid */
                            SweetAlertDialog(
                                this.context,
                                SweetAlertDialog.ERROR_TYPE
                            ).setTitleText("Invalid QR Code!")
                                .setContentText("Request Customer to display an active QR code!")
                                .setConfirmText("Ok!").show()
                        }
                    } catch (ex: Exception) {
                        Toast.makeText(this.context, ex.message, Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener { exception ->
                    SweetAlertDialog(
                        this.context,
                        SweetAlertDialog.ERROR_TYPE
                    ).setTitleText("Invalid QR Code!")
                        .setContentText("Request Customer to display an active QR code!")
                        .setConfirmText("Ok!").show()
                }

            }

        }

        val etOrderNumber = view.findViewById<TextInputLayout>(R.id.order_number_text_input)
        etOrderNumber.editText?.doAfterTextChanged{
            Log.d("ButtonTest", it.toString())
            val textSize = it.toString().length
            qrScanner.text = if (textSize > 0) "Submit" else "Scan QR Code"
        }
        // Inflate the layout for this fragment
        return view
    }

}