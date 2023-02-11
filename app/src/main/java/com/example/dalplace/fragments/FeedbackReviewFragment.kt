package com.example.dalplace.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dalplace.adapter.FeedbackAdapter
import com.example.dalplace.databinding.FragmentFeedbackReviewBinding
import com.example.dalplace.model.Feedback
import com.example.dalplace.utilities.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class FeedbackReviewFragment : Fragment() {

    var binding: FragmentFeedbackReviewBinding? = null
    var progressDialog: ProgressDialog? = null

    private val feedbacks = mutableListOf<Feedback>()
    private val adapter = FeedbackAdapter(feedbacks)
    var feedbackSnapshotListener: ListenerRegistration? = null

    /*********************************************************************************************
     * FRAGMENT LIFECYCLES
     *********************************************************************************************/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFeedbackReviewBinding.inflate(layoutInflater)
        initViews()
        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFeedbacksChangeListener()
    }

    /*********************************************************************************************
     * VIEW/WIDGET INITIALIZATION
     *********************************************************************************************/

    private fun initViews() {
        initRecyclerView()
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

    /*********************************************************************************************
     * MESSAGES RECYCLER VIEW INITIALIZATION
     *********************************************************************************************/

    private fun initRecyclerView() {
        binding?.rvFeedbacks?.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding?.rvFeedbacks?.adapter = adapter
        addFeedbackChangeListener()
    }

    private fun addFeedbackChangeListener() {
        feedbackSnapshotListener?.let {
            removeFeedbacksChangeListener()
        }


        val query = getListQuery()
        showProgressDialog("Loading Messages...")
        feedbackSnapshotListener = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.d("Feedbacks", "Error: ${error.message}")
                return@addSnapshotListener
            }

            Log.d("Feedbacks", "Size: ${snapshots!!.documentChanges.size}")
            for (documentChange in snapshots.documentChanges) {

                val feedback = documentChange.document.toObject(Feedback::class.java)
                when (documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        feedbacks.add(documentChange.newIndex, feedback)
                        adapter.notifyItemInserted(documentChange.newIndex)
                        Log.d("Feedbacks", "DocumentChange: ADDED")
                    }

                    DocumentChange.Type.MODIFIED -> {
                        feedbacks.set(documentChange.newIndex, feedback)
                        adapter.notifyItemChanged(documentChange.newIndex)
                        Log.d("Feedbacks", "DocumentChange: MODIFIED")
                    }

                    DocumentChange.Type.REMOVED -> {
                        feedbacks.removeAt(documentChange.oldIndex)
                        adapter.notifyItemRemoved(documentChange.oldIndex)
                        Log.d("Feedbacks", "DocumentChange: REMOVED")
                    }
                }
            }
            hideProgressDialog()
        }
    }

    private fun removeFeedbacksChangeListener() {
        feedbackSnapshotListener?.remove()
        feedbackSnapshotListener = null
    }

    private fun getListQuery(): Query {
        val firestore = FirebaseFirestore.getInstance()

        return firestore.collection(Firebase.COLLECTION_FEEDBACKS)
            .orderBy("title", Query.Direction.ASCENDING)
    }
}