package com.example.dalplace.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @DocumentId var id: String? = null,
    var email: String? = null,
    var name: String? = null,
    var bannerId: String? = null,
    var isAdmin: Boolean? = null,
    var phoneNumber : String? = null
) : Parcelable