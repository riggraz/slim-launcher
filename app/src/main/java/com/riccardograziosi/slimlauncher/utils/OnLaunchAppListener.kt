package com.riccardograziosi.slimlauncher.utils

import android.view.View
import com.riccardograziosi.slimlauncher.models.HomeApp

interface OnLaunchAppListener{
    fun onLaunch(app: HomeApp, view: View)
}