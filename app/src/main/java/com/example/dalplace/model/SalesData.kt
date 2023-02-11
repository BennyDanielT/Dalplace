package com.example.dalplace.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class SalesData (
    @DocumentId var id: String? = null,
    var buyerId: String? = null,
    var productTitle: String? = null,
    var amount : String? = null,
    var isDelivered : Boolean? = null,
    var ownerId : String? = null,
    var productId : String? = null,
    var sellingDate : String? = null,
    var sellerPhoneNumber:String? =null
) : Parcelable