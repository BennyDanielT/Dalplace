package com.example.dalplace.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dalplace.R
import com.example.dalplace.adapter.ViewDailySalesAdapter
import com.example.dalplace.model.Sales
import com.example.dalplace.model.SalesList

class AdminViewDailySalesFragment : Fragment() {

    private lateinit var tableRecyclerView: RecyclerView
    private val sales: Sales = Sales()
    private lateinit var tableRowAdapter: ViewDailySalesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_view_daily_sales, container, false)

        SalesList.clearList()

        tableRecyclerView = view.findViewById(R.id.table_recycler_view)
        tableRowAdapter = ViewDailySalesAdapter()
        tableRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        tableRecyclerView.setHasFixedSize(true)
        tableRecyclerView.adapter = tableRowAdapter

        sales.getSales {
            tableRowAdapter.notifyDataSetChanged()
        }

        return view
    }
}