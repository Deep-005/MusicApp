package com.example.musicapp.Activities

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.Adapter.OnlineAdapter
import com.example.musicapp.DataClass.MyData
import com.example.musicapp.Interface.ApiInterface
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityOnlineBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.musicapp.DataClass.Music

class OnlineActivity : AppCompatActivity() {

    lateinit var binding: ActivityOnlineBinding
    lateinit var recycle: RecyclerView
    lateinit var adapter: OnlineAdapter
    lateinit var input: String
    lateinit var string:String
    lateinit var editText: EditText

    companion object{
    lateinit var songsList: ArrayList<Music>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backbtn.setOnClickListener { finish() }

        // if internet is there
        if(isInternetAvailable(this)){

            // initialize recyclerView
            recycle = findViewById(R.id.songsRV)

            binding.subhead.visibility = View.VISIBLE
            binding.astronaut.visibility = View.GONE
            binding.offlineText.visibility = View.GONE

            // Initialize adapter with an empty list
            adapter = OnlineAdapter(this@OnlineActivity, emptyList())
            recycle.adapter = adapter
            recycle.layoutManager = LinearLayoutManager(this@OnlineActivity)

            editText = findViewById(R.id.searchOnline)


            // Fetching songs from Deezer
            binding.iconSearch.setOnClickListener{

                hideKeyboard()
                string = editText.text.toString()
                input= string.lowercase(Locale.ROOT)

                val retrofitBuilder = Retrofit.Builder()
                    .baseUrl("https://deezerdevs-deezer.p.rapidapi.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiInterface::class.java)

                val retrofitData = retrofitBuilder.getData(input)
                retrofitData.enqueue(object : Callback<MyData> {
                    override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                        if (response.isSuccessful && response.body() != null) {
                            val dataList = response.body()?.data
                            if (dataList != null) {
                                // Map the Data objects to Music objects
                                songsList = ArrayList(dataList.map { data ->
                                    Music(
                                        id = data.id.toString(),  // Convert id to String
                                        title = data.title,
                                        album = data.album.title,  // Assuming `album` has a `title` property
                                        artist = data.artist.name, // Assuming `artist` has a `name` property
                                        duration = data.duration.toLong(),  // Convert duration to Long
                                        path = data.preview,  // Assuming `preview` is the song URL (or file path)
                                        artURI = "https://e-cdns-images.dzcdn.net/images/cover/${data.md5_image}/500x500.jpg" // Construct image URI
                                    )
                                })

                                // Update the adapter with the Music list
                                adapter.updateData(songsList)
                            } else {
                                Log.d("TAG: onResponse", "Data list is null")
                            }
                        } else {
                            Log.d("TAG: onResponse", "Response not successful or body is null")
                        }
                    }

                    override fun onFailure(call: Call<MyData?>, t: Throwable) {
                        Log.d("TAG: onFailure", "onFailure: ${t.message}")
                    }
                })

            }

        }else{
            binding.subhead.visibility = View.GONE
            binding.astronaut.visibility = View.VISIBLE
            binding.offlineText.visibility = View.VISIBLE
        }
    }

    // check the internet connection
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
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
