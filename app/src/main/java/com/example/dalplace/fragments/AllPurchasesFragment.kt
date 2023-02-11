package com.example.dalplace.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dalplace.R
import com.example.dalplace.adapter.AllPurchaseQRAdapter
import com.example.dalplace.model.Product
import com.example.dalplace.model.Purchase
import com.example.dalplace.utilities.Firebase
import com.google.firebase.firestore.FirebaseFirestore

class AllPurchasesFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var list = mutableListOf<Purchase>()
    private var adapter = AllPurchaseQRAdapter(list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_all_purchases, container, false)
        recyclerView = view.findViewById(R.id.rcv_all_purchases)
        if (recyclerView == null) {

            println(" recycler view is null")
        }

        recyclerView?.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView?.adapter = adapter

        getData()

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_action -> {
                var searchView = MenuItemCompat.getActionView(item) as SearchView

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {

                        adapter?.filter?.filter(newText)
                        return false
                    }
                })

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun getData() {
        FirebaseFirestore.getInstance().collection("purchases")
            .whereEqualTo("buyerId", Firebase.userId).get()
            .addOnSuccessListener { result ->
                println("DocumentSnapshot data: ${result.size()}")
                for (document in result) {
                    list.add(
                        Purchase(
                            document.data["id"].toString(),
                            document.data["buyerId"].toString(),
                            document.data["ownerId"].toString(),
                            document.data["productId"].toString(),
                            document.data["productTitle"].toString(),
                            (document.data["price"]).toString().toDoubleOrNull(),
                            document.data["isQRValidated"].toString().toBoolean(),
                            document.data["date"].toString(),
                            (document.data["isSold"]).toString().toBoolean()
                        )
                    )

                }
                println("DocumentSnapshot data: ${list.size}")
                adapter.notifyDataSetChanged()
            }
    }


}

