package com.example.musicapp.DataClass

import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.musicapp.Activities.FavActivity
import com.example.musicapp.Activities.MusicActivity
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class Music(val id:String, val title:String, val album:String, val artist:String, val duration:Long=0,val path:String, val artURI: String)

class PlayList{
    lateinit var name:String
    lateinit var playList: ArrayList<Music>
    lateinit var createdOn:String
}

class MusicPlayList{
    var ref:ArrayList<PlayList> = ArrayList()
}

fun formatDuration(duration: Long): String {
    val min = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val sec = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            min * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))

    return String.format("%02d:%02d", min, sec)
}


fun getImageArt(path:String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

 fun setSongPosition(increment: Boolean){

    if (!MusicActivity.repeat){

        if (increment){

            if(MusicActivity.musicListMA.size - 1 == MusicActivity.songPosition)
                MusicActivity.songPosition = 0
            else ++MusicActivity.songPosition

        }else{

            if(MusicActivity.songPosition == 0)
                MusicActivity.songPosition = MusicActivity.musicListMA.size - 1
            else --MusicActivity.songPosition

        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun AppExit(){
    if(MusicActivity.musicService != null){
        MusicActivity.musicService!!.audioManager.abandonAudioFocus(MusicActivity.musicService)
        MusicActivity.musicService!!.stopForeground(true)
        MusicActivity.musicService!!.mediaPlayer!!.release()
        MusicActivity.musicService = null
    }
    exitProcess(1)
}

fun favouriteChecker(id:String):Int{
    MusicActivity.isFavourite = false
    FavActivity.favSongs.forEachIndexed { index, music ->
        if (id == music.id){
            MusicActivity.isFavourite = true
            return index
        }
    }
    return -1
}

fun checkPlaylist(playList: ArrayList<Music>): ArrayList<Music>{
    playList.forEachIndexed { index, music ->
        val file = File(music.path)
        if (!file.exists())
            playList.removeAt(index)
    }
    return playList
}