package com.example.musicapp.Activities

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import com.example.musicapp.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    lateinit var binding:ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "About Rhythm"

        //about text
        binding.aboutText.text = Html.fromHtml(aboutText1())
    }

    private fun aboutText1():String{

        return "Welcome to <b>Rhythm</b>, your ultimate music companion developed as part" +
                " of an internship project at <b>FebTech Pvt. Ltd.</b> Whether you're relaxing " +
                "at home, commuting, or working out, Rhythm delivers a seamless and " +
                "enjoyable music experience tailored just for you.<br>" +
                "<br><b>What Rhythm is About</b>" +
                "<br>Rhythm is a feature-rich yet user-friendly music player designed to help" +
                " you organize and enjoy your music collection with ease. Whether your tunes" +
                " are stored locally or in the cloud, Rhythm supports a wide range of audio " +
                "formats, ensuring you can listen to your favorite tracks anytime, anywhere.<br>" +
                "<br><b>Purpose</b>" +
                "<br>The purpose of Rhythm is to create a personalized music experience that" +
                " resonates with your lifestyle. At FebTech Pvt. Ltd., we understand" +
                " that music is more than just sound—it's an expression of your emotions and a" +
                " way to enhance every moment of your day. Rhythm is crafted to be fast," +
                " intuitive, and packed with features that cater to your unique musical preferences.<br>"+
                "<br><b>Features You’ll Love</b>"+
                "<br><b>* Custom Playlists:</b> Easily create and manage playlists to suit any mood or occasion."+
                "<br><b>* Advanced Equalizer:</b> Customize your audio experience with our built-in equalizer."+
                "<br><b>* Offline Access:</b> Enjoy your favorite music even when you're offline."+
                "<br><b>* Minimalist Design:</b> Experience a clean, modern interface that's simple and elegant."+
                "<br><b>* Favorite Tracks:</b> Quickly access your favorite songs with just a tap."+
                "<br><b>* Shuffle & Repeat:</b> Take control of your listening experience with shuffle and repeat options.<br>"+
                "<br><b>Our Vision</b>"+
                "<br><b>Rhythm</b> is more than just a music player—it's a project that reflects our dedication " +
                "to delivering quality software during our internship at <b>FebTech Pvt. Ltd.</b> Our vision" +
                " is to make music playback as enjoyable and straightforward as possible, ensuring" +
                " you always have the best music experience at your fingertips.<br><br>"
    }

    override fun onStart() {
        super.onStart()
        binding.aboutAnimation.playAnimation()  // Start the animation when the activity is visible
    }

    override fun onStop() {
        super.onStop()
        binding.aboutAnimation.cancelAnimation()  // Stop the animation when the activity is not visible
    }
}