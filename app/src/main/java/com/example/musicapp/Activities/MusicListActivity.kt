package com.example.musicapp.Activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.Adapter.MusicAdapter
import com.example.musicapp.DataClass.AppExit
import com.example.musicapp.DataClass.Music
import com.example.musicapp.DataClass.MusicPlayList
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityMusicListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar


class MusicListActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMusicListBinding
    private lateinit var drawer: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter

    companion object{
        lateinit var MusicListMLA: ArrayList<Music>
        lateinit var musicListSearch: ArrayList<Music>
        var search: Boolean = false
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMusicListBinding.inflate(layoutInflater)
        setContentView(binding.drawerLayout)

        // drawerLayout call
        drawer = ActionBarDrawerToggle(this,binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(drawer)
        drawer.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.online.setOnClickListener {
            Toast.makeText(this@MusicListActivity,"Search Online", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,OnlineActivity::class.java))
        }

        binding.liked.setOnClickListener {
            Toast.makeText(this@MusicListActivity,"Favorite Songs", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,FavActivity::class.java))
        }

        binding.playlist.setOnClickListener {
            Toast.makeText(this@MusicListActivity,"Your PlayLists", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,PlayListActivity::class.java))
        }

        // initialize layout when permission granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requestRuntimePermission()) {

                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivityForResult(intent, 2296)
                // Initialize layout
                initialLayout()

                // Retrieve favorite songs
                FavActivity.favSongs = ArrayList()
                val sharedPreferences = getSharedPreferences("FAVORITES", MODE_PRIVATE)
                val jsonString = sharedPreferences.getString("FavSongs", null)
                if (jsonString != null) {
                    val typeToken = object : TypeToken<ArrayList<Music>>() {}.type
                    val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
                    FavActivity.favSongs.addAll(data)
                }

                // Retrieve playlist
                PlayListActivity.musicPlayList = MusicPlayList()
                val sharedPreferencesPlay = getSharedPreferences("MusicPlayList", MODE_PRIVATE)
                val jsonStringPlay = sharedPreferencesPlay.getString("musicPlayList", null)
                Log.d("PlayListLoading", "Loaded Playlist: $jsonStringPlay")

                if (jsonStringPlay != null) {
                    val dataPlayList: MusicPlayList = GsonBuilder().create().fromJson(jsonStringPlay, MusicPlayList::class.java)
                    PlayListActivity.musicPlayList = dataPlayList
                }
            }

            // deleting a song or undoing it, bg color
            val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    // We don't want drag & drop functionality, so return false
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val deletedSong = MusicListMLA[position]

                    // Remove the item from the list
                    MusicListMLA.removeAt(position)
                    musicAdapter.notifyItemRemoved(position)
                    updateTotalSongsCount()

                    // Show an undo Snackbar
                    val snackbar = Snackbar.make(binding.listOfSongs, "${deletedSong.title} deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO") {
                            // Undo: re-add the deleted song to the original position
                            MusicListMLA.add(position, deletedSong)
                            musicAdapter.notifyItemInserted(position)
                            // Scroll to the restored position to ensure visibility
                            binding.listOfSongs.scrollToPosition(position)
                            updateTotalSongsCount()
                        }

                    snackbar.addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            // If the snackbar was dismissed without pressing UNDO (e.g., timeout), delete the file
                            if (event != DISMISS_EVENT_ACTION) {
                                deleteSongFile(deletedSong.path)
                                updateTotalSongsCount()
                            }
                        }
                    })
                    snackbar.show()
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    // If the item is swiped, change the background color
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        val itemView = viewHolder.itemView
                        val background = ColorDrawable(Color.GRAY)

                        if (dX > 0) { // Swiping to the right
                            background.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                        } else { // Swiping to the left
                            background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                        }

                        background.draw(c)
                    }

                    // Call the parent method to perform the swipe
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }

            }

            val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
            itemTouchHelper.attachToRecyclerView(binding.listOfSongs)
        }


        // navigation item clicked events
        binding.drawerNavigation.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.item1 -> startActivity(Intent(this@MusicListActivity,FeedbackActivity::class.java))
                R.id.item2 -> startActivity(Intent(this@MusicListActivity,SettingsActivity::class.java))
                R.id.item3 -> startActivity(Intent(this@MusicListActivity,AboutActivity::class.java))
                R.id.item4 -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit MyMusic")
                        .setMessage("Do you really want to exit the player?")
                        .setPositiveButton("Yes"){_,_ ->
                           AppExit()
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
            true
        }


    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestRuntimePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if READ_MEDIA_AUDIO and WRITE_EXTERNAL_STORAGE permissions are granted
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Request permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    15
                )
                return false
            }
        } else {
            // For Android versions lower than TIRAMISU (API level 33), check WRITE_EXTERNAL_STORAGE permission
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    15
                )
                return false
            }
        }
        return true // Permissions are already granted
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 15) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                initialLayout() // Initialize layout after permissions are granted
            } else {
                Toast.makeText(this, "Permission is required to access and delete files", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawer.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("Recycle", "Range", "SuspiciousIndentation")
    private fun getAllAudio(): ArrayList<Music>{
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.DATA,
             MediaStore.Audio.Media.ALBUM_ID)

        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            null
        )

        if(cursor !=null) {
            if (cursor.moveToFirst())
                do {

                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))

                    val albumIDC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse( "content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri,albumIDC).toString()

                    val music = Music(id = idC, title = titleC, album = albumC, artist = artistC, duration = durationC, path = pathC, artURI = artUriC)
                    val file = File(music.path)
                    if (file.exists())
                        tempList.add(music)

                } while (cursor.moveToNext())
                cursor.close()
        }
        return tempList
    }

    @SuppressLint("SetTextI18n")
    private fun initialLayout(){

        search = false

        //music_songs
        MusicListMLA = getAllAudio()

        // musicAdapter
        binding.listOfSongs.setHasFixedSize(true)
        binding.listOfSongs.setItemViewCacheSize(20)
        binding.listOfSongs.layoutManager = LinearLayoutManager(this@MusicListActivity)
        musicAdapter = MusicAdapter(this@MusicListActivity, MusicListMLA )
        binding.listOfSongs.adapter = musicAdapter
        updateTotalSongsCount()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        if (!MusicActivity.isPlaying && MusicActivity.musicService != null){
          AppExit()
        }
    }

    override fun onResume() {
        super.onResume()

        // Convert ArrayList<Music> to JSON string
        val jsonString = GsonBuilder().create().toJson(FavActivity.favSongs)
        // Store JSON string in SharedPreferences
        val sharedPreferences = getSharedPreferences("FAVORITES", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("FavSongs", jsonString)
            apply()
        }

        // Convert playlist to JSON string
        val jsonStringPlayList = GsonBuilder().create().toJson(PlayListActivity.musicPlayList)
        Log.d("PlayListSaving", "Saving Playlist: $jsonStringPlayList")

        // Store JSON string in SharedPreferences
        val sharedPreferencesPlaylist = getSharedPreferences("MusicPlayList", MODE_PRIVATE)
        with(sharedPreferencesPlaylist.edit()) {
            putString("musicPlayList", jsonStringPlayList)
            apply()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.local_search, menu)
        val searchView = menu?.findItem(R.id.searchLocal)?.actionView as androidx.appcompat.widget.SearchView
        searchView.setOnQueryTextListener(object :androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch = ArrayList()
                if (newText != null){
                    val userInput = newText.lowercase()
                    for(song in MusicListMLA)
                        if (song.title.lowercase().contains(userInput))
                            musicListSearch.add(song)

                    search = true
                    musicAdapter.updateMusicList(searchList = musicListSearch)
                }
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun deleteSongFile(path: String) {
        val file = File(path)
        if (file.exists()) {
            if (file.delete()) {
                Toast.makeText(this, "Song deleted from storage", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to delete song", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTotalSongsCount() {
        val totalSongs = MusicListMLA.size
        binding.totalSongs.text = "Total Songs: $totalSongs"
    }


}