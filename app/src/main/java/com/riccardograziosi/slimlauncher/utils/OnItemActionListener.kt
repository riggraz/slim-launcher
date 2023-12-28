package com.riccardograziosi.slimlauncher.utils

interface OnItemActionListener {
    fun onViewMoved(oldPosition: Int, newPosition: Int): Boolean
    fun onViewSwiped(position: Int)
    fun onViewIdle()
}