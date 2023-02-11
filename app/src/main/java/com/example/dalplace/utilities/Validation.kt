package com.example.dalplace.utilities

import android.util.Patterns
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

object Validation {

    fun validString(str: String?): Boolean {
        return !str.isNullOrBlank()
    }

    fun validEmail(email: String): Boolean {
        return (validString(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches())
    }

    fun validDalEmail(email: String): Boolean {
        val domain = email.takeLast(6)
        return (validEmail(email) && domain.equals("dal.ca", true))
    }

    /*********************************************************************************************
     * VALIDATE TEXT INPUT LAYOUT
     *********************************************************************************************/

    fun validateTilStringInput(view: TextInputLayout?): Boolean {
        val str = view?.editText?.text.toString()
        if (!validString(str)) {
            view?.helperText = "Required"
            view?.error = "Required"
            return false
        } else {
            view?.error = null
            view?.helperText = null
            return true
        }
    }

    fun validateTietStringInput(view: TextInputEditText?): Boolean {
        val str = view?.text.toString()
        return if (!validString(str)) {
            view?.error = "Required"
            false
        } else {
            view?.error = null
            true
        }
    }

    fun validateTilCurrencyInput(view: TextInputLayout?): Boolean {
        if (validateTilStringInput(view)) {
            val currency = view?.editText?.text.toString().toDoubleOrNull()
            currency?.let {
                if (currency < 0) {
                    view?.helperText = "Invalid Amount"
                    view?.error = "Invalid Amount"
                } else {
                    view?.error = null
                    view?.helperText = null
                    return true
                }
            }
        }
        return false
    }

    fun validateTilPhoneInput(view: TextInputLayout?): Boolean {
        if (validateTilStringInput(view)) {
            val phoneNumber = view?.editText?.text.toString()
            phoneNumber.let {
                if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
                    view?.helperText = "Invalid Phone Number"
                    view?.error = "Invalid Phone Number"
                } else {
                    view?.error = null
                    view?.helperText = null
                    return true
                }
            }
        }
        return false
    }
}