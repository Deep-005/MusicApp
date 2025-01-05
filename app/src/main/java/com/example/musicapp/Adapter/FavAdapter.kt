package com.example.musicapp.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.Activities.MusicActivity
import com.example.musicapp.Activities.MusicListActivity
import com.example.musicapp.DataClass.Music
import com.example.musicapp.DataClass.formatDuration
import com.example.musicapp.R
import com.example.musicapp.databinding.FavouriteViewBinding
import com.example.musicapp.databinding.MusicViewBinding
import com.squareup.picasso.Picasso

class FavAdapter(private val context: Context, private var musicList:ArrayList<Music>) : RecyclerView.Adapter<FavAdapter.MyHolder>(){

    class MyHolder(binding: FavouriteViewBinding): RecyclerView.ViewHolder(binding.root){
        val image = binding.songImgFav
        val name = binding.songNameFav
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(FavouriteViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        holder.name.text = musicList[position].title

        Picasso.get()
            .load(musicList[position].artURI) // Load the image from the URI
            .placeholder(R.drawable.music) // Set a placeholder image
            .centerCrop() // Crop the image to fill the ImageView, maintaining aspect ratio
            .fit() // Resize the image to fit the ImageView
            .into(holder.image) // Target ImageView


        holder.root.setOnClickListener {
            val intent = Intent(context, MusicActivity::class.java)
            intent.putExtra("index", position)
            intent.putExtra("class", "FavouriteAdapter")
            ContextCompat.startActivity(context, intent, null)
        }

    }

}


