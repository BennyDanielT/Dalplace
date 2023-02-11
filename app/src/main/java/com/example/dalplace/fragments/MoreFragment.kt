package com.example.dalplace.fragments

import android.content.Intent
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
import com.example.dalplace.activities.AuthActivity
import com.example.dalplace.activities.MainActivity
import com.example.dalplace.utilities.Firebase
import com.google.firebase.auth.FirebaseAuth

class MoreFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_more, container, false)
        val logout = view.findViewById<TextView>(R.id.tvLogout)

        val myProfile = view.findViewById<TextView>(R.id.tvMyProfile)
        val giveFeedback = view.findViewById<TextView>(R.id.tvFeedback)
        val resetPassword = view.findViewById<TextView>(R.id.resetpassword)

        resetPassword.setOnClickListener {
            var email = FirebaseAuth.getInstance().currentUser?.email

            auth.sendPasswordResetEmail(email.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this.context,"Email Sent", Toast.LENGTH_SHORT)
                            .show()// successful! } else { // failed! } }
                    } else {
                        Toast.makeText(this.context, it.exception.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }

                }
        }


        myProfile.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_more_to_myProfileFragment)
        }

        giveFeedback.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_more_to_feedbackFragment)
        }

        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val activity: Class<*>
            activity = if (Firebase.isUserAuthenticated) {
                MainActivity::class.java
            } else {
                AuthActivity::class.java
            }

            val intent = Intent(this.context, activity)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        // Inflate the layout for this fragment
        return view
    }
}