package com.example.musicapp.Activities

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.musicapp.databinding.ActivityFeedbackBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class FeedbackActivity : AppCompatActivity() {

    lateinit var binding: ActivityFeedbackBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Feedback"

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()

        // Send feedback
        binding.sendFb.setOnClickListener {
            val feedbackMsg = binding.feedbackMsg.text.toString()
            val subject = binding.topic.text.toString()
            val email = binding.email.text.toString()

            val cs = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cs.activeNetwork
            val networkCapabilities = cs.getNetworkCapabilities(activeNetwork)
            val isConnected = networkCapabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            if (feedbackMsg.isNotEmpty() && subject.isNotEmpty() && isConnected) {
                val feedback = hashMapOf(
                    "email" to email,
                    "subject" to subject,
                    "message" to feedbackMsg,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                db.collection("feedback")
                    .add(feedback)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Thanks for feedback!!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Something went wrong: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

            } else {
                Toast.makeText(this, "Please fill all fields and ensure you're connected to the internet.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
