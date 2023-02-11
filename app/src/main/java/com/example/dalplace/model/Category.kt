package com.example.dalplace.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    @DocumentId val id: String? = null,
    val name: String? = null,
) : Parcelable {
    override fun toString(): String {
        return name ?: "Unknown Category"
    }
}
