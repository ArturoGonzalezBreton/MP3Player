package com.example.reproductormp3

import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val player = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val search: SearchView = findViewById(R.id.searchView)
        val song: TextView = findViewById(R.id.songName)
        val addBtn: ImageButton = findViewById(R.id.addButton)
        val playPauseBtn: ImageButton = findViewById(R.id.playButton)
        val prevBtn: ImageButton = findViewById(R.id.previousButton)
        val nextBtn: ImageButton = findViewById(R.id.nextButton)
        val artist: TextView = findViewById(R.id.artist)
        val cover: ImageView = findViewById(R.id.albumCover)
        val seek: SeekBar = findViewById(R.id.seekBar)
        val canciones: Button = findViewById(R.id.songsButton)

        artist.isSelected = true
        song.isSelected = true

        val db = creaBase()

        var cursor = Lector.busca(db)

        actualiza(cursor, player)
        actualiza(cursor, cover)
        actualiza(cursor, song, artist)

        startSeek(seek)

        var hayCanciones = cursor.moveToNext()

        solicitaPermiso()

        addBtn.setOnClickListener {
            val minero = Minero()
            val editText = EditText(this)
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setTitle("Path")
                setMessage("Escriba la ruta del directorio")
                setView(editText)
                setPositiveButton("Agrega"
                ) { _, _ ->
                    if (editText.text.isNotEmpty()) {
                        minero.mina(editText.text.toString(), db)
                        cursor = Lector.busca(db)
                        hayCanciones = cursor.moveToNext()
                        actualiza(cursor, player)
                    } else {
                        Toast.makeText(this@MainActivity,
                            "Falta el directorio",
                            Toast.LENGTH_LONG).show()
                    }
                }
                setNegativeButton("Cancelar"
                ) { _, _ ->
                }
            }.create().show()
        }

        playPauseBtn.setOnClickListener {
            if (hayCanciones) {
                if (!player.isPlaying) {
                    player.start()
                } else {
                    player.pause()
                }
            }
        }

        prevBtn.setOnClickListener {
            when {
                player.currentPosition > 4000 -> {
                    player.seekTo(0)
                }
                cursor.isFirst -> {
                    cursor.moveToLast()
                    if (hayCanciones) {
                        if (player.isPlaying)
                            player.stop()
                        player.reset()
                        player.setDataSource(
                            cursor.getString(cursor.getColumnIndexOrThrow("path"))
                        )
                        player.prepare()
                        startSeek(seek)
                        player.start()
                    }
                    actualiza(cursor, cover)
                    actualiza(cursor, song, artist)
                }
                else -> {
                    cursor.moveToPrevious()
                    if (hayCanciones) {
                        if (player.isPlaying)
                            player.stop()
                        player.reset()
                        player.setDataSource(
                            cursor.getString(cursor.getColumnIndexOrThrow("path"))
                        )
                        player.prepare()
                        startSeek(seek)
                        player.start()
                    }
                    actualiza(cursor, cover)
                    actualiza(cursor, song, artist)
                }
            }
        }

        nextBtn.setOnClickListener {
            if (cursor.isLast) {
                cursor.moveToFirst()
                if (hayCanciones) {
                    if (player.isPlaying)
                        player.stop()
                    player.reset()
                    player.setDataSource(
                        cursor.getString(cursor.getColumnIndexOrThrow("path"))
                    )
                    player.prepare()
                    startSeek(seek)
                    player.start()
                }
                actualiza(cursor, cover)
                actualiza(cursor, song, artist)
            } else {
                cursor.moveToNext()
                if (hayCanciones) {
                    if (player.isPlaying)
                        player.stop()
                    player.reset()
                    player.setDataSource(
                        cursor.getString(cursor.getColumnIndexOrThrow("path"))
                    )
                    player.prepare()
                    startSeek(seek)
                    player.start()
                }
                actualiza(cursor, cover)
                actualiza(cursor, song, artist)
            }
        }

        seek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) player.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }

    private fun startSeek(seek: SeekBar) {
        seek.max = player.duration
        val handler = Handler()
        handler.postDelayed(object: Runnable {
            override fun run() {
                try {
                    seek.progress = player.currentPosition
                    handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    seek.progress = 0
                }
            }
        }, 0)
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
        if (hayPermiso())
            return

        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            111
        )
    }

    private fun actualiza(cursor: Cursor, player: MediaPlayer) {
        if (cursor.count > 0) {
            cursor.moveToFirst()
            player.reset()
            player.setDataSource(
                cursor.getString(cursor.getColumnIndexOrThrow("path"))
            )
            player.prepare()
        }
    }

    private fun actualiza(cursor: Cursor, song: TextView, artist: TextView) {
        if (cursor.count > 0) {
            Log.d("count", cursor.count.toString())
            song.text = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val text = cursor.getString(cursor.getColumnIndexOrThrow("album_name")) +
                    ", " + cursor.getString(cursor.getColumnIndexOrThrow("performer_name"))
            artist.text = text
        }
    }

    private fun actualiza(cursor: Cursor, cover: ImageView) {
        val mr = MediaMetadataRetriever()
        mr.setDataSource(cursor.getString(cursor.getColumnIndexOrThrow("path")))
        val byteArray = mr.embeddedPicture
        val bitmap = byteArray?.let { BitmapFactory.decodeByteArray(byteArray, 0, it.size) }
        cover.setImageBitmap(bitmap)

    }
}