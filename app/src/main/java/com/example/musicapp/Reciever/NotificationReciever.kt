package com.example.musicapp.Reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.Activities.MusicActivity
import com.example.musicapp.Application.ApplicationClass
import com.example.musicapp.DataClass.AppExit
import com.example.musicapp.DataClass.favouriteChecker
import com.example.musicapp.DataClass.setSongPosition
import com.example.musicapp.Fragment.NowPlaying
import com.example.musicapp.R


class NotificationReciever:BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ApplicationClass.PREVIOUS -> prevNextSong(increment = false, context = context!!)
            ApplicationClass.PLAY -> {
                if(MusicActivity.isPlaying)
                    pauseMusic()
                else
                    playMusic()
            }
            ApplicationClass.NEXT -> prevNextSong(increment = true, context = context!!)
            ApplicationClass.EXIT -> { AppExit() }
        }
    }

    private fun playMusic(){
        MusicActivity.isPlaying = true
        MusicActivity.musicService!!.mediaPlayer!!.start()
        MusicActivity.musicService!!.showNotification(R.drawable.pause)
        MusicActivity.binding.playSong.setIconResource(R.drawable.pause)
        NowPlaying.binding.playPauseBtnFrag.setIconResource(R.drawable.pause)
    }

    private fun pauseMusic(){
        MusicActivity.isPlaying = true
        MusicActivity.musicService!!.mediaPlayer!!.pause()
        MusicActivity.musicService!!.showNotification(R.drawable.play)
        MusicActivity.binding.playSong.setIconResource(R.drawable.play)
        NowPlaying.binding.playPauseBtnFrag.setIconResource(R.drawable.play)
    }

    private fun prevNextSong(increment : Boolean, context : Context){

        setSongPosition(increment = increment)
        MusicActivity.musicService!!.songPlay()
        Glide.with(context)
            .load(MusicActivity.musicListMA[MusicActivity.songPosition].artURI)
            .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
            .into(MusicActivity.binding.disc)

        MusicActivity.binding.mName.text = MusicActivity.musicListMA[MusicActivity.songPosition].title
        MusicActivity.binding.mArtist.text = MusicActivity.musicListMA[MusicActivity.songPosition].artist

        Glide.with(context)
            .load(MusicActivity.musicListMA[MusicActivity.songPosition].artURI)
            .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
            .into(NowPlaying.binding.fragImage)

        NowPlaying.binding.songNameNP.text = MusicActivity.musicListMA[MusicActivity.songPosition].title
        playMusic()

        MusicActivity.fIndex = favouriteChecker(MusicActivity.musicListMA[MusicActivity.songPosition].id)
        if (MusicActivity.isFavourite) MusicActivity.binding.favBtn.setImageResource(R.drawable.favorite)
        else MusicActivity.binding.favBtn.setImageResource(R.drawable.favorite_empty)
    }
}