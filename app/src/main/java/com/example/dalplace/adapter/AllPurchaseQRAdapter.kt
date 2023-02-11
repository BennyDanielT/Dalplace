package com.example.dalplace.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.dalplace.model.Purchase
import com.example.dalplace.databinding.RvPurchaseBinding
import com.example.dalplace.utilities.QrCodeUtils
import java.util.*
import kotlin.collections.ArrayList

class AllPurchaseQRAdapter(var list: MutableList<Purchase>) :
    RecyclerView.Adapter<AllPurchaseQRAdapter.ViewHolder>(), Filterable {

    var listFilter: MutableList<Purchase> = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            RvPurchaseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun getItemCount(): Int {

        return list.size

    }

    inner class ViewHolder(private val binding: RvPurchaseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindItem(product: Purchase) {

            binding.imgPurchaseQR.setImageBitmap(QrCodeUtils.generateQrCode(product.buyerId + "+++" + product.ownerId + "+++" + product.productId))
            binding.txtTitleQR.text = "Product Title: ${product.productTitle}"
            binding.txtIdQR.text = "Product ID: ${product.productId}"
            binding.txtQRPrice.text = "Product Price: ${product.price.toString()}"
        }

    }

    override fun onBindViewHolder(holder: AllPurchaseQRAdapter.ViewHolder, position: Int) {
        val purchase = list[position]
        holder.bindItem(purchase)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                var filterResults = FilterResults()
                if (constraint == null || constraint.isEmpty()) {
                    filterResults.count = listFilter.size
                    filterResults.values = listFilter
                } else {
                    var searchChar = constraint.toString().toLowerCase(Locale.ROOT)
                    var resultData: MutableList<Purchase> = ArrayList()
                    for (purchase in listFilter) {
                        if (purchase.productTitle!!.toLowerCase(Locale.ROOT)
                                .contains(searchChar) || purchase.productId!!.toLowerCase(Locale.ROOT)
                                .contains(searchChar)
                        ) {
                            resultData.add(purchase)
                        }
                    }
                    filterResults.count = resultData.size
                    println("filterResults.count = ${filterResults.count}")
                    filterResults.values = resultData
                    println("filterResults.values = ${filterResults.values}")
                }
                return filterResults
            }


            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                list = results?.values as MutableList<Purchase>
                println("list = ${list}")
                notifyDataSetChanged()
            }

        }
    }

}