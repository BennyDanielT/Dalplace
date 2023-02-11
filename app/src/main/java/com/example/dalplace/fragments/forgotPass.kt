package com.example.dalplace.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.dalplace.R
import com.example.dalplace.utilities.Validation
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class forgotPass : Fragment() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_forgot_pass, container, false)
        val sendEmailButton = view.findViewById<Button>(R.id.resetPasswordButton)

        val cancelButton = view.findViewById<TextView> (R.id.cancelButton)

        cancelButton.setOnClickListener {
            findNavController().navigate(R.id.action_forgotPass_to_loginFragment)
        }


        sendEmailButton.setOnClickListener {

            view.findViewById<TextInputLayout>(R.id.resetEmailText).error = ""

            val resetPasswordEmail =
                view.findViewById<TextInputLayout>(R.id.resetEmailText).editText?.text.toString()
            var error: Int = 0
            if (!Validation.validDalEmail(resetPasswordEmail)) {
                view.findViewById<TextInputLayout>(R.id.resetEmailText).error =
                    "Requires valid Dalhousie Email"
                error = +1;
            }

            if (error == 0) {
                auth.sendPasswordResetEmail(resetPasswordEmail.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this.context, "Email Sent", Toast.LENGTH_SHORT)
                                .show()// successful! } else { // failed! } }
                        } else {
                            Toast.makeText(
                                this.context,
                                it.exception.toString(),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
            }
        }

        // Inflate the layout for this fragment
        return view
    }

}