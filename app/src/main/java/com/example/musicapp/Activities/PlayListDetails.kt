package com.example.musicapp.Activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.Adapter.MusicAdapter
import com.example.musicapp.DataClass.AppExit
import com.example.musicapp.DataClass.MusicPlayList
import com.example.musicapp.DataClass.checkPlaylist
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityPlayListDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

class PlayListDetails : AppCompatActivity() {

    lateinit var binding:ActivityPlayListDetailsBinding
    lateinit var adapter:MusicAdapter

    companion object{
        var currentPlayListPos: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayListDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //playList click event
        currentPlayListPos = intent.extras?.get("index") as Int
        PlayListActivity.musicPlayList.ref[currentPlayListPos].playList = checkPlaylist(playList =  PlayListActivity.musicPlayList.ref[currentPlayListPos].playList)

        //back btn
        binding.backToList2.setOnClickListener { finish() }

        //playList details RV
        binding.playListDetailsRV.setItemViewCacheSize(10)
        binding.playListDetailsRV.setHasFixedSize(true)
        binding.playListDetailsRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this,PlayListActivity.musicPlayList.ref[currentPlayListPos].playList, playListDetails = true)
        binding.playListDetailsRV.adapter = adapter

        //Add button
        binding.addSong.setOnClickListener {
            startActivity(Intent(this@PlayListDetails,SelectionActivityPlayList::class.java))
        }

        //remove all button
        binding.removeAll.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Remove")
                .setMessage("Would you like to remove all songs from this list?")
                .setPositiveButton("Yes"){dialog,_ ->
                    PlayListActivity.musicPlayList.ref[currentPlayListPos].playList.clear()
                    adapter.refreshPlayList()
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
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        binding.playListFolderName.text = PlayListActivity.musicPlayList.ref[currentPlayListPos].name
        binding.folderName.text = PlayListActivity.musicPlayList.ref[currentPlayListPos].name
        binding.totalSongs.text = "Total ${adapter.itemCount} songs."
        binding.created.text = "Created On:\n\t${PlayListActivity.musicPlayList.ref[currentPlayListPos].createdOn}"

        if (adapter.itemCount > 0){
            Glide.with(this)
                .load(PlayListActivity.musicPlayList.ref[currentPlayListPos].playList[0].artURI)
                .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
                .into(binding.imgDetails)
        }
        adapter.notifyDataSetChanged()
    }
}