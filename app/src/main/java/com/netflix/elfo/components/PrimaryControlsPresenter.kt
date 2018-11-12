package com.netflix.elfo.components

import android.annotation.SuppressLint
import com.netflix.elfo.ElfoEvent
import com.netflix.elfo.EventBusFactory
import io.reactivex.rxkotlin.subscribeBy

@SuppressLint("CheckResult")
class PrimaryControlsPresenter<T>(uiView: PrimaryControlsUIView<T>, bus: EventBusFactory) {
    init {
        bus.getSafeManagedObservable(PlayerEvents::class.java)
            .subscribeBy(
                onNext = {
                    when (it) {
                        PlayerEvents.Buffering -> {
                            uiView.hide()
                        }
                        PlayerEvents.PlayStarted -> {
                            uiView.show()
                            uiView.setPlayPauseImageResource(true)
                        }
                        PlayerEvents.Paused -> {
                            uiView.show()
                            uiView.setPlayPauseImageResource(false)
                        }
                    }
                },
                onError = {
                    uiView.hide()
                }
            )
    }
}

sealed class PlayerEvents : ElfoEvent() {
    object Buffering : PlayerEvents()
    object PlayStarted : PlayerEvents()
    object Paused : PlayerEvents()
}