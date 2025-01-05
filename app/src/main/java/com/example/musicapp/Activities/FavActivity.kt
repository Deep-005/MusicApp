package com.example.musicapp.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicapp.Adapter.FavAdapter
import com.example.musicapp.DataClass.Music
import com.example.musicapp.DataClass.checkPlaylist
import com.example.musicapp.databinding.ActivityFavBinding

class FavActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavBinding
    private lateinit var adapter: FavAdapter

    companion object{
        var favSongs:ArrayList<Music> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //reloading list when open app for data redundancy
        favSongs = checkPlaylist(favSongs)

        binding.backToList.setOnClickListener{ finish() }

        // fav RecyclerView songs
        binding.favouriteRV.setHasFixedSize(true)
        binding.favouriteRV.setItemViewCacheSize(20)
        binding.favouriteRV.layoutManager = GridLayoutManager(this@FavActivity,4)
        adapter = FavAdapter(this@FavActivity, favSongs )
        binding.favouriteRV.adapter = adapter

        // no fav songs
        binding.noFavSong.visibility = View.INVISIBLE
        if (favSongs.isEmpty()){
            binding.favouriteRV.visibility = View.INVISIBLE
            binding.noFavSong.visibility = View.VISIBLE
        }else{
            binding.favouriteRV.visibility = View.VISIBLE
            binding.noFavSong.visibility = View.INVISIBLE
        }

    }
}