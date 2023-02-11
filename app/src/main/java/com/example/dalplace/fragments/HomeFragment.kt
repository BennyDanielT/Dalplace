package com.example.dalplace.fragments

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.OnClickListener
import android.widget.ProgressBar
import android.widget.SearchView
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dalplace.R
import com.example.dalplace.adapter.ProductAdapter
import com.example.dalplace.databinding.FragmentHomeBinding
import com.example.dalplace.model.Product
import com.example.dalplace.utilities.Firebase
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.button.MaterialButtonToggleGroup.OnButtonCheckedListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class HomeFragment : Fragment(), OnClickListener {

    var progressDialog: ProgressDialog? = null
    var binding: FragmentHomeBinding? = null
    var productSnapshotListener: ListenerRegistration? = null

    private var products = mutableListOf<Product>()
    private var productAdapter = ProductAdapter(products)

    /*********************************************************************************************
     * FRAGMENT LIFECYCLES
     *********************************************************************************************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu (true)
        addProductsChangeListener(isProductMode = true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_action -> {
                var searchView = MenuItemCompat.getActionView(item) as androidx.appcompat.widget.SearchView

                searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {

                        productAdapter?.filter?.filter(newText)
                        println("hua $newText")
                        return false
                    }
                })

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater)
        initViews()
        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        removeProductsChangeListener()
    }

    /*********************************************************************************************
     * VIEW/WIDGET INITIALIZATION
     *********************************************************************************************/

    private fun initViews() {
        initProgressDialog()
        initRvProducts()
        initToggleButtonGroup()

        // Setting on click listeners
        binding?.fabAddProduct?.setOnClickListener(this)
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

    private fun initRvProducts() {
        binding?.rvProducts?.layoutManager = GridLayoutManager(
            requireContext(),
            2
        )
        binding?.rvProducts?.setHasFixedSize(true)
        binding?.rvProducts?.adapter = productAdapter
    }

    private fun addProductsChangeListener(isProductMode: Boolean = true) {
        products = mutableListOf()
        productAdapter.products = products
        productAdapter.updateAdapter(true)
        productSnapshotListener?.let {
            removeProductsChangeListener()
        }


        val query = getListQuery(isProductMode)
        showProgressDialog("Loading Products...")
        Log.d("ProductsList", "productSnapshotListener: addSnapshotListener()")
        productSnapshotListener = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.d("ProductsList", "Error: ${error.message}")
                return@addSnapshotListener
            }

            Log.d("ProductsList", "Size: ${snapshots!!.documentChanges.size}")
            for (documentChange in snapshots!!.documentChanges) {

                val product = documentChange.document.toObject(Product::class.java)
                when (documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        products?.add(documentChange.newIndex, product)
                        productAdapter.notifyItemInserted(documentChange.newIndex)
                        productAdapter.updateAdapter()
                        Log.d("ProductsList", "DocumentChange: ADDED")
                    }

                    DocumentChange.Type.MODIFIED -> {
                        products?.set(documentChange.newIndex, product)
                        productAdapter.notifyItemChanged(documentChange.newIndex)
                        productAdapter.updateAdapter()
                        Log.d("ProductsList", "DocumentChange: MODIFIED")
                    }

                    DocumentChange.Type.REMOVED -> {
                        products?.removeAt(documentChange.oldIndex)
                        productAdapter.notifyItemRemoved(documentChange.oldIndex)
                        productAdapter.updateAdapter()
                        Log.d("ProductsList", "DocumentChange: REMOVED")
                    }
                }
            }
            hideProgressDialog()
        }
    }

    private fun removeProductsChangeListener() {
        productSnapshotListener?.remove()
        productSnapshotListener = null
    }

    /*********************************************************************************************
     * TOGGLE BUTTON GROUP IMPLEMENTATION
     *********************************************************************************************/

    private fun initToggleButtonGroup() {
        // Setting Toggle Group Buttons
        binding?.btnToggleGroup?.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            if (checkedId == R.id.btnToggleProducts)
                addProductsChangeListener(isProductMode = true)

            if (checkedId == R.id.btnToggleRequests)
                addProductsChangeListener(isProductMode = false)
        }
    }

    private fun getListQuery(isProductMode: Boolean = true): Query {
        val firestore = FirebaseFirestore.getInstance()

        return if (isProductMode) {
            firestore.collection(Firebase.COLLECTION_PRODUCTS)
                .whereEqualTo("urgentRequest", false)
                .whereEqualTo("sold", false)
                .whereEqualTo("deleted", false)
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
        } else {
            firestore.collection(Firebase.COLLECTION_PRODUCTS)
                .whereEqualTo("urgentRequest", true)
                .whereEqualTo("sold", false)
                .whereEqualTo("deleted", false)
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
        }
    }

    /*********************************************************************************************
     * ONCLICK LISTENER IMPLEMENTATION
     *********************************************************************************************/

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.fabAddProduct -> { // Add Product Button
                navigateToAddProduct(view)
            }

        }
    }

    /*********************************************************************************************
     * NAVIGATION
     *********************************************************************************************/

    private fun navigateToAddProduct(view: View) {
        val isNew = true
        val action =
            HomeFragmentDirections.actionHomeToAddEditProduct(isNew = isNew, selectedProduct = null)
        Navigation.findNavController(view).navigate(action)
    }
}
