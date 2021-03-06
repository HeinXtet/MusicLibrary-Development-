package com.heinhtet.deevd.servicesample

import android.content.BroadcastReceiver
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.support.v4.media.session.MediaButtonReceiver
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import com.google.android.exoplayer2.ExoPlayer
import com.heinhtet.deevd.servicesample.helper.MusicManager
import com.heinhtet.deevd.serviceeample.model.SongHelper
import com.heinhtet.deevd.servicesample.base.MediaPlayer
import com.heinhtet.deevd.servicesample.utils.FormatUtils
import kotlinx.android.synthetic.main.activity_main.*


data class MediaItem(var title: String, var path: String)


class MainActivity : AppCompatActivity(), MediaPlayer.MediaPlayerListener {


    lateinit var play: Button

    override fun onStateChanged(state: Int, playWhenReady: Boolean,item: MediaItem) {
        Log.i(TAG, " player state change $state $playWhenReady")
    }

    override fun trackChange(item: MediaItem) {
        Log.i(TAG, " track  change $item")
    }

    override fun onResume() {
        super.onResume()
        checkState(App.musicManager.isPlaying())

    }

    override fun progressChange(progress: Long, player: ExoPlayer) {
        Log.i(TAG, " progress change  change $progress ${player.duration}")
        seek_bar.max = player.duration.toInt()
        seek_bar.progress = progress.toInt()
        start_time.text = FormatUtils.formatMusicTime(progress)
        total_time.text = FormatUtils.formatMusicTime(player.duration)
    }

    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    App.musicManager.seekTo(p1.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
        play = findViewById(R.id.print_timestamp)
        play.setOnClickListener {
            App.musicManager.loadMediaItems()
            App.musicManager.play(state = {
                if (it) {
                    play.text = "pause"
                } else {
                    play.text = "play"
                }
            })
        }
        findViewById<Button>(R.id.next).setOnClickListener {
            App.musicManager.next()
        }
        findViewById<Button>(R.id.stop_service).setOnClickListener {
            App.musicManager.previous()
            //startActivity(Intent(this@MainActivity, SecondActivity::class.java))
        }
    }

    private fun checkState(playWhenReady: Boolean) {
        if (playWhenReady) {
            play.text = "pause"
        } else {
            play.text = "play"
        }
    }


    override fun onStart() {
        super.onStart()
        App.musicManager.register()
        gettingSong()
        App.musicManager.setListener(this)
    }

    private fun gettingSong() {
        val songHelper = SongHelper()
        songHelper.scanSongs(this, "external")
        songHelper.getSongs().forEach {
            Log.i(TAG, "load media item ${it.toString()}")
            AppConstants.list.add(MediaItem(it.title, it.filePath))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        App.musicManager.unRegister()
    }

}
