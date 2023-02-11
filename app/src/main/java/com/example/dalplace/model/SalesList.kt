package com.example.dalplace.model

object SalesList {
    val salesList = mutableListOf<SalesData>()

    fun add(sales: SalesData) {
        if (!salesList.contains(sales)) {
            salesList.add(sales)
        }
    }

    fun getAllSales(): MutableList<SalesData> {
        return salesList
    }

    fun clearList(): MutableList<SalesData> {
        salesList.clear()
        return salesList
    }
}