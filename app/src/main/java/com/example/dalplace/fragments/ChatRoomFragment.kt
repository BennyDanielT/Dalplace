package com.example.dalplace.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dalplace.R
import com.example.dalplace.adapter.MessageAdapter
import com.example.dalplace.databinding.FragmentChatRoomBinding
import com.example.dalplace.model.ChatRoom
import com.example.dalplace.model.Message
import com.example.dalplace.model.Product
import com.example.dalplace.model.User
import com.example.dalplace.utilities.Firebase
import com.example.dalplace.utilities.LoginInfo
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.Calendar

class ChatRoomFragment : Fragment() {

    var binding: FragmentChatRoomBinding? = null
    private var chatRoom: ChatRoom? = null
    var progressDialog: ProgressDialog? = null

    private val messages = mutableListOf<Message>()
    private val adapter = MessageAdapter(messages)
    var messageSnapshotListener: ListenerRegistration? = null

    /*********************************************************************************************
     * FRAGMENT LIFECYCLES
     *********************************************************************************************/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatRoomBinding.inflate(layoutInflater)
        initViews()
        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        removeMessagesChangeListener()
    }

    /*********************************************************************************************
     * VIEW/WIDGET INITIALIZATION
     *********************************************************************************************/

    private fun initViews() {
        getFragmentArguments()
        getProductFromDb()
        getOwnerFromDb()
        getBuyerFromDb()
        initRecyclerView()

        // Setting on click listeners
        binding?.btnSend?.setOnClickListener {
            sendMessage()
        }
    }

    private fun getFragmentArguments() {
        arguments?.let {
            chatRoom = ChatRoomFragmentArgs.fromBundle(it).chatRoom
        }
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

    private fun sendMessage() {
        val message = binding?.etMessage?.text.toString()
        if (message.isBlank()) return

        binding?.tilMessage?.editText?.text = null
        saveMessageToDb(message)
    }

    /*********************************************************************************************
     * MESSAGES RECYCLER VIEW INITIALIZATION
     *********************************************************************************************/

    private fun initRecyclerView() {
        binding?.rvMessages?.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            true
        )
        binding?.rvMessages?.adapter = adapter
        addMessagesChangeListener()
    }

    private fun addMessagesChangeListener() {
        messageSnapshotListener?.let {
            removeMessagesChangeListener()
        }


        val query = getListQuery()
        showProgressDialog("Loading Messages...")
        messageSnapshotListener = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.d("Messages", "Error: ${error.message}")
                return@addSnapshotListener
            }

            Log.d("Messages", "Size: ${snapshots!!.documentChanges.size}")
            for (documentChange in snapshots!!.documentChanges) {

                val message = documentChange.document.toObject(Message::class.java)
                when (documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        messages.add(documentChange.newIndex, message)
                        adapter.notifyItemInserted(documentChange.newIndex)
                        Log.d("Messages", "DocumentChange: ADDED")
                    }

                    DocumentChange.Type.MODIFIED -> {
                        messages?.set(documentChange.newIndex, message)
                        adapter.notifyItemChanged(documentChange.newIndex)
                        Log.d("Messages", "DocumentChange: MODIFIED")
                    }

                    DocumentChange.Type.REMOVED -> {
                        messages?.removeAt(documentChange.oldIndex)
                        adapter.notifyItemRemoved(documentChange.oldIndex)
                        Log.d("Messages", "DocumentChange: REMOVED")
                    }
                }
            }
            hideProgressDialog()
        }
    }

    private fun removeMessagesChangeListener() {
        messageSnapshotListener?.remove()
        messageSnapshotListener = null
    }

    private fun getListQuery(): Query {
        val firestore = FirebaseFirestore.getInstance()

        return firestore.collection(Firebase.COLLECTION_CHATS)
            .document(chatRoom?.id!!)
            .collection(Firebase.COLLECTION_MESSAGES)
            .orderBy("sentTime", Query.Direction.DESCENDING)
    }

    /*********************************************************************************************
     * FIREBASE DB
     *********************************************************************************************/

    private fun getOwnerFromDb() {
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

    private fun getBuyerFromDb() {
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

    private fun getProductFromDb() {
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

    private fun saveMessageToDb(message: String) {
        val firestore = FirebaseFirestore.getInstance()

        val newMessage = Message(
            message = message,
            userId = LoginInfo.user?.id,
            sentTime = Calendar.getInstance().time
        )

        firestore.collection(Firebase.COLLECTION_CHATS)
            .document(chatRoom?.id!!)
            .collection(Firebase.COLLECTION_MESSAGES)
            .add(newMessage)
    }
}