package com.example.musicapp.Adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.Activities.MusicActivity
import com.example.musicapp.DataClass.Data
import com.example.musicapp.DataClass.Music
import com.example.musicapp.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class OnlineAdapter(val context: Activity, private var dataList: List<Music>)
    : RecyclerView.Adapter<OnlineAdapter.MyViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingPosition: Int = -1
    private var lastViewHolder: MyViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.category, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val currentData = dataList[position]
        Log.d("TAG: onBindViewHolder", "Binding data for position: $position with title: ${currentData.title}")

        holder.mName.text = currentData.title
        holder.album.text = currentData.album
        holder.duration.text = formatDurationFromSeconds(currentData.duration.toInt())

        Glide.with(context)
            .load(currentData.artURI)
            .error(R.drawable.music)
            .into(holder.mImage)

        holder.play.setOnClickListener {
            if (currentPlayingPosition == position) {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
//                    holder.play.text = "Play"
                    holder.play.setImageResource(R.drawable.play)
                } else {
                    mediaPlayer?.start()
                    holder.play.setImageResource(R.drawable.pause)
//                    holder.play.text = "Pause"
                }
            } else {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(context, currentData.path.toUri())
                mediaPlayer?.start()

//                lastViewHolder?.play?.text = "Play"
//                holder.play.text = "Pause"

                currentPlayingPosition = position
                lastViewHolder = holder
            }
        }

        holder.root.setOnClickListener {
            val intent = Intent(context, MusicActivity::class.java)
            intent.putExtra("index", position)
            intent.putExtra("class", "OnlineAdapter")
            ContextCompat.startActivity(context, intent, null)
        }

    }

    override fun onViewRecycled(holder: MyViewHolder) {
        super.onViewRecycled(holder)
        if (currentPlayingPosition == holder.adapterPosition) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            currentPlayingPosition = -1
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newDataList: List<Music>) {
        dataList = newDataList
        notifyDataSetChanged()  // Refresh the RecyclerView to show updated data
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mImage: ImageView = itemView.findViewById(R.id.coverImg)
        val mName: TextView = itemView.findViewById(R.id.nameC)
        val root: CardView = itemView.findViewById(R.id.categoryRoot)
        val duration:TextView = itemView.findViewById(R.id.duration)
        val album:TextView = itemView.findViewById(R.id.album)
        val play: ImageButton = itemView.findViewById(R.id.playFake)
    }

    fun formatDurationFromSeconds(seconds: Int): String {
        val min = seconds / 60
        val sec = seconds % 60

        return String.format("%02d:%02d", min, sec)
    }

}

