package com.example.dalplace.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.dalplace.databinding.RvChatBinding
import com.example.dalplace.fragments.ChatListFragmentDirections
import com.example.dalplace.fragments.HomeFragmentDirections
import com.example.dalplace.model.ChatRoom
import com.example.dalplace.model.Message
import com.example.dalplace.model.Product
import com.example.dalplace.model.User
import com.example.dalplace.utilities.Firebase
import com.example.dalplace.utilities.Formatter
import com.google.firebase.firestore.FirebaseFirestore

class ChatAdapter(var chatRooms: MutableList<ChatRoom>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RvChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatRoom = chatRooms[position]
        holder.bindItem(chatRoom)
    }

    override fun getItemCount(): Int {
        return chatRooms.size
    }

    inner class ViewHolder(private val binding: RvChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindItem(chatRoom: ChatRoom) {
            getProductFromDb(chatRoom)
            getOwnerFromDb(chatRoom)
            getBuyerFromDb(chatRoom)

            // Click Listener
            binding.rvChatRoom.setOnClickListener { onItemClicked(chatRoom) }
        }

        private fun onItemClicked(chatRoom: ChatRoom) {
            val action = ChatListFragmentDirections.actionChatsToChatRoom(chatRoom)
            Navigation.findNavController(binding.root).navigate(action)
        }

        private fun getOwnerFromDb(chatRoom: ChatRoom) {
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection(Firebase.COLLECTION_USERS)
                .document(chatRoom?.ownerId!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val owner = task.result.toObject(User::class.java)
                        binding?.tvOwner?.text = "Seller: ${owner?.name}"
                    }
                }
        }

        private fun getBuyerFromDb(chatRoom: ChatRoom) {
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection(Firebase.COLLECTION_USERS)
                .document(chatRoom?.buyerId!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val buyer = task.result.toObject(User::class.java)
                        binding?.tvBuyer?.text = "Requester: ${buyer?.name}"
                    }
                }
        }

        private fun getProductFromDb(chatRoom: ChatRoom) {
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection(Firebase.COLLECTION_PRODUCTS)
                .document(chatRoom?.productId!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val product = task.result.toObject(Product::class.java)
                        binding?.tvTitle?.text = product?.title
                    }
                }
        }
    }
}