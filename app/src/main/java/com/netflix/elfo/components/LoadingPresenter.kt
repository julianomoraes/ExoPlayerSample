package com.netflix.elfo.components

import android.annotation.SuppressLint
import com.netflix.elfo.ElfoEvent
import com.netflix.elfo.EventBusFactory
import io.reactivex.rxkotlin.subscribeBy

@SuppressLint("CheckResult")
class LoadingPresenter(uiView: LoadingUIView<LoadingEvents>, bus: EventBusFactory) {
    init {
        bus.getSafeManagedObservable(LoadingEvents::class.java)
            .subscribeBy(
                onNext = {
                    when (it) {
                        LoadingEvents.Loading -> {
                            uiView.show()
                        }
                        LoadingEvents.Loaded -> {
                            uiView.hide()
                        }
                    }
                },
                onError = {
                    uiView.hide()
                }
            )
    }
}

sealed class LoadingEvents : ElfoEvent() {
    object Loading : LoadingEvents()
    object Loaded : LoadingEvents()
}