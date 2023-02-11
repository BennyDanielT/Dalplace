package com.example.dalplace.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dalplace.R
import com.example.dalplace.model.Feedback
import com.example.dalplace.utilities.Firebase
import com.example.dalplace.utilities.PushNotification
import com.example.dalplace.utilities.Validation
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 * The [FeedbackFragment] allows the DalPlace users to give feedback on th app.
 */
class FeedbackFragment : Fragment(), OnFocusChangeListener {
    private var progressDialog: ProgressDialog? = null

    private fun showProgressDialog() {
        progressDialog?.let {
            it.setMessage("Submitting feedback...")
            if (!it.isShowing) it.show()
        }
    }

    private fun hideProgressDialog() {
        progressDialog?.let {
            if (it.isShowing) it.dismiss()
        }
    }

    override fun onFocusChange(view: View, isFocused: Boolean) {
        val tilTitle = view.findViewById<TextInputEditText>(R.id.feedbackTitle)
        val tilDescription = view.findViewById<TextInputEditText>(R.id.feedbackDescription)

        when (view.id) {
            R.id.etTitle -> {
                if (isFocused) return
                Validation.validateTietStringInput(tilTitle)
            }
            R.id.etDescription -> {
                if (isFocused) return
                Validation.validateTietStringInput(tilDescription)
            }
        }
    }

    private fun showValidationAlertBox(message: String) {
        AlertDialog.Builder(context)
            .setTitle("Invalid Form")
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                //Close the Dialog Box
            }.show()
    }

    private fun isFormValid(title: String, description: String, recommendRgCheckId: Int): Boolean {
        if (title == "") {
            showValidationAlertBox("Title cannot be empty")
            return false
        } else if (description == "") {
            showValidationAlertBox("Description cannot be empty")
            return false
        } else if (recommendRgCheckId == -1) {
            showValidationAlertBox("Recommendation not selected!")
            return false
        }
        return true
    }

    private fun addFeedback(feedback: Feedback) {
        val firestore = FirebaseFirestore.getInstance()

        showProgressDialog()
        firestore
            .collection(Firebase.COLLECTION_FEEDBACKS)
            .add(feedback)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Thank you for your valuable feedback 😊",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_feedbackFragment_to_fragment_home)
                } else {
                    hideProgressDialog()
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun submitFeedback(view: View) {

        val feedbackTitle =
            view.findViewById<TextInputEditText>(R.id.feedbackTitle).text.toString()
        val feedbackDescription =
            view.findViewById<TextInputEditText>(R.id.feedbackDescription).text.toString()
        val feedbackWillRecommendRadioGroup = view.findViewById<RadioGroup>(R.id.rgWillRecommend)
        val feedbackWillRecommendCheckedId = feedbackWillRecommendRadioGroup!!.checkedRadioButtonId
        val feedbackWillRecommend =
            view.findViewById<RadioButton>(feedbackWillRecommendCheckedId)?.text.toString()
        val feedbackIsComplaint = view.findViewById<CheckBox>(R.id.cbIsComplaint).isChecked
        val feedbackRating = view.findViewById<RatingBar>(R.id.ratingBarFeedback).rating

//      Submit the feedback only if the form is valid
//      Check for title, description and recommendation
        if (isFormValid(feedbackTitle, feedbackDescription, feedbackWillRecommendCheckedId)) {
            val feedback = Feedback(
                title = feedbackTitle,
                description = feedbackDescription,
                willRecommend = feedbackWillRecommend,
                isComplaint = feedbackIsComplaint,
                rating = feedbackRating
            )

            addFeedback(feedback)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_feedback, container, false)

        val tilTitle = view.findViewById<TextInputEditText>(R.id.feedbackTitle)
        val tilDescription = view.findViewById<TextInputEditText>(R.id.feedbackDescription)
        val btnSubmitFeedback = view.findViewById<Button>(R.id.btnSubmitFeedback)

        tilTitle.onFocusChangeListener = this
        tilDescription.onFocusChangeListener = this

        btnSubmitFeedback.setOnClickListener { submitFeedback(view) }

        return view
    }
}