package com.netflix.elfo

import android.support.annotation.IdRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class ElfoView<T>(val container: ViewGroup) {
    /**
     * Get the XML id for the IUIView
     */
    @get:IdRes
    abstract val containerId: Int

    /**
     * Show the IUIView
     */
    abstract fun show()

    /**
     * Hide the IUIView
     */
    abstract fun hide()
}