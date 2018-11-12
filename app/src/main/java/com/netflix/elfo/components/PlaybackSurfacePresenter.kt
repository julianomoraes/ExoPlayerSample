package com.netflix.elfo.components

import android.annotation.SuppressLint
import com.netflix.elfo.ElfoEvent
import com.netflix.elfo.EventBusFactory
import io.reactivex.rxkotlin.subscribeBy

@SuppressLint("CheckResult")
class PlaybackSurfacePresenter(uiView: PlaybackSurfaceUIView<PlayerEvents>, bus: EventBusFactory) {
    init {
        bus.getSafeManagedObservable(PlayerEvents::class.java)
            .filter { it is PlayerEvents.PlayStarted }
            .subscribe {
                uiView.show()
            }
    }
}