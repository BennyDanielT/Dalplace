package com.example.dalplace.model

import com.google.firebase.firestore.DocumentId
import java.util.*

data class Purchase(
    @DocumentId val id: String? = null,
    val buyerId: String? = null,
    val ownerId: String? = null,
    val productId: String? = null,
    val productTitle: String? = null,
    val price: Double? = null,
    val isQRValidated: Boolean? = null,
    val date: String? = null,
    val isSold: Boolean? = null,
    val buyerEmail: String? = null,
    val buyerName: String? = null,
    val buyerPhoneNumber: String? = null,
    val sellerEmail: String? = null,
    val sellerPhoneNumber: String? = null,
    val sellerPaid: Boolean? = null,
)