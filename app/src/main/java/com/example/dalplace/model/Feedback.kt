package com.example.dalplace.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

/**
 * This is the model for the feedback given by the DalPlace users.
 */
@Parcelize
data class Feedback(
    @DocumentId var id: String? = null,
    var title: String? = null,
    var description: String? = null,
    var willRecommend: String? = null,
    var isComplaint: Boolean? = null,
    var rating: Float? = null,
) : Parcelable