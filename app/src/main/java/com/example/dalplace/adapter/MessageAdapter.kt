package com.example.dalplace.adapter

import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.dalplace.R
import com.example.dalplace.databinding.RvMessageBinding
import com.example.dalplace.databinding.RvProductBinding
import com.example.dalplace.fragments.HomeFragmentDirections
import com.example.dalplace.model.Message
import com.example.dalplace.model.Product
import com.example.dalplace.utilities.Formatter
import com.example.dalplace.utilities.LoginInfo
import com.squareup.picasso.Picasso

class MessageAdapter(var messages: MutableList<Message>) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RvMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.bindItem(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class ViewHolder(private val binding: RvMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindItem(message: Message) {
            binding.tvMessage.text = message.message
            binding.tvTimeStamp.text = Formatter.friendlyDate(message.sentTime)

            val loggedInUserId = LoginInfo.user?.id

            // Message Box UI
            if (loggedInUserId!! != message.userId) {
                binding.rvMessage.gravity = Gravity.START
                val bgColor = binding.root.context.resources.getColor(R.color.wingtip_500)
                val textColor = binding.root.context.resources.getColor(R.color.text_light)
                binding.cardView.setCardBackgroundColor(bgColor)
                binding.tvMessage.setTextColor(textColor)
                binding.tvTimeStamp.setTextColor(textColor)
            }
        }
    }
}