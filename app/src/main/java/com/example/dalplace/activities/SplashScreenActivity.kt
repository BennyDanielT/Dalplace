package com.example.dalplace.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.dalplace.R
import com.example.dalplace.model.User
import com.example.dalplace.utilities.Firebase
import com.example.dalplace.utilities.LoginInfo
import com.google.firebase.firestore.FirebaseFirestore


class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        if (Firebase.isUserAuthenticated) {
            getLoggedInUser()
        } else {
            val activity = AuthActivity::class.java
            navigateToNextActivity(activity)
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
                        this,
                        "Error Logging In: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun navigateToNextActivity(activity: Class<*>) {
        val intent = Intent(this, activity)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}