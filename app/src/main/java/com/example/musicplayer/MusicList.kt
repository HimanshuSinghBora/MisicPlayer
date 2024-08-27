package com.example.musicplayer

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.musicplayer.databinding.ActivityMusicListBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File

class MusicList : AppCompatActivity() {
    lateinit var binding: ActivityMusicListBinding
    var currentSong:String = ""
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityMusicListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var listView = binding.listView
        Dexter.withContext(this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse?) {
                    Toast.makeText(
                        this@MusicList,
                        "Runtime permission given",
                        Toast.LENGTH_SHORT
                    ).show()
                    val mySongs = fetchSongs(Environment.getExternalStorageDirectory())
                    val items = arrayOfNulls<String>(mySongs.size)
                    for (i in mySongs.indices) {
                        items[i] = mySongs[i]!!.name.replace(".mp3", "")
                    }
                    var adapter = ArrayAdapter<String>(
                        this@MusicList,
                        android.R.layout.simple_list_item_1,
                        items
                    )
                    listView.setAdapter(adapter)
                    listView.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
                        val intent: Intent = Intent(
                            this@MusicList,
                            PlaySong::class.java
                        )
                        currentSong = listView.getItemAtPosition(position).toString()
                        intent.putExtra("songList", mySongs)
                        intent.putExtra("currentSong", currentSong)
                        intent.putExtra("position", position)
                        startActivity(intent)
                    })
                }

                override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse?) {}
                override fun onPermissionRationaleShouldBeShown(
                    permissionRequest: PermissionRequest?,
                    permissionToken: PermissionToken
                ) {
                    permissionToken.continuePermissionRequest()
                }
            })
            .check()

    }

    override fun onResume() {
        super.onResume()
        sharedPreferences=this.getSharedPreferences("Song", MODE_PRIVATE)
        val current =sharedPreferences.getString("name","No Current Song")
        var songText= binding.textView2
        songText.text = current
    }

    fun fetchSongs(file: File): ArrayList<File?> {
        val arrayList: ArrayList<File?> = ArrayList<File?>()
        val songs = file.listFiles()
        if (songs != null) {
            for (myFile in songs) {
                if (!myFile.isHidden && myFile.isDirectory) {
                    arrayList.addAll(fetchSongs(myFile))
                }else {
                    if (myFile.name.endsWith(".mp3") && !myFile.name.startsWith(".")) {
                        arrayList.add(myFile)
                    }
                }
                }
            }
        return arrayList
    }

}