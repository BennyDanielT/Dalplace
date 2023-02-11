package com.example.dalplace.fragments

/*********************************************************************************************
 * Author: Benny Daniel Tharigopala
 *--- REFERENCES ---*

 *----1----*
 * Source: https://github.com/F0RIS/sweet-alert-dialog
 * Owner: F0RIS Anton
 * Motivation for Reference: I have used this library to display visually appealing Pop-up messages as an alternative to Toast messages.
Sweet alerts are used to inform the users of what's going on in the system.
 * Modifications Performed: I have modified the Title Text, Content Text and Confirm Button Text for all Sweet Alerts.

 *----2----*
 * Source: https://github.com/yuriy-budiyev/code-scanner
 * Owner: yuriy-budiyev
 * Motivation for Reference: I have used this Code Scanner library to scan QR codes and retrieve transaction details embedded in the QR Code.
 * Modifications Performed: I have followed the documentation to configure and initiate the QR Scanner
when a DalPlace admin desires to scan a QR code. I have modified the sample code to navigate
to the Transaction Details fragment and pass the information retrieved from the QR Code to the fragment.
 *********************************************************************************************/

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.example.dalplace.R
import com.example.dalplace.databinding.FragmentScannerCameraBinding
import com.google.firebase.firestore.FirebaseFirestore

class ScannerCameraFragment : Fragment() {

    private var binding: FragmentScannerCameraBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var codeScanner: CodeScanner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_scanner_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scannerView = view.findViewById<CodeScannerView>(R.id.scannerView)
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                /*Retrieve the text embedded in the QR Code */
                val orderNumber = it.text
                val qrComponents = orderNumber?.split("+++")
                if (qrComponents!!.size != 3) {
                    SweetAlertDialog(
                        this.context,
                        SweetAlertDialog.ERROR_TYPE
                    ).setTitleText("Invalid QR Code!")
                        .setContentText("Request Customer to display an active QR code!")
                        .setConfirmText("Ok!").show()
                } else {
                    val buyer = qrComponents!![0]
                    val seller = qrComponents!![1]
                    val productId = qrComponents!![2]
                    val db = FirebaseFirestore.getInstance()
                    val transactions = db.collection("purchases")
                    println("Here $buyer $seller $productId")
                    /*Retrieve the Transaction detaiuls and pass it to the Transaction Details fragment */
                    transactions.whereEqualTo("buyerId", buyer).whereEqualTo("ownerId", seller)
                        .whereEqualTo("productId", productId).get()
                        .addOnSuccessListener { documents ->
                            try {
                                if (documents.size() != 0) {
                                    for (document in documents) {
                                        val pDialog =
                                            SweetAlertDialog(
                                                this.context,
                                                SweetAlertDialog.PROGRESS_TYPE
                                            )
                                        pDialog.progressHelper.barColor =
                                            Color.parseColor("#4BB543")
                                        pDialog.titleText = "Retrieving Transaction Details..."
                                        pDialog.progressHelper.progress = 1F
//                                        pDialog.show()
                                        val transactionInformation =
                                            orderNumber + "+++" + document.getString("productTitle") + "+++" + document.getBoolean(
                                                "qrvalidated"
                                            ).toString() + "+++" + document.id
                                        val action = ScannerCameraFragmentDirections
                                            .actionScannerCameraFragmentToScannerDetailsFragment(
                                                transactionInformation
                                            )
                                        findNavController().navigate(action)
                                    }
                                } else {
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
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}