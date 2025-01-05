package com.example.musicapp.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.Activities.MusicActivity
import com.example.musicapp.Activities.PlayListActivity
import com.example.musicapp.Activities.PlayListDetails
import com.example.musicapp.DataClass.AppExit
import com.example.musicapp.DataClass.Music
import com.example.musicapp.DataClass.PlayList
import com.example.musicapp.R
import com.example.musicapp.databinding.FavouriteViewBinding
import com.example.musicapp.databinding.PlaylistViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayListViewAdapter(private val context: Context, private var playListList:ArrayList<PlayList>) : RecyclerView.Adapter<PlayListViewAdapter.MyHolder>(){

    class MyHolder(binding: PlaylistViewBinding): RecyclerView.ViewHolder(binding.root){
        val image = binding.playListImg
        val name = binding.playListName
        val root = binding.root
        val del = binding.playListDel
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun getItemCount(): Int {
        return playListList.size
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        holder.name.text = playListList[position].name
        holder.name.isSelected = true
        holder.del.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(playListList[position].name)
                .setMessage("Do you really want to delete this playList?")
                .setPositiveButton("Yes"){dialog,_ ->
                    PlayListActivity.musicPlayList.ref.removeAt(position)
                    refreshPlayList()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){dailog, _ ->
                    dailog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GREEN)
        }

        holder.root.setOnClickListener {
            val intent = Intent(context,PlayListDetails::class.java)
            intent.putExtra("index",position)
            ContextCompat.startActivity(context,intent,null)
        }

        if (PlayListActivity.musicPlayList.ref[position].playList.size > 0){
            Glide.with(context)
                .load(PlayListActivity.musicPlayList.ref[position].playList[0].artURI)
                .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
                .into(holder.image)
        }

//        holder.root.setOnClickListener {
//            val intent = Intent(context, MusicActivity::class.java)
//            intent.putExtra("index", position)
//            intent.putExtra("class", "FavouriteAdapter")
//            ContextCompat.startActivity(context, intent, null)
//        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlayList(){
        playListList = ArrayList()
        playListList.addAll(PlayListActivity.musicPlayList.ref)
        notifyDataSetChanged()
    }

}