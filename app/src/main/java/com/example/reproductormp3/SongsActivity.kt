package com.example.reproductormp3

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog

class SongsActivity : AppCompatActivity() {

    private val player = Reproductor
    private var db: SQLiteDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songs)

        val list: ListView = findViewById(R.id.songList)
        val homeBtn: ImageButton = findViewById(R.id.homeButton)
        val addBtn: ImageButton = findViewById(R.id.addButton)
        val items = mutableListOf<String>()

        db = SQLite(this, "musica", null, 1).writableDatabase
        var cursor = Lector.busca(db)

        var hayCanciones = cursor.moveToFirst()

        var adaptador = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items)
        list.adapter = adaptador

        if (hayCanciones) {
            actualiza(cursor, adaptador)
        }

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
                        actualiza(cursor, player)
                        actualiza(cursor, adaptador)
                    } else {
                        Toast.makeText(this@SongsActivity,
                            "Falta el directorio",
                            Toast.LENGTH_LONG).show()
                    }
                }
                setNegativeButton("Cancelar"
                ) { _, _ ->
                }
            }.create().show()
        }

        list.setOnItemClickListener { parent, view, position, id ->
            cursor.moveToPosition(position)
            actualiza(cursor, player)
            player.start()
        }

        homeBtn.setOnClickListener{
            finish()
        }
    }

    private fun actualiza(cursor: Cursor, player: MediaPlayer) {
        if (cursor.count > 0) {
            player.reset()
            player.setDataSource(
                cursor.getString(cursor.getColumnIndexOrThrow("path"))
            )
            player.prepare()
        }
    }

    private fun actualiza(cursor: Cursor, adapter: ArrayAdapter<String>) {
        if (cursor.count > 0) {
            adapter.clear()
            do {
                adapter.add(cursor.getString(cursor.getColumnIndexOrThrow("title")))
            } while (cursor.moveToNext())
            cursor.moveToFirst()
            adapter.notifyDataSetChanged()
        }
    }
}