package com.example.dalplace.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.dalplace.R
import com.example.dalplace.databinding.RvProductBinding
import com.example.dalplace.fragments.HomeFragmentDirections
import com.example.dalplace.model.Product
import com.example.dalplace.utilities.Formatter
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class ProductAdapter(var products: MutableList<Product>) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>(), Filterable {

    var originalList = ArrayList(products)
    var listFilter = ArrayList(products)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RvProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.bindItem(product)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun updateAdapter(updateAll: Boolean = false) {
        originalList = ArrayList(products)
        if (updateAll) {
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(private val binding: RvProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindItem(product: Product) {
            val displayText = "${Formatter.currency(product.price)} - ${product.title}"
            binding.tvDisplayText.text = displayText

            // Load Image
            Picasso.get()
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_baseline_image_24)
                .error(R.drawable.ic_baseline_error_outline_24)
                .into(binding.ivProductImage)

            // Click Listener
            binding.rvProduct.setOnClickListener { onItemClicked(product, it) }
        }

        private fun onItemClicked(product: Product, view: View?) {
            val action = HomeFragmentDirections.actionHomeToProductDetails(product)
            Navigation.findNavController(view!!).navigate(action)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                listFilter = ArrayList(originalList)
                Log.d("FilterResultTest", "Orignial Size: ${originalList.size}")
                var filterResults = FilterResults()
                var searchChar = constraint.toString().trim().toLowerCase(Locale.ROOT)
                var resultData: MutableList<Product> = ArrayList()
                for (product in listFilter) {
                    if (product.title!!.toLowerCase(Locale.ROOT).contains(searchChar) ||
                        product.description!!.toLowerCase(Locale.ROOT).contains(searchChar) ||
                        product.category!!.toLowerCase(Locale.ROOT).contains(searchChar)
                    ) {
                        resultData.add(product)
                    }
                }
                filterResults.count = resultData.size
                filterResults.values = resultData
                println(filterResults)

                return filterResults
            }


            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                products = results?.values as MutableList<Product>
                println("list = $products")
                notifyDataSetChanged()
            }

        }
    }
}
