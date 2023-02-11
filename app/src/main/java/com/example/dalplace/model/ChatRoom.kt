package com.example.dalplace.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class ChatRoom(
    @DocumentId var id: String? = null,
    var productId: String? = null,
    var ownerId: String? = null,
    var buyerId: String? = null,
    var startTime: Date? = null
) : Parcelable