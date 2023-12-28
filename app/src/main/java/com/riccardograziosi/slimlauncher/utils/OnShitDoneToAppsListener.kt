package com.riccardograziosi.slimlauncher.utils

import android.view.View
import com.riccardograziosi.slimlauncher.models.HomeApp

interface OnShitDoneToAppsListener {
    fun onAppsUpdated(list: List<HomeApp>)
    fun onAppMenuClicked(view: View, app: HomeApp)
}