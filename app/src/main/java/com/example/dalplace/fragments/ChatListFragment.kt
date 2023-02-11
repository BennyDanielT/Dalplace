package com.example.dalplace.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dalplace.R
import com.example.dalplace.adapter.ChatAdapter
import com.example.dalplace.databinding.FragmentChatListBinding
import com.example.dalplace.databinding.FragmentHomeBinding
import com.example.dalplace.model.ChatRoom
import com.example.dalplace.model.Product
import com.example.dalplace.utilities.Firebase
import com.example.dalplace.utilities.LoginInfo
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatListFragment : Fragment() {

    var progressDialog: ProgressDialog? = null
    var binding: FragmentChatListBinding? = null
    var chatsSnapshotListener: ListenerRegistration? = null

    private var chats = mutableListOf<ChatRoom>()
    private var chatAdapter = ChatAdapter(chats)

    /*********************************************************************************************
     * FRAGMENT LIFECYCLES
     *********************************************************************************************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addChatsChangeListener(isRequestMode = true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatListBinding.inflate(layoutInflater)
        initViews()
        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        removeChatsChangeListener()
    }

    /*********************************************************************************************
     * VIEW/WIDGET INITIALIZATION
     *********************************************************************************************/

    private fun initViews() {
        initProgressDialog()
        initRvChats()
        initToggleButtonGroup()
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

    private fun initRvChats() {
        binding?.rvChats?.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding?.rvChats?.setHasFixedSize(true)
        binding?.rvChats?.adapter = chatAdapter
    }

    private fun addChatsChangeListener(isRequestMode: Boolean = true) {
        chats = mutableListOf()
        chatAdapter.chatRooms = chats
        chatAdapter.notifyDataSetChanged()
        chatsSnapshotListener?.let {
            removeChatsChangeListener()
        }


        val query = getListQuery(isRequestMode)
        showProgressDialog("Loading Products...")
        Log.d("ChatList", "chatsSnapshotListener: addSnapshotListener()")
        chatsSnapshotListener = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.d("ChatList", "Error: ${error.message}")
                return@addSnapshotListener
            }

            Log.d("ChatList", "Size: ${snapshots!!.documentChanges.size}")
            for (documentChange in snapshots!!.documentChanges) {

                val chatRoom = documentChange.document.toObject(ChatRoom::class.java)
                when (documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        chats?.add(documentChange.newIndex, chatRoom)
                        chatAdapter.notifyItemInserted(documentChange.newIndex)
                        Log.d("ProductsList", "DocumentChange: ADDED")
                    }

                    DocumentChange.Type.MODIFIED -> {
                        chats?.set(documentChange.newIndex, chatRoom)
                        chatAdapter.notifyItemChanged(documentChange.newIndex)
                        Log.d("ProductsList", "DocumentChange: MODIFIED")
                    }

                    DocumentChange.Type.REMOVED -> {
                        chats?.removeAt(documentChange.oldIndex)
                        chatAdapter.notifyItemRemoved(documentChange.oldIndex)
                        Log.d("ProductsList", "DocumentChange: REMOVED")
                    }
                }
            }
            hideProgressDialog()
        }
    }

    private fun removeChatsChangeListener() {
        chatsSnapshotListener?.remove()
        chatsSnapshotListener = null
    }

    /*********************************************************************************************
     * TOGGLE BUTTON GROUP IMPLEMENTATION
     *********************************************************************************************/

    private fun initToggleButtonGroup() {
        // Setting Toggle Group Buttons
        binding?.btnToggleGroup?.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            if (checkedId == R.id.btnToggleRequests)
                addChatsChangeListener(isRequestMode = true)

            if (checkedId == R.id.btnToggleProducts)
                addChatsChangeListener(isRequestMode = false)
        }
    }

    private fun getListQuery(isRequestMode: Boolean): Query {
        val firestore = FirebaseFirestore.getInstance()

        return if (isRequestMode) {
            firestore.collection(Firebase.COLLECTION_CHATS)
                .whereEqualTo("buyerId", LoginInfo.user?.id!!)
                .orderBy("startTime", Query.Direction.DESCENDING)
        } else {
            firestore.collection(Firebase.COLLECTION_CHATS)
                .whereEqualTo("ownerId", LoginInfo.user?.id!!)
                .orderBy("startTime", Query.Direction.DESCENDING)
        }
    }
}