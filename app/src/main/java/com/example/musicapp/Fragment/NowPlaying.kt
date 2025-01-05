package com.example.musicapp.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.Activities.MusicActivity
import com.example.musicapp.DataClass.setSongPosition
import com.example.musicapp.R
import com.example.musicapp.databinding.FragmentNowPlayingBinding

class NowPlaying : Fragment() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : FragmentNowPlayingBinding
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE

        // Bottom Play_Pause Button
        binding.playPauseBtnFrag.setOnClickListener {
            if (MusicActivity.isPlaying == true) pauseMusic()
            else playMusic()
        }

        // Bottom_next_Btn
        binding.nextBtnFrag.setOnClickListener {
            setSongPosition(increment = true)
            MusicActivity.musicService!!.songPlay()

            Glide.with(this)
                .load(MusicActivity.musicListMA[MusicActivity.songPosition].artURI)
                .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
                .into(binding.fragImage)

            binding.songNameNP.text = MusicActivity.musicListMA[MusicActivity.songPosition].title
            MusicActivity.musicService!!.showNotification(R.drawable.pause)

            playMusic()
        }

        // Go_to_Music_Activity
        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), MusicActivity::class.java)
            intent.putExtra("index", MusicActivity.songPosition)
            intent.putExtra("class", "NowPlaying")
            ContextCompat.startActivity(requireContext(), intent, null)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if (MusicActivity.musicService != null){
            binding.root.visibility = View.VISIBLE
            binding.songNameNP.isSelected = true

            Glide.with(this)
                .load(MusicActivity.musicListMA[MusicActivity.songPosition].artURI)
                .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
                .into(binding.fragImage)

            binding.songNameNP.text = MusicActivity.musicListMA[MusicActivity.songPosition].title

            if (MusicActivity.isPlaying) binding.playPauseBtnFrag.setIconResource(R.drawable.pause)
            else binding.playPauseBtnFrag.setIconResource(R.drawable.play)
        }
    }

    private fun playMusic(){
        MusicActivity.musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnFrag.setIconResource(R.drawable.pause)
        MusicActivity.musicService!!.showNotification(R.drawable.pause)
        MusicActivity.binding.playSong.setIconResource(R.drawable.pause)
        MusicActivity.isPlaying = true
    }

    private fun pauseMusic(){
        MusicActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnFrag.setIconResource(R.drawable.play)
        MusicActivity.musicService!!.showNotification(R.drawable.play)
        MusicActivity.binding.playSong.setIconResource(R.drawable.play)
        MusicActivity.isPlaying = false
    }

}