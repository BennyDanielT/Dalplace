package com.example.dalplace.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Product(
    @DocumentId var id: String? = null,
    var ownerId: String? = null,
    var category: String? = null,
    var title: String? = null,
    var description: String? = null,
    var price: Double? = null,
    var imageUrl: String? = null,
    var isChatEnabled: Boolean = false,
    var isUrgentRequest: Boolean = false,
    var isSold: Boolean = false,
    var isDelivered: Boolean = false,
    var isDeleted: Boolean = false,
    var lastUpdated: Date? = null,


    @get:Exclude
    var owner: User? = null
) : Parcelable