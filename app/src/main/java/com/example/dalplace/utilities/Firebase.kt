package com.example.dalplace.utilities

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object Firebase {

    private val auth = FirebaseAuth.getInstance()

    /*********************************************************************************************
     * FIREBASE COLLECTIONS
     *********************************************************************************************/

    const val COLLECTION_USERS = "users"
    const val COLLECTION_PRODUCTS = "products"
    const val COLLECTION_PRODUCT_CATEGORIES = "productCategories"
    const val COLLECTION_SALES = "sales"
    const val COLLECTION_FEEDBACKS = "feedbacks"
    const val COLLECTION_CHATS = "chats"
    const val COLLECTION_MESSAGES = "messages"
    const val COLLECTION_PURCHASES = "purchases"

    /*********************************************************************************************
     * FIREBASE BUCKETS
     *********************************************************************************************/

    const val BUCKET_PRODUCT = "products"

    /*********************************************************************************************
     * AUTHENTICATIONS
     *********************************************************************************************/

    val firebaseUser: FirebaseUser?
        get() {
            return auth.currentUser
        }

    val userId: String?
        get() {
            return auth.currentUser?.uid
        }

    val isUserAuthenticated: Boolean
        get() {
            return auth.currentUser != null
        }

    val userEmail: String?
        get() {
            return auth.currentUser?.email
        }

    val isUserEmailVerified: Boolean
        get() {
            return auth.currentUser?.isEmailVerified ?: false
        }

    fun logoutUser() {
        auth.signOut()
    }

    /*********************************************************************************************
     * FireStore
     *********************************************************************************************/


    /*********************************************************************************************
     * Storage
     *********************************************************************************************/
}
