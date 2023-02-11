package com.example.dalplace.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.dalplace.R
import com.example.dalplace.activities.ConfirmCheckout
import com.example.dalplace.databinding.FragmentProductDetailsBinding
import com.example.dalplace.model.ChatRoom
import com.example.dalplace.model.Product
import com.example.dalplace.model.User
import com.example.dalplace.utilities.Firebase
import com.example.dalplace.utilities.Formatter
import com.example.dalplace.utilities.LoginInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.util.*


class ProductDetailsFragment : Fragment(), OnClickListener {

    private var binding: FragmentProductDetailsBinding? = null
    private var selectedProduct: Product? = null
    var progressDialog: ProgressDialog? = null

    /*********************************************************************************************
     * FRAGMENT LIFECYCLES
     *********************************************************************************************/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProductDetailsBinding.inflate(layoutInflater)
        initViews()

        return binding?.root
    }

    /*********************************************************************************************
     * VIEW/WIDGET INITIALIZATION
     *********************************************************************************************/

    private fun initViews() {
        getFragmentArguments()
        initProgressDialog()

        // Setup Image Holder
        Picasso.get()
            .load(selectedProduct?.imageUrl)
            .placeholder(R.drawable.ic_baseline_image_24)
            .error(R.drawable.ic_baseline_error_outline_24)
            .into(binding?.ivProductImage)

        binding?.tvTitle?.text = selectedProduct?.title
        binding?.tvPrice?.text = Formatter.currency(selectedProduct?.price)
        binding?.tvCategory?.text = selectedProduct?.category
        binding?.tvLastUpdated?.text = Formatter.friendlyDate(selectedProduct?.lastUpdated)
        binding?.tvDescription?.text = selectedProduct?.description
        getOwnerFromDb()

        // Button Click Listeners
        binding?.btnBuy?.setOnClickListener(this)
        binding?.btnChat?.setOnClickListener(this)
        binding?.btnDelete?.setOnClickListener(this)
        binding?.btnEdit?.setOnClickListener(this)

        binding?.btnChat?.isEnabled = selectedProduct?.isChatEnabled!!

        val isOwner = selectedProduct?.ownerId == Firebase.userId
        setBtnMode(isOwner)
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

    private fun getFragmentArguments() {
        arguments?.let {
            selectedProduct = ProductDetailsFragmentArgs.fromBundle(it).selectedProduct
        }
    }

    private fun setBtnMode(isOwner: Boolean = false) {
        if (isOwner) {
            binding?.btnBuy?.visibility = GONE
            binding?.btnChat?.visibility = GONE
            binding?.btnDelete?.visibility = VISIBLE
            binding?.btnEdit?.visibility = VISIBLE
        } else {
            binding?.btnBuy?.visibility = VISIBLE
            binding?.btnChat?.visibility = VISIBLE
            binding?.btnDelete?.visibility = GONE
            binding?.btnEdit?.visibility = GONE
        }
    }

    private fun getOwnerFromDb() {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection(Firebase.COLLECTION_USERS)
            .document(selectedProduct?.ownerId!!)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val owner = task.result.toObject(User::class.java)
                    var ownerDetails = "Seller:"
                    if (selectedProduct?.isUrgentRequest!!) {
                        ownerDetails = """Seller: ${owner?.name}
                            |Phone Number: ${Formatter.phone(owner?.phoneNumber)}
                        """.trimMargin()
                    } else {
                        ownerDetails = "Seller: ${owner?.name}"
                    }

                    binding?.tvOwner?.text = ownerDetails
                }
            }
    }

    /*********************************************************************************************
     * ONCLICK LISTENER IMPLEMENTATION
     *********************************************************************************************/

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnBuy -> {

                val intent = Intent(activity, ConfirmCheckout::class.java)

                intent.putExtra("productId", selectedProduct?.id.toString())
                startActivity(intent)
            }
            R.id.btnChat -> {
                getChatRoom()
            }
            R.id.btnDelete -> {
                showDeleteConfirmationAlertBox()
            }
            R.id.btnEdit -> {
                navigateToEditProduct()
            }
        }
    }

    private fun deleteProduct() {
        val firestore = FirebaseFirestore.getInstance()

        showProgressDialog("Deleting Product...")
        firestore.collection(Firebase.COLLECTION_PRODUCTS)
            .document(selectedProduct?.id!!)
            .update("deleted", true)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Product Deleted", Toast.LENGTH_SHORT).show()
                    navigateToHomeProduct()
                }
                hideProgressDialog()
            }
    }

    private fun showDeleteConfirmationAlertBox() {
        AlertDialog.Builder(context)
            .setTitle("Are you sure?")
            .setMessage("Delete Product?")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                deleteProduct()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                // Close Dialog Box
            }
            .show()
    }

    private fun getChatRoom() {
        val firestore = FirebaseFirestore.getInstance()

        showProgressDialog("Opening Chat...")
        firestore.collection(Firebase.COLLECTION_CHATS)
            .whereEqualTo("productId", selectedProduct?.id!!)
            .whereEqualTo("buyerId", LoginInfo.user?.id!!)
            .limit(1)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatRooms = task.result.toObjects(ChatRoom::class.java)
                    if (chatRooms.isEmpty()) {
                        createChatRoom()
                    } else {
                        hideProgressDialog()
                        navigateToChatRoom(chatRooms[0])
                    }
                } else {
                    Toast.makeText(context, "Network Error! Try Again Later", Toast.LENGTH_SHORT)
                        .show()
                    hideProgressDialog()
                }
            }
    }

    private fun createChatRoom() {
        val firestore = FirebaseFirestore.getInstance()

        val chatRoom = ChatRoom(
            productId = selectedProduct?.id,
            ownerId = selectedProduct?.ownerId,
            buyerId = LoginInfo.user?.id,
            startTime = Calendar.getInstance().time
        )
        firestore.collection(Firebase.COLLECTION_CHATS)
            .add(chatRoom)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    chatRoom.id = task.result.id
                    navigateToChatRoom(chatRoom)
                } else {
                    Toast.makeText(context, "Network Error! Try Again Later", Toast.LENGTH_SHORT)
                        .show()
                }
                hideProgressDialog()
            }
    }

    /*********************************************************************************************
     * NAVIGATION
     *********************************************************************************************/

    private fun navigateToEditProduct() {
        val isNew = false
        val action =
            ProductDetailsFragmentDirections.actionProductDetailsToAddEditProduct(
                isNew = isNew,
                selectedProduct = selectedProduct
            )
        Navigation.findNavController(binding?.root!!).navigate(action)
    }

    private fun navigateToChatRoom(chatRoom: ChatRoom) {
        val action =
            ProductDetailsFragmentDirections.actionProductDetailsToChatRoom(
                chatRoom = chatRoom
            )
        Navigation.findNavController(binding?.root!!).navigate(action)
    }

    private fun navigateToHomeProduct() {
        Navigation.findNavController(binding?.root!!).popBackStack()
    }
}