package com.example.dalplace.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.example.dalplace.model.SalesData
import android.view.ViewGroup
import com.example.dalplace.activities.ConfirmCheckout

import com.example.dalplace.databinding.RvViewDailySalesBinding
import com.example.dalplace.model.Sales
import com.example.dalplace.model.SalesList
import com.google.firebase.firestore.FirebaseFirestore

class ViewDailySalesAdapter() :
    RecyclerView.Adapter<ViewDailySalesAdapter.ViewHolder>() {

    private val dailySalesList = SalesList.getAllSales()
    lateinit var activity: Activity
    lateinit var db: FirebaseFirestore
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewDailySalesAdapter.ViewHolder {
        return ViewHolder(
            RvViewDailySalesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }


    override fun onBindViewHolder(holder: ViewDailySalesAdapter.ViewHolder, position: Int) {
        val sales = dailySalesList[position]
        holder.bindItem(sales)
    }

    override fun getItemCount(): Int {
        return dailySalesList.size
    }

    inner class ViewHolder(val binding: RvViewDailySalesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindItem(sales: SalesData) {
            activity = binding.root.context as Activity
            db = FirebaseFirestore.getInstance()
            binding.tvSalesProductTitle.text = "Product: " + sales.productTitle
            binding.tvSalesPrice.text = "Price: " + sales.amount
            binding.tvSalesOwner.text = "Owner: " + sales.ownerId
            binding.tvSalesBuyer.text = "Buyer: " + sales.buyerId
            binding.tvSalesDate.text = "Date: " + sales.sellingDate
            if (sales.isDelivered == true) {
                binding.tvSalesDeliveryStatus.text = "Delivery Status: Delivered"
            } else {
                binding.tvSalesDeliveryStatus.text = "Delivery Status: Not Delivered"
            }


            binding.btnPaySeller.setOnClickListener {

                if (sales.isDelivered == true) {
                    val intent = Intent(activity, ConfirmCheckout::class.java)
                    intent.putExtra("productId", sales.productId)
                    activity.startActivity(intent)
                }
            }

        }

    }


}