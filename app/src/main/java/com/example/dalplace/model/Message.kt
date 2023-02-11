package com.example.dalplace.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Message(
    @DocumentId var id: String? = null,
    var message: String? = null,
    var userId: String? = null,
    var sentTime: Date? = null
) : Parcelable