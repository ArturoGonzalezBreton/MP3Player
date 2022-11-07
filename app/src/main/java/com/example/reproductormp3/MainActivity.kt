package com.example.reproductormp3

import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
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

    val player = MediaPlayer()

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

        val db = creaBase()
        val lector = Lector()
        val columnas = mutableListOf("title", "album_name", "performer_name",
            "rolas.year", "genre", "rolas.path")
        val tablas = mutableListOf("rolas", "albums", "performers")
        val criterios = mapOf("rolas.id_album" to "albums.id_album",
            "rolas.id_performer" to "performers.id_performer")
        var cursor = lector.busca(tablas, columnas, criterios, db)
        var hayCanciones = cursor.moveToNext()
        Log.d("songs", cursor.count.toString())
        if (hayCanciones) {
            player.setDataSource(
                cursor.getString(cursor.getColumnIndexOrThrow("path"))
            )
            player.prepare()
        }

        solicitaPermiso()

        actualiza(cover)
        actualiza(cursor, song, artist)

        startSeek(seek)

        addBtn.setOnClickListener {
            creaBase()
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
            cursor = lector.busca(tablas, columnas, criterios, db)
            hayCanciones = cursor.moveToNext()
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
                    actualiza(cover)
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
                    actualiza(cover)
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
                actualiza(cover)
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
                actualiza(cover)
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

    private fun actualiza(cursor: Cursor, song: TextView, artist: TextView) {
        if (cursor.count > 0) {
            Log.d("count", cursor.count.toString())
            song.text = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val text = cursor.getString(cursor.getColumnIndexOrThrow("album_name")) +
                    ", " + cursor.getString(cursor.getColumnIndexOrThrow("performer_name"))
            artist.text = text
        }
    }

    private fun actualiza(cover: ImageView) {

    }
}