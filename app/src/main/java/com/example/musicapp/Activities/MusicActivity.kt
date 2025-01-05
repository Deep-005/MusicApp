package com.example.musicapp.Activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.musicapp.DataClass.*
import com.example.musicapp.R
import com.example.musicapp.Services.MusicService
import com.example.musicapp.databinding.ActivityMusicBinding
import com.example.musicapp.databinding.VolumeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso


class MusicActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object{
        lateinit var musicListMA : ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying:Boolean = false
        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityMusicBinding
        var repeat:Boolean = false
        var min15:Boolean = false
        var min30:Boolean = false
        var min60:Boolean = false
        var nowPlayingId: String = ""
        var isFavourite: Boolean = false
        var fIndex:Int = -1
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // back button
        binding.backToList.setOnClickListener { finish() }

        //getting song
        songReady()

        //play_Pause_Music
        binding.playSong.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }

        // next_Previous_Music
        binding.next.setOnClickListener { prevNextSong(increment = true) }
        binding.previous.setOnClickListener { prevNextSong(increment = false) }

        //shuffle_Songs
        binding.shuffleBlack.setOnClickListener {
            musicListMA = ArrayList()
            musicListMA.addAll(MusicListActivity.MusicListMLA)
            musicListMA.shuffle()
        }

        binding.seekbarMusic.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })

        //repeat button
        binding.repeatBlack.setOnClickListener {
            if (!repeat){
                repeat = true
                binding.repeatBlack.setIconResource(R.drawable.repeat)
                Toast.makeText(baseContext,"Repeat Mode",Toast.LENGTH_SHORT).show()
            }else{
                repeat = false
                binding.repeatBlack.setIconResource(R.drawable.repeat_black)
                Toast.makeText(baseContext,"No Repeat",Toast.LENGTH_SHORT).show()
            }
        }

        // equalizer Button
        binding.equalizerBtn.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent,50)
            }catch (e:java.lang.Exception){
                Toast.makeText(this,"Not Supported for your device", Toast.LENGTH_SHORT).show()
            }
        }

        //Timer Button
        binding.timerBtn.setOnClickListener {
            val timer = min15 || min30 || min60
            if(!timer) showBottomSheetTimer()
            else {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Would you like to stop this timer?")
                    .setPositiveButton("Yes"){_,_ ->
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.timerBtn.setColorFilter(ContextCompat.getColor(this,R.color.black))
                    }
                    .setNegativeButton("No"){dailog, _ ->
                        dailog.dismiss()
                    }
                val customDialog = builder.create()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GREEN)
                customDialog.show()
            }

        }

        //Share Button
        binding.share.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListMA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Sharing this song!"))
        }

        //Favourite Button
        binding.favBtn.setOnClickListener {
            if(isFavourite) {
                isFavourite = false
                binding.favBtn.setImageResource(R.drawable.favorite_empty)
                FavActivity.favSongs.removeAt(fIndex)
            }else{
                isFavourite = true
                binding.favBtn.setImageResource(R.drawable.favorite)
                FavActivity.favSongs.add(musicListMA[songPosition])
            }
        }

        binding.homeBtn.setOnClickListener {
            startActivity(Intent(this@MusicActivity,PlayListActivity::class.java))
            finish()
        }

        binding.volume.setOnClickListener {
            val customDialog = LayoutInflater.from(this).inflate(R.layout.volume, binding.root, false)
            val bindingB = VolumeBinding.bind(customDialog)
            val dialogB = MaterialAlertDialogBuilder(this).setView(customDialog)
                .setOnCancelListener { playMusic() }
//                .setBackground(ColorDrawable(R.drawable.common_bg))
                .create()
            dialogB.show()

            dialogB.window?.apply {
                val params = attributes
                params.gravity = Gravity.BOTTOM or Gravity.END
                params.width = (90 * resources.displayMetrics.density).toInt()
                params.y = 100
                params.x = 100
                attributes = params
            }

            // Initialize AudioManager to control the volume
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

            // Set the max value of the vertical seekbar to the device's max volume
            bindingB.verticalBar.maxValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

            // Set the current progress of the vertical seekbar to the current device volume
            bindingB.verticalBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

            // Listen for changes on the vertical seekbar to adjust the device volume
            bindingB.verticalBar.setOnProgressChangeListener { progress ->
                // Set the device volume to the seekbar's progress
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }

            playMusic()
        }



    }

    private fun songReady(){
        songPosition = intent.getIntExtra("index",0)
        when(intent.getStringExtra("class")){

            "OnlineAdapter"->{
                // for starting service
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                // getting songs
                musicListMA = ArrayList()
                musicListMA.addAll(OnlineActivity.songsList)
                setLayout()
            }

            "PlayListDetailsAdapter"->{
                // for starting service
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                // getting songs
                musicListMA = ArrayList()
                musicListMA.addAll(PlayListActivity.musicPlayList.ref[PlayListDetails.currentPlayListPos].playList)
                setLayout()
            }

            "FavouriteAdapter"->{
                // for starting service
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                // getting songs
                musicListMA = ArrayList()
                musicListMA.addAll(FavActivity.favSongs)
                setLayout()
            }

            "NowPlaying"->{
                setLayout()
                binding.startMusic.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.endMusic.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekbarMusic.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekbarMusic.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying) binding.playSong.setIconResource(R.drawable.pause)
                else binding.playSong.setIconResource(R.drawable.play)
            }

            "MusicAdapterSearch"->{
                // for starting service
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                // getting songs
                musicListMA = ArrayList()
                musicListMA.addAll(MusicListActivity.musicListSearch)
                setLayout()
            }

            "MusicAdapter"->{
                // for starting service
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                // getting songs
                musicListMA = ArrayList()
                musicListMA.addAll(MusicListActivity.MusicListMLA)
                setLayout()
            }
        }
    }

    private fun setLayout(){

        fIndex = favouriteChecker(musicListMA[songPosition].id)

        Picasso.get()
            .load(musicListMA[songPosition].artURI) // Load the image from the URI
            .placeholder(R.drawable.music) // Set a placeholder image
            .centerCrop() // Center crop the image
            .fit() // Fit the image to the ImageView
            .into(binding.disc) // Target ImageView


        binding.mName.text = musicListMA[songPosition].title
        binding.mArtist.text = musicListMA[songPosition].artist

        if (repeat) binding.repeatBlack.setIconResource(R.drawable.repeat)
        if (min15 || min30 || min60) binding.timerBtn.setColorFilter(ContextCompat.getColor(this,R.color.blue))

        if(isFavourite) binding.favBtn.setImageResource(R.drawable.favorite)
        else binding.favBtn.setImageResource(R.drawable.favorite_empty)
    }

    private fun songPlay(){

        try{
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListMA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.playSong.setIconResource(R.drawable.pause)
            musicService!!.showNotification(R.drawable.pause)
            binding.startMusic.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.endMusic.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekbarMusic.progress = 0
            binding.seekbarMusic.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListMA[songPosition].id

        }catch (e:java.lang.Exception){
            return
        }

    }

    private fun playMusic(){
        binding.playSong.setIconResource(R.drawable.pause)
        musicService!!.showNotification(R.drawable.pause)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()

    }

    private fun pauseMusic(){
        binding.playSong.setIconResource(R.drawable.play)
        musicService!!.showNotification(R.drawable.play)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun prevNextSong(increment:Boolean){

        if (increment){

            setSongPosition(increment = true)
            setLayout()
            songPlay()

        }else{

            setSongPosition(increment = false)
            setLayout()
            songPlay()

        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?)  {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        songPlay()
        musicService!!.seekBarsetter()
        musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService!!.audioManager.requestAudioFocus(musicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        songPlay()
        try {
            setLayout()
        }catch (e:java.lang.Exception){
            return
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 50 || resultCode == RESULT_OK){
            return
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showBottomSheetTimer(){

        val dialog = BottomSheetDialog(this@MusicActivity)
        dialog.setContentView(R.layout.timer_sheet)
        dialog.show()

        dialog.findViewById<LinearLayout>(R.id.fisrtTime)?.setOnClickListener {
            Toast.makeText(this,"Music will stop after 15 minutes.",Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this,R.color.blue))
            min15 = true
            Thread{Thread.sleep((15 * 60000).toLong())
              if (min15) AppExit()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.secondTime)?.setOnClickListener {
            Toast.makeText(this,"Music will stop after 30 minutes.",Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this,R.color.blue))
            min15 = true
            Thread{Thread.sleep((30 * 60000).toLong())
                if (min30) AppExit()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.thirdTime)?.setOnClickListener {
            Toast.makeText(this,"Music will stop after 60 minutes.", Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this,R.color.blue))
            min15 = true
            Thread{Thread.sleep((60 * 60000).toLong())
                if (min60) AppExit()
            }.start()
            dialog.dismiss()
        }

    }

}


