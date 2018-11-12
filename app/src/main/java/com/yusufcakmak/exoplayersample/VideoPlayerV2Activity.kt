package com.yusufcakmak.exoplayersample

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import com.netflix.elfo.EventBusFactory
import com.netflix.elfo.components.*
import com.netflix.elfo.emit
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class VideoPlayerV2Activity : AppCompatActivity() {
    private var player: SimpleExoPlayer? = null
    private val playerView: PlayerView by lazy { findViewById<PlayerView>(R.id.player_view2) }
    private lateinit var primaryControlsUIView: PrimaryControlsUIView<PlayerEvents>
    private lateinit var playbackSurfaceUIView: PlaybackSurfaceUIView<PlayerEvents>

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player_v2)
        val rootViewContainer = findViewById<ConstraintLayout>(R.id.root)

        initializeUIComponents(rootViewContainer)
        layoutUIComponents(rootViewContainer)

        EventBusFactory.get(this).getSafeManagedObservable(PlayerUserInteractionEvents::class.java)
            .subscribe { event ->
                when (event) {
                    PlayerUserInteractionEvents.IntentPlayPauseClicked -> {
                        player?.playWhenReady = !player?.playWhenReady!!
                    }
                    PlayerUserInteractionEvents.IntentRwClicked -> {
                        player?.seekTo(player?.currentPosition!! - 30)
                    }
                    PlayerUserInteractionEvents.IntentFwClicked -> {
                        player?.seekTo(player?.currentPosition!! + 30)
                    }
                }
            }
    }

    private fun initializeUIComponents(rootViewContainer: ConstraintLayout) {

        playbackSurfaceUIView = PlaybackSurfaceUIView(rootViewContainer)
        PlaybackSurfacePresenter(playbackSurfaceUIView, EventBusFactory.get(this))

        // Player controls UI component
        primaryControlsUIView = PrimaryControlsUIView(rootViewContainer, EventBusFactory.get(this))
        PrimaryControlsPresenter(primaryControlsUIView, EventBusFactory.get(this))

        // Loading UI component
        LoadingPresenter(
            LoadingUIView(rootViewContainer),
            EventBusFactory.get(this)
        )
    }

    private fun layoutUIComponents(rootViewContainer: ConstraintLayout) {
        // Create a constraintSet and clone it from the main container
        val mainContainerConstraintSet = ConstraintSet()
        mainContainerConstraintSet.clone(rootViewContainer)

        mainContainerConstraintSet.connect(
            primaryControlsUIView.containerId,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        mainContainerConstraintSet.connect(
            primaryControlsUIView.containerId,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM
        )

        // Apply the constraints from all UI components to the parent
        mainContainerConstraintSet.applyTo(rootViewContainer)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) releasePlayer()
    }

    private fun initializePlayer() {
        playerView.requestFocus()
        player = ExoPlayerFactory.newSimpleInstance(
            this,
            DefaultTrackSelector(AdaptiveTrackSelection.Factory(DefaultBandwidthMeter()))
        )

        player?.let {
            playerView.player = it
            it.addListener(PlayerEventListener())
        }

        // artificial delay to simulate a network request
        Observable.just(Any())
            .delay(10, TimeUnit.SECONDS)
            .doOnNext {
                val mediaSource = ExtractorMediaSource.Factory(
                    DefaultDataSourceFactory(
                        this, Util.getUserAgent(this, "mediaPlayerSample"),
                        DefaultBandwidthMeter() as TransferListener<in DataSource>
                    )
                ).createMediaSource(Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"))

                player?.prepare(mediaSource, false, false)
            }
            .subscribe()

        player?.playWhenReady = true
    }

    private fun releasePlayer() {
        player?.let {
            it.release()
            player = null
        }
    }

    private inner class PlayerEventListener : Player.DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                // The player does not have any media to play yet.
                Player.STATE_IDLE -> {
                    emit<LoadingEvents>(LoadingEvents.Loading)
                }
                // The player is buffering (loading the content)
                Player.STATE_BUFFERING -> {
                    emit<LoadingEvents>(LoadingEvents.Loading)
                    emit<PlayerEvents>(PlayerEvents.Buffering)
                }
                // The player is able to immediately play
                Player.STATE_READY -> {
                    emit<LoadingEvents>(LoadingEvents.Loaded)
                    if (playWhenReady) {
                        emit<PlayerEvents>(PlayerEvents.PlayStarted)
                    } else {
                        emit<PlayerEvents>(PlayerEvents.Paused)
                    }
                }
                // The player has finished playing the media
                Player.STATE_ENDED -> {
                    // TODO
                }
            }
        }
    }
}