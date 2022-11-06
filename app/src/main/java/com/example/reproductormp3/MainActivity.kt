package com.example.reproductormp3

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.database.sqlite.SQLiteDatabase
import android.graphics.drawable.Icon
import android.hardware.camera2.params.InputConfiguration
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lector = Lector(creaBase())

        val addBtn: ImageButton = findViewById(R.id.addButton)
        addBtn.setOnClickListener {
            creaBase()

        }

        val song: TextView = findViewById(R.id.songName)

        val player = MediaPlayer()
        val lector = MediaMetadataRetriever()
        if (hayPermiso()) {
            player.setDataSource("/storage/self/primary/sampledata/song1.mp3")
            player.prepare()
            lector.setDataSource("/storage/self/primary/sampledata/song1.mp3")
            song.text = lector.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_TITLE)
        } else {
            solicitaPermiso()
        }

        val playPauseBtn: ImageButton = findViewById(R.id.playButton)
        playPauseBtn.setOnClickListener {
            if (!player.isPlaying) {
                player.start()
            } else {            
                player.pause()
            }
        }

    }

    private fun creaBase(): SQLiteDatabase? {
        val sqlite = SQLite(this, "musica", null, 1)
        val db: SQLiteDatabase? = sqlite.writableDatabase
        if (db == null)
            Toast.makeText(this,
                             "Error al crear la base de datos",
                            Toast.LENGTH_LONG).show()
        return db
    }

    private fun hayPermiso(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this@MainActivity,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (result == PackageManager.PERMISSION_GRANTED)
            return true
        return false
    }

    private fun solicitaPermiso() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@MainActivity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(
                this@MainActivity,
                "Permiso requerido para reproducir música",
                Toast.LENGTH_SHORT).show()
        } else ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            111
        )
    }
}