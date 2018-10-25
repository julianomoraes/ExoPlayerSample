package com.netflix.elfo.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netflix.elfo.ElfoView
import com.yusufcakmak.exoplayersample.R

class PrimaryControlsUIView<T>(container: ViewGroup) : ElfoView<T>(container) {
    private val uiView: View =
        LayoutInflater.from(container.context).inflate(R.layout.primary_controls, container, false)

    init {
        container.addView(uiView)
    }

    override val containerId: Int = uiView.id

    override fun show() {
        uiView.visibility = View.VISIBLE
    }

    override fun hide() {
        uiView.visibility = View.GONE
    }

}
