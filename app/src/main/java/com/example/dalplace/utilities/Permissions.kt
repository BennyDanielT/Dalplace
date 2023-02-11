package com.example.dalplace.utilities

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object Permissions {

    /*********************************************************************************************
     * CAMERA PERMISSIONS
     *********************************************************************************************/

    fun hasCameraPermission(context: Context): Boolean {
        return (
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED)
    }

    fun requestCameraPermission(activity: Activity, permissionRequestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                android.Manifest.permission.CAMERA
            ),
            permissionRequestCode
        )
    }
}