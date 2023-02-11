package com.example.dalplace.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
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
import com.example.dalplace.activities.AdminMainActivity
import com.example.dalplace.activities.MainActivity
import com.example.dalplace.model.User
import com.example.dalplace.utilities.Firebase
import com.example.dalplace.utilities.LoginInfo
import com.example.dalplace.utilities.Validation
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    var progressDialog: ProgressDialog? = null

    @SuppressLint("CutPasteId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initProgressDialog()
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        val createuser = view.findViewById<TextView>(R.id.registernewuser)
        val loginButton = view.findViewById<Button>(R.id.loginbutton)
        val forgotPasswordButton = view.findViewById<TextView>(R.id.forgotpassword)

        forgotPasswordButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPass)
        }

        loginButton.setOnClickListener {
            view.findViewById<TextInputLayout>(R.id.login_emailbox).error = ""
            view.findViewById<TextInputLayout>(R.id.login_passwordbox).error = ""

            var error: Int = 0

            val email =
                view.findViewById<TextInputLayout>(R.id.login_emailbox).editText?.text.toString()
            val password =
                view.findViewById<TextInputLayout>(R.id.login_passwordbox).editText?.text.toString()

            if (!Validation.validDalEmail(email)) {
                view.findViewById<TextInputLayout>(R.id.login_emailbox).error =
                    "Requires valid Dalhousie Email"
                error = +1;
            }

            if (password.toString().length < 8 || !Validation.validString(password)) {
                view.findViewById<TextInputLayout>(R.id.login_passwordbox).error =
                    "Requires minimum 8 characters"
                error = +1;
            }

            if (error == 0) {
                auth = FirebaseAuth.getInstance()
                showProgressDialog("Logging In...")
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            getLoggedInUser()
                        } else {
                            showValidationAlertBox("Login Failed", "Error")
                        }
                        hideProgressDialog()
                    }

            }
        }
        createuser.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        // Inflate the layout for this fragment
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

    private fun getLoggedInUser() {
        val firebase = FirebaseFirestore.getInstance()

        firebase.collection(Firebase.COLLECTION_USERS)
            .document(Firebase.userId!!)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    LoginInfo.user = task.result.toObject(User::class.java)
                    val activity = if (LoginInfo.user?.isAdmin!!) {
                        AdminMainActivity::class.java
                    } else {
                        MainActivity::class.java
                    }
                    navigateToNextActivity(activity)
                } else {
                    Toast.makeText(
                        context,
                        "Error Logging In: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun navigateToNextActivity(activity: Class<*>) {
        val intent = Intent(context, activity)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

}