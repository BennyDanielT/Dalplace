package com.example.dalplace.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dalplace.databinding.RvFeedbackBinding
import com.example.dalplace.model.Feedback

class FeedbackAdapter(var feedbacks: MutableList<Feedback>) :
    RecyclerView.Adapter<FeedbackAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RvFeedbackBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feedback = feedbacks[position]
        holder.bindItem(feedback)
    }

    override fun getItemCount(): Int {
        return feedbacks.size
    }

    inner class ViewHolder(private val binding: RvFeedbackBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindItem(feedback: Feedback) {
            binding.tvTitle.text = feedback.title
            binding.tvDescription.text = feedback.description
            val recommendText = "Will recommend to a friend? ${feedback.willRecommend}"
            binding.tvRecommend.text = recommendText
            binding.cardViewLabel.visibility =
                if (feedback.isComplaint ?: false) View.VISIBLE else View.GONE

            binding.ratingBarFeedback?.rating = feedback.rating ?: 0f
        }
    }
}