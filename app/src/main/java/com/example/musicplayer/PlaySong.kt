package com.example.musicplayer

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.musicplayer.databinding.ActivityPlaySongBinding

@Suppress("DEPRECATION")
class PlaySong : AppCompatActivity() {
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
        updateSeek.interrupt()
    }
    private lateinit var binding: ActivityPlaySongBinding
    lateinit var songs: ArrayList<Parcelable>
    lateinit var mediaPlayer: MediaPlayer
    lateinit var textContent: String
    var position = 0
    var currentPosition:Int = 0
    lateinit var updateSeek: Thread
    lateinit var sharedPreferences:SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityPlaySongBinding.inflate(layoutInflater)
        var textView = binding.song
        var play = binding.play
       var previous = binding.previous
        var next = binding.next
        var seekBar = binding.seekBar
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val intent = intent
        val bundle = intent.extras
        songs = bundle!!.getParcelableArrayList("songList")!!
        textContent = intent.getStringExtra("currentSong").toString()
        textView.text = textContent
        textView.isSelected = true
        position = intent.getIntExtra("position", 0)
        val uri = Uri.parse(songs!![position].toString())
        mediaPlayer = MediaPlayer.create(this, uri)
        mediaPlayer?.start()
        seekBar.max = mediaPlayer.duration

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mediaPlayer?.seekTo(seekBar.progress)
            }
        })

        updateSeek = object : Thread() {
            override fun run() {
                currentPosition = 0
                try {
                    while (currentPosition < mediaPlayer.duration) {
                        currentPosition = mediaPlayer.currentPosition
                        seekBar.progress = currentPosition
                        sleep(800)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        (updateSeek).start()

        play.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                play.setImageResource(R.drawable.play)
                mediaPlayer?.pause()
            } else {
                play.setImageResource(R.drawable.pause)
                mediaPlayer?.start()
            }
        }

        previous.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            position = if (position != 0) {
                position - 1
            } else {
                songs.size - 1
            }
            val uri = Uri.parse(songs[position].toString())
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            mediaPlayer?.start()
            play.setImageResource(R.drawable.pause)
            seekBar.max = mediaPlayer.duration
            textContent = songs[position].toString()
            textContent=textContent.substringAfterLast(delimiter = "/", missingDelimiterValue = textContent)
            textContent=textContent.substringBeforeLast(delimiter = ".", missingDelimiterValue = textContent)

            textView.text = textContent
        }

        next.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            position = if (position != songs.size - 1) {
                position + 1
            } else {
                0
            }
            val uri = Uri.parse(songs[position].toString())
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            mediaPlayer?.start()
            play.setImageResource(R.drawable.pause)
            seekBar.max = mediaPlayer.duration
            textContent = songs[position].toString()
            textContent=textContent.substringAfterLast(delimiter = "/", missingDelimiterValue = textContent)
            textContent=textContent.substringBeforeLast(delimiter = ".", missingDelimiterValue = textContent)
            textView.text = textContent
        }
    }

    override fun onPause() {
        sharedPreferences=this.getSharedPreferences("Song", MODE_PRIVATE)
        val editor:SharedPreferences.Editor=sharedPreferences.edit()
        editor.putString("name",textContent)
        editor.putInt("seek",currentPosition)
        editor.apply()
        super.onPause()
    }
}