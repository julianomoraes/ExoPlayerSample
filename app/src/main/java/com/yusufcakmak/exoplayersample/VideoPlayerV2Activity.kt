package com.yusufcakmak.exoplayersample

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
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
import com.netflix.elfo.components.LoadingEvents
import com.netflix.elfo.components.LoadingPresenter
import com.netflix.elfo.components.LoadingUIView
import com.netflix.elfo.emit

class VideoPlayerV2Activity : AppCompatActivity() {
    private var player: SimpleExoPlayer? = null
    private val playerView: PlayerView by lazy { findViewById<PlayerView>(R.id.player_view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player_v2)
        val rootViewContainer = findViewById<ViewGroup>(android.R.id.content)

        LoadingPresenter(LoadingUIView(rootViewContainer), EventBusFactory.get(this))
        emit<LoadingEvents>(LoadingEvents.Loading)
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

        val mediaSource = ExtractorMediaSource.Factory(
            DefaultDataSourceFactory(
                this, Util.getUserAgent(this, "mediaPlayerSample"),
                DefaultBandwidthMeter() as TransferListener<in DataSource>
            )
        )
            .createMediaSource(Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"))

        player?.prepare(mediaSource, true, false)
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
                }
                // The player is able to immediately play
                Player.STATE_READY -> {
                    emit<LoadingEvents>(LoadingEvents.Loaded)
                }
                // The player has finished playing the media
                Player.STATE_ENDED -> {
                    // TODO
                }
            }
        }
    }
}