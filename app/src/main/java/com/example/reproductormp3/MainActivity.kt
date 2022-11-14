package com.example.reproductormp3

import android.app.Activity
import android.content.Intent
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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // Instancia de reproductor.
    private val player = MediaPlayer()
    // Base de datos.
    private var db: SQLiteDatabase? = null
    // Objeto que recorre los resultados de una consulta en la base de datos.
    private lateinit var cursor: Cursor

    private lateinit var song: TextView
    private lateinit var addBtn: ImageButton
    private lateinit var playPauseBtn: ImageButton
    private lateinit var prevBtn: ImageButton
    private lateinit var nextBtn: ImageButton
    private lateinit var artist: TextView
    private lateinit var cover: ImageView
    private lateinit var seek: SeekBar
    private lateinit var songsBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        song = findViewById(R.id.songName)
        addBtn = findViewById(R.id.addButton)
        playPauseBtn = findViewById(R.id.playButton)
        prevBtn = findViewById(R.id.previousButton)
        nextBtn = findViewById(R.id.nextButton)
        artist = findViewById(R.id.artist)
        cover = findViewById(R.id.albumCover)
        seek = findViewById(R.id.seekBar)
        songsBtn = findViewById(R.id.songsButton)

        db = creaBase()
        cursor = Lector.busca(db)
        var hayCanciones = cursor.moveToNext()

        val register = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(result)
        }

        artist.isSelected = true
        song.isSelected = true

        if (hayCanciones) {
            actualizaReproductor()
            playPauseBtn.setImageResource(R.drawable.ic_play_1_1s_40px)
            actualizaPortada()
            actualizaInfo()
        }

        startSeek()

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
                        hayCanciones = cursor.moveToFirst()
                        if (hayCanciones) {
                            actualizaReproductor()
                            playPauseBtn.setImageResource(R.drawable.ic_play_1_1s_40px)
                            actualizaInfo()
                            actualizaPortada()
                        }
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

        songsBtn.setOnClickListener{
            switchActs(register)

        }

        playPauseBtn.setOnClickListener {
            if (hayCanciones) {
                if (!player.isPlaying) {
                    player.start()
                } else {
                    player.pause()
                }
                actualizaPlayBtn()
            }
        }

        prevBtn.setOnClickListener {
            if (player.currentPosition > 4000) {
                    player.seekTo(0)
            } else {
                if (cursor.isFirst)
                    cursor.moveToLast()
                else
                    cursor.moveToPrevious()
                if (hayCanciones) {
                    val start = player.isPlaying
                    if (start)
                        player.stop()
                    player.reset()
                    player.setDataSource(
                        cursor.getString(cursor.getColumnIndexOrThrow("path")))
                    player.prepare()
                    startSeek()
                    if (start)
                        player.start()
                    }
                actualizaPortada()
                actualizaInfo()
            }
        }

        nextBtn.setOnClickListener {
            if (cursor.isLast)
                cursor.moveToFirst()
            else
                cursor.moveToNext()
            if (hayCanciones) {
                val start = player.isPlaying
                if (start)
                    player.stop()
                player.reset()
                player.setDataSource(
                    cursor.getString(cursor.getColumnIndexOrThrow("path")))
                player.prepare()
                startSeek()
                if (start)
                    player.start()
                }
            actualizaPortada()
            actualizaInfo()
        }

        player.setOnCompletionListener {
            if (hayCanciones) {
                if (cursor.isLast)
                    cursor.moveToFirst()
                else
                    cursor.moveToNext()
                player.reset()
                player.setDataSource(
                    cursor.getString(cursor.getColumnIndexOrThrow("path"))
                )
                player.prepare()
                startSeek()
                player.start()
                actualizaPortada()
                actualizaInfo()
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

    /**
     * Método privado para cambiar a la actividad en donde se muestra
     * la lista de canciones.
     */
    private fun switchActs(register: ActivityResultLauncher<Intent>) {
        val switchIntent = Intent(this, SongsActivity::class.java)
        register.launch(switchIntent)
    }

    /**
     * Método privado que maneja el resultado que envíe la actividad
     * de canciones al seleccionar una canción
     */
    private fun onActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                val position: Int
                val res = it.extras?.getInt("pos")
                Log.d("position", res.toString())
                position = res ?: 0
                cursor.moveToPosition(position)
                actualizaReproductor()
                startSeek()
                player.start()
                actualizaPortada()
                actualizaInfo()
                actualizaPlayBtn()
            }
        }
    }

    /**
     * Crea la base de datos. Si ya existe, devuelve la existente.
     */
    private fun creaBase(): SQLiteDatabase? {
        val sqlite = SQLite(this, "musica", null, 1)
        val db: SQLiteDatabase? = sqlite.writableDatabase
        if (db == null)
            Toast.makeText(this,
                "Error al crear la base de datos",
                Toast.LENGTH_LONG).show()
        return db
    }

    /**
     * Inicia la barra que indica el progreso de la reproducción.
     */
    private fun startSeek() {
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

    /**
     * Verifica si la aplicación tiene permiso para acceder al
     * almacenamiento del dispositivo.
     */
    private fun hayPermiso(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this@MainActivity,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (result == PackageManager.PERMISSION_GRANTED)
            return true
        return false
    }

    /**
     * Solicita permiso para acceder al almacenamiento del dispositivo.
     */
    private fun solicitaPermiso() {
        if (hayPermiso())
            return

        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            111
        )
    }

    /**
     * Prepara el reproductor para comenzar a reproducir la canción a la que
     * apunta el cursor.
     */
    private fun actualizaReproductor() {
        if (cursor.count > 0) {
            if (cursor.position < 0)
                cursor.moveToFirst()
            player.reset()
            player.setDataSource(
                cursor.getString(cursor.getColumnIndexOrThrow("path"))
            )
            player.prepare()
        }
    }

    /**
     * Actualiza la información de la canción en la actividad principal.
     */
    private fun actualizaInfo() {
        if (cursor.count > 0) {
            Log.d("count", cursor.count.toString())
            song.text = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val text = cursor.getString(cursor.getColumnIndexOrThrow("album_name")) +
                ", " + cursor.getString(cursor.getColumnIndexOrThrow("performer_name"))
            artist.text = text
        }
    }

    /**
     * Actualiza la la portada del álbum de la canción.
     */
    private fun actualizaPortada() {
        if (cursor.count > 0) {
            val mr = MediaMetadataRetriever()
            mr.setDataSource(cursor.getString(cursor.getColumnIndexOrThrow("path")))
            val byteArray = mr.embeddedPicture
            val bitmap = byteArray?.let { BitmapFactory.decodeByteArray(byteArray, 0, it.size) }
            cover.setImageBitmap(bitmap)
        }
    }

    /**
     * Actualiza la imagen del botón de pausa y play.
     */
    private fun actualizaPlayBtn() {
        if (player.isPlaying) {
            playPauseBtn.setImageResource(R.drawable.ic_pause_1_1s_40px)
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_play_1_1s_40px)
        }
    }
}