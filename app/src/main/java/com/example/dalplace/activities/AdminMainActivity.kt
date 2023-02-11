package com.example.dalplace.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.dalplace.R
import com.example.dalplace.utilities.Firebase
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class AdminMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        val bottomNavView = findViewById<BottomNavigationView>(R.id.admin_bottom_nav_view)

//        reference : https://stackoverflow.com/questions/67641594/bottomnavigation-view-onnavigationitemselectedlistener-is-deprecated
//        reference : https://c1ctech.com/android-bottomnavigationview-example-in-kotlin/
        bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.fragment_admin_home -> {
                    findNavController(R.id.AdminMainFragmentContainerView).navigate(R.id.fragment_daily_sales)
                    return@setOnItemSelectedListener true
                }

                R.id.fragment_admin_scan_qr_code -> {
                    findNavController(R.id.AdminMainFragmentContainerView).navigate(R.id.scannerHomeFragment)
                    return@setOnItemSelectedListener true
                }

                R.id.fragment_feedback -> {
                    findNavController(R.id.AdminMainFragmentContainerView).navigate(R.id.fragment_feedback)
                    return@setOnItemSelectedListener true
                }

                R.id.admin_logout -> {

                    FirebaseAuth.getInstance().signOut()
                    val activity: Class<*>
                    activity = if (Firebase.isUserAuthenticated) {
                        MainActivity::class.java
                    } else {
                        AuthActivity::class.java
                    }

                    val intent = Intent(this, activity)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }

            }
            false
        }
    }
}