package com.example.dalplace.fragments

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dalplace.R
import com.example.dalplace.model.User
import com.example.dalplace.utilities.Firebase
import com.example.dalplace.utilities.Validation
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import android.widget.ImageView as ImageView1
class MyProfileFragment : Fragment() {

    private var imageUri: Uri? = null
    private lateinit var profilePic: ImageView1
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        initProgressDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_profile, container, false)
        val uploadImageButton = view.findViewById<Button>(R.id.updateImageButton)
        val currentUserID: String? = FirebaseAuth.getInstance().uid.toString()
        //----------------------------------------------------------------------------
        var bannerId: String
        var fullname: String = ""
        var email: String
        firebaseStorage = FirebaseStorage.getInstance()
        storageRef = firebaseStorage.getReference()
        val db = FirebaseFirestore.getInstance()

        val profile_name = view.findViewById<TextInputLayout>(R.id.profile_fullname)
        val profile_banner = view.findViewById<TextInputLayout>(R.id.profile_bannerID)
        val profile_email = view.findViewById<TextInputLayout>(R.id.profile_email_readonly)


        showProgressDialog()
        db.collection("users")
            .document(Firebase.userId!!)
            .get()
            .addOnSuccessListener { result ->
                val user = result.toObject(User::class.java)
                    println(user?.id + " ----------------- " + user?.email)
                    profile_name.editText?.setText(user?.name)
                    profile_banner.editText?.setText(user?.bannerId)
                    profile_email.editText?.setText(user?.email)

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        //------------------------------------------------------------

        val currentUser: String? = FirebaseAuth.getInstance().uid.toString()
        val pathReference = storageRef.child("$currentUser.jpg")

        val localfile = File.createTempFile("tempImage", ".jpg")
        pathReference.getFile(localfile)
            .addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                profilePic.setImageBitmap(bitmap)
            }
            .addOnFailureListener {

            }
//
        profilePic = view.findViewById<ImageView1>(R.id.profilePicture)
        uploadImageButton.setOnClickListener {
            choosePicture();
        }

        hideProgressDialog()

        val updateDetailsButton = view.findViewById<Button>(R.id.updateDetails)

        updateDetailsButton.setOnClickListener {

            profile_name.error = ""
            profile_banner.error = ""
            var error: Int = 0

            if (!Validation.validString(profile_name.editText?.text.toString())) {
                profile_name.error = "Required"
                error = +1;
            }
            if (!Validation.validString(profile_banner.editText?.text.toString())) {
                profile_banner.error = "Required"
                error = +1;
            }
            if (error == 0) {
                showProgressDialog()
                val items = HashMap<String, Any>()
                items.put("fullname", profile_name.editText?.text.toString())
                items.put("bannerID", profile_banner.editText?.text.toString())
                items.put("email", profile_email.editText?.text.toString())
                db.collection("users").document(currentUserID.toString()).update(items)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            showValidationAlertBox("Details Updated", "Success")
                        } else {
                            showValidationAlertBox("Update failed", "Error")
                        }
                    }
                hideProgressDialog()
            }
        }


        // Inflate the layout for this fragment
        return view
    }

    private fun choosePicture() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, 1);

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data;
            profilePic.setImageURI(imageUri)
            uploadPicture()
        }
    }


    private fun uploadPicture() {
        showProgressDialog()


        // Create a reference to "mountains.jpg"
        val currentUser: String? = FirebaseAuth.getInstance().uid
        val imageRef = storageRef.child(currentUser + ".jpg")
        if (imageUri != null) {
            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    showValidationAlertBox("Upload Successful", "Success")
                }.addOnFailureListener {
                    showValidationAlertBox("Upload Failure", "Error")
                }
            hideProgressDialog()
        }

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