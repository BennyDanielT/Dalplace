package com.example.dalplace.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dalplace.R
import com.example.dalplace.model.User
import com.example.dalplace.utilities.Firebase
import com.example.dalplace.utilities.Validation
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    var progressDialog: ProgressDialog? = null

    @SuppressLint("CutPasteId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initProgressDialog()
        val view = inflater.inflate(R.layout.fragment_register, container, false);
        val alreadyUser = view.findViewById<TextView>(R.id.alreadyauser)
        val registerButton = view.findViewById<Button>(R.id.registerButton)
        val db = FirebaseFirestore.getInstance()

        registerButton.setOnClickListener {
            view.findViewById<TextInputLayout>(R.id.register_email).error = ""
            view.findViewById<TextInputLayout>(R.id.register_password).error = ""
            view.findViewById<TextInputLayout>(R.id.register_fullname).error = ""
            view.findViewById<TextInputLayout>(R.id.register_banner).error = ""
            view.findViewById<TextInputLayout>(R.id.register_mobile).error = ""

            var error: Int = 0
            val email =
                view.findViewById<TextInputLayout>(R.id.register_email).editText?.text.toString()
            val password =
                view.findViewById<TextInputLayout>(R.id.register_password).editText?.text.toString()
            val fullname =
                view.findViewById<TextInputLayout>(R.id.register_fullname).editText?.text.toString()
            val bannerID =
                view.findViewById<TextInputLayout>(R.id.register_banner).editText?.text.toString()

            val phoneNumber =
                view.findViewById<TextInputLayout>(R.id.register_mobile).editText?.text.toString()

            if (!Validation.validDalEmail(email)) {
                view.findViewById<TextInputLayout>(R.id.register_email).error =
                    "Requires valid Dalhousie Email"
                error = +1;
            }
            if (!Validation.validString(fullname)) {
                view.findViewById<TextInputLayout>(R.id.register_fullname).error = "Required"
                error = +1;
            }
            if (!Validation.validString(bannerID)) {
                view.findViewById<TextInputLayout>(R.id.register_banner).error = "Required"
                error = +1;
            }
            if (password.length < 8 || !Validation.validString(password)) {
                view.findViewById<TextInputLayout>(R.id.register_password).error =
                    "Requires minimum 8 characters"
                error = +1;
            }

            if (!Validation.validString(phoneNumber)) {
                view.findViewById<TextInputLayout>(R.id.register_mobile).error = "Required"
                error = +1;
            }

            if (error == 0) {
//                val items = HashMap<String, Any>()
//                items.put("fullname", fullname)
//                items.put("bannerID", bannerID)
//                items.put("email", email)
                auth = FirebaseAuth.getInstance()
                showProgressDialog("Registration in progress")
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { it ->
                        if (it.isSuccessful) {
                            val newUser = User(
                                email = email,
                                name = fullname,
                                bannerId = bannerID,
                                phoneNumber = phoneNumber,
                                isAdmin = false
                            )
                            it.result.user?.let { firebaseUser ->
                                db.collection(Firebase.COLLECTION_USERS).document(firebaseUser.uid)
                                    .set(newUser)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Registration Successful!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                                    }.addOnFailureListener {
                                        showValidationAlertBox(it.cause?.message!!, "Error")
                                    }
                            }
                        } else {
                            showValidationAlertBox(it.exception?.message!!, "Error")
                        }
                        hideProgressDialog()
                    }
            }
        }

        alreadyUser.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }


//        Inflate the layout for this fragment
        return view

    }

    private fun showValidationAlertBox(message: String, title: String) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                //Close the Dialog Box
            }.show()
    }


    private fun initProgressDialog() {
        progressDialog = ProgressDialog(context)
        progressDialog?.setCancelable(false)
        progressDialog?.setMessage("Loading...")
    }

    private fun showProgressDialog(message: String = "Loading...") {
        progressDialog?.let {
            it.setMessage(message)
            if (!it.isShowing) it.show()
        }
    }

    private fun hideProgressDialog() {
        progressDialog?.let {
            if (it.isShowing) it.dismiss()
        }
    }


}