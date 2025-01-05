package com.example.musicapp.Services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.example.musicapp.Activities.MusicActivity
import com.example.musicapp.Activities.MusicListActivity
import com.example.musicapp.Application.ApplicationClass
import com.example.musicapp.DataClass.formatDuration
import com.example.musicapp.DataClass.getImageArt
import com.example.musicapp.Fragment.NowPlaying
import com.example.musicapp.R
import com.example.musicapp.Reciever.NotificationReciever

class MusicService: Service(), AudioManager.OnAudioFocusChangeListener {

    private val myBinder = MyBinder()
    var mediaPlayer:MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable : Runnable
    lateinit var audioManager: AudioManager

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext,"My Music")
        return myBinder
    }
    inner class MyBinder: Binder(){
        fun currentService(): MusicService{
            return this@MusicService
        }
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification(playPauseBtn: Int) {

        val intent = Intent(baseContext, MusicListActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val previousIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.PREVIOUS)
        val previousPendingIntent = PendingIntent.getBroadcast(baseContext, 1, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val playIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 3, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val exitIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 4, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val imageArt = getImageArt(MusicActivity.musicListMA[MusicActivity.songPosition].path)
        val image = if (imageArt != null) {
            BitmapFactory.decodeByteArray(imageArt, 0, imageArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.music)
        }

        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentTitle(MusicActivity.musicListMA[MusicActivity.songPosition].title)
            .setContentText(MusicActivity.musicListMA[MusicActivity.songPosition].artist)
            .setContentIntent(contentIntent)
            .setSmallIcon(R.drawable.music_notes)
            .setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.rewind_btn, "Previous", previousPendingIntent)
            .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.forward_btn, "Next", nextPendingIntent)
            .addAction(R.drawable.cross, "Exit", exitPendingIntent)
            .build()

        startForeground(10, notification)
    }


    fun songPlay(){

        try{
            if (MusicActivity.musicService!!.mediaPlayer == null) MusicActivity.musicService!!.mediaPlayer = MediaPlayer()
            MusicActivity.musicService!!.mediaPlayer!!.reset()
            MusicActivity.musicService!!.mediaPlayer!!.setDataSource(MusicActivity.musicListMA[MusicActivity.songPosition].path)
            MusicActivity.musicService!!.mediaPlayer!!.prepare()
            MusicActivity.binding.playSong.setIconResource(R.drawable.pause)
            MusicActivity.musicService!!.showNotification(R.drawable.pause)

            MusicActivity.binding.startMusic.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            MusicActivity.binding.endMusic.text = formatDuration(mediaPlayer!!.duration.toLong())
            MusicActivity.binding.seekbarMusic.progress = 0
            MusicActivity.binding.seekbarMusic.max = mediaPlayer!!.duration
            MusicActivity.nowPlayingId = MusicActivity.musicListMA[MusicActivity.songPosition].id

        }catch (e:java.lang.Exception){
            return
        }

    }

    fun seekBarsetter(){
        runnable = Runnable {
            MusicActivity.binding.startMusic.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            MusicActivity.binding.seekbarMusic.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0){
            // pause music
            MusicActivity.binding.playSong.setIconResource(R.drawable.play)
            NowPlaying.binding.playPauseBtnFrag.setIconResource(R.drawable.play)
            showNotification(R.drawable.play)
            MusicActivity.isPlaying = false
            MusicActivity.musicService!!.mediaPlayer!!.pause()
        }else{
            // play music
            MusicActivity.binding.playSong.setIconResource(R.drawable.pause)
            NowPlaying.binding.playPauseBtnFrag.setIconResource(R.drawable.pause)
            showNotification(R.drawable.pause)
            MusicActivity.isPlaying = true
            MusicActivity.musicService!!.mediaPlayer!!.start()
        }
    }
}