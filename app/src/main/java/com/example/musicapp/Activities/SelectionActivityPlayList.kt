package com.example.musicapp.Activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.Adapter.MusicAdapter
import com.example.musicapp.databinding.ActivitySelectionPlayListBinding

class SelectionActivityPlayList : AppCompatActivity() {

    private lateinit var binding: ActivitySelectionPlayListBinding
    private lateinit var adapter: MusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionPlayListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back button to playlist details
        binding.backToPlayListDetails.setOnClickListener { finish() }

        // Song selection RecyclerView
        binding.addTooPlayListRV.setItemViewCacheSize(10)
        binding.addTooPlayListRV.setHasFixedSize(true)
        binding.addTooPlayListRV.layoutManager = LinearLayoutManager(this)

        adapter = MusicAdapter(this, MusicListActivity.MusicListMLA, selectionActivityPlayList = true)
        binding.addTooPlayListRV.adapter = adapter

        // Set up SearchView
        setupSearchView()

        // Search View Songs To Add
        binding.searchToAdd.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                MusicListActivity.musicListSearch = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for (song in MusicListActivity.MusicListMLA) {
                        if (song.title.lowercase().contains(userInput)) {
                            MusicListActivity.musicListSearch.add(song)
                        }
                    }

                    MusicListActivity.search = true
                    adapter.updateMusicList(searchList = MusicListActivity.musicListSearch)
                }
                return true
            }
        })
    }

    private fun setupSearchView() {
        // Access the SearchView
        val searchView = binding.searchToAdd

        // Access the EditText within the SearchView
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)

        // Set the text color
        searchEditText.setTextColor(Color.BLACK)

        // Set the hint color
        searchEditText.setHintTextColor(Color.GRAY)

        // Set an OnClickListener to handle the keyboard visibility
        searchView.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (currentFocus == searchEditText) {
                // If the SearchView is already focused, hide the keyboard
                hideKeyboard()
            } else {
                // Otherwise, show the keyboard
                searchEditText.requestFocus()
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}

