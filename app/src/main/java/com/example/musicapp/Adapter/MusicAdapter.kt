package com.example.musicapp.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.Activities.*
import com.example.musicapp.DataClass.Music
import com.example.musicapp.DataClass.formatDuration
import com.example.musicapp.R
import com.example.musicapp.databinding.MusicViewBinding

class MusicAdapter(private val context: Context, private var musicList:ArrayList<Music>,
                   private val playListDetails:Boolean = false, private val selectionActivityPlayList: Boolean = false)
    : RecyclerView.Adapter<MusicAdapter.MyHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
       return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)
        Glide.with(context)
            .load(musicList[position].artURI)
            .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
            .into(holder.image)

        when{
            playListDetails->{
                holder.root.setOnClickListener {
                    sendIntent(ref = "PlayListDetailsAdapter", pos = position)
                }
            }

            selectionActivityPlayList->{
                holder.root.setOnClickListener {
                    if (addSong(musicList[position])){
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.selected))
                    }else{
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
                    }
                }
            }

            else->{
                holder.root.setOnClickListener {
                    when{
                        MusicListActivity.search -> {sendIntent(ref = "MusicAdapterSearch", pos = position)}
                        musicList[position].id == MusicActivity.nowPlayingId->{sendIntent(ref = "NowPlaying", pos = MusicActivity.songPosition)}
                        else -> {sendIntent(ref = "MusicAdapter", pos = position)}
                    }
            }
        }
        }
    }

    class MyHolder(binding:MusicViewBinding): RecyclerView.ViewHolder(binding.root){

        val title = binding.songName
        val album = binding.album
        val image = binding.coverInner
        val duration = binding.songDuration
        val root = binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(searchList : ArrayList<Music>){
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref : String, pos : Int){
        val intent = Intent(context, MusicActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

    private fun addSong(song:Music):Boolean{
        PlayListActivity.musicPlayList.ref[PlayListDetails.currentPlayListPos].playList.forEachIndexed { index, music ->
            if (song.id == music.id){
                PlayListActivity.musicPlayList.ref[PlayListDetails.currentPlayListPos].playList.removeAt(index)
                return false
            }
        }
        PlayListActivity.musicPlayList.ref[PlayListDetails.currentPlayListPos].playList.add(song)
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlayList(){
        musicList = ArrayList()
        musicList = PlayListActivity.musicPlayList.ref[PlayListDetails.currentPlayListPos].playList
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList2(newList: ArrayList<Music>) {
        musicList = newList
        notifyDataSetChanged()
    }
}