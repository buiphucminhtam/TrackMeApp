package com.fossil.trackme.utils

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.fossil.trackme.R


/**
 * Get ViewModel from Fragment
 */
internal fun <VM : ViewModel> Fragment.getViewModel(
    vmClass: Class<VM>,
    vmFactory: ViewModelProvider.Factory? = null
): VM {
    return vmFactory?.let { ViewModelProviders.of(this, it).get(vmClass) } ?: ViewModelProviders.of(
        this
    ).get(vmClass)
}

/**
 * Get ViewModel from Fragment with key
 */
internal fun <VM : ViewModel> Fragment.getViewModel(
    key: String,
    vmClass: Class<VM>,
    vmFactory: ViewModelProvider.Factory? = null
): VM {
    return vmFactory?.let { ViewModelProviders.of(this, it).get(key, vmClass) }
        ?: ViewModelProviders.of(this).get(
            key,
            vmClass
        )
}

/**
 * Get ViewModel from Activity
 */
internal fun <VM : ViewModel> FragmentActivity.getViewModel(
    vmClass: Class<VM>,
    vmFactory: ViewModelProvider.Factory? = null
): VM {
    return vmFactory?.let { ViewModelProviders.of(this, it).get(vmClass) } ?: ViewModelProviders.of(
        this
    ).get(vmClass)
}

/**
 * Quick inflate view in fragment [onCreate] function
 */
internal fun Fragment.inflateView(
    viewId: Int,
    viewGroup: ViewGroup?,
    attachToRoot: Boolean = false
): View {
    return layoutInflater.inflate(viewId, viewGroup, attachToRoot)
}

internal fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
    observeForever(object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

const val MY_PERMISSIONS_REQUEST_LOCATION = 99

fun Activity.openAppPermissionSetting() {
    AlertDialog.Builder(this)
        .setTitle(R.string.title_disable_permission)
        .setMessage(R.string.text_location_open_detail_setting)
        .setPositiveButton(R.string.ok,
            DialogInterface.OnClickListener { dialogInterface, i -> //Prompt the user once explanation has been shown
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            })
        .create()
        .show()
}

fun Activity.checkLocationPermission(): Boolean {
    return if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        != PackageManager.PERMISSION_GRANTED
    ) {

        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            AlertDialog.Builder(this)
                .setTitle(R.string.title_location_permission)
                .setMessage(R.string.text_location_permission)
                .setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialogInterface, i -> //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    })
                .create()
                .show()
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
        false
    } else {
        true
    }
}
