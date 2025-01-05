package com.example.musicapp.Activities



import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicapp.Adapter.PlayListViewAdapter
import com.example.musicapp.DataClass.MusicPlayList
import com.example.musicapp.DataClass.PlayList
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityPlaylistBinding
import com.example.musicapp.databinding.AddPlaylistDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PlayListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var adapter: PlayListViewAdapter

    companion object{
        var musicPlayList:MusicPlayList  = MusicPlayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // plaListAdapter connection to playlistRV
        binding.playListRV.setHasFixedSize(true)
        binding.playListRV.setItemViewCacheSize(20)
        binding.playListRV.layoutManager = GridLayoutManager(this@PlayListActivity,2)
        adapter = PlayListViewAdapter(this@PlayListActivity, playListList = musicPlayList.ref )
        binding.playListRV.adapter = adapter

        //empty playlist
        binding.noPlaylist.visibility = View.INVISIBLE
//        if (binding.playListRV.isEmpty()){
//            binding.playListRV.visibility = View.INVISIBLE
//            binding.noPlaylist.visibility = View.VISIBLE
//        }else{
//            binding.playListRV.visibility = View.VISIBLE
//            binding.noPlaylist.visibility = View.INVISIBLE
//        }

        // back btn
        binding.backToList2.setOnClickListener { finish() }

        // add playList btn
        binding.addPlayList.setOnClickListener {
            customAlertDialog()
        }


    }

    private fun customAlertDialog(){
        val customDialog = LayoutInflater.from(this@PlayListActivity).inflate(R.layout.add_playlist_dialog, binding.root,false)
        val binder = AddPlaylistDialogBinding.bind(customDialog)
        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(customDialog)
            .setTitle("Create PlayList")
            .setPositiveButton("Add"){dialog,_ ->
               val playListName =  binder.playListNameText.text
                if (playListName != null)
                    if (playListName.isNotEmpty()){
                        addPlayList(playListName.toString())
                    }
               dialog.dismiss()
            }.show()
    }

    private fun addPlayList(name:String){
        var playListExist = false
        for(i in musicPlayList.ref){
            if (name.equals(i.name)){
                playListExist = true
                break
            }
        }
        if (playListExist) Toast.makeText(this,"PlayList Exists", Toast.LENGTH_SHORT).show()
        else{
            val tempPlayList = PlayList()
            tempPlayList.name = name
            tempPlayList.playList = ArrayList()

            val calender = Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlayList.createdOn = sdf.format(calender)

            musicPlayList.ref.add(tempPlayList)
            adapter.refreshPlayList()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

}