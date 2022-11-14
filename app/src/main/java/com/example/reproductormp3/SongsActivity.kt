package com.example.reproductormp3

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
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

        val search: SearchView = findViewById(R.id.searchView)
        val list: ListView = findViewById(R.id.songList)
        val homeBtn: ImageButton = findViewById(R.id.homeButton)
        val addBtn: ImageButton = findViewById(R.id.addButton)
        val items = mutableListOf<String>()

        db = SQLite(this, "musica", null, 1).writableDatabase
        var cursor = Lector.busca(db)
        var hayCanciones = cursor.moveToFirst()

        val adaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        list.adapter = adaptador

        if (hayCanciones) {
            actualiza(cursor, adaptador)
        }

        search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                try {
                    cursor = Lector.busca(db, search.query.toString())
                } catch (sqle: SQLiteException) {
                    Toast.makeText(
                        this@SongsActivity,
                        "Hint: song:Ich Will. &. year:>1990. +. " +
                                "artist:Rammstein. &. album:Mutter. &. genre:Industrial Metal",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (cursor.count <= 0) {
                    Toast.makeText(
                        this@SongsActivity,
                        "No hay resultados",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    actualiza(cursor, adaptador)
                    Toast.makeText(
                        this@SongsActivity,
                        "Se hallaron ${cursor.count} canciones",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!search.query.contains(":"))
                cursor = Lector.busca(db, search.query.toString())
                actualiza(cursor, adaptador)
                return true
            }
        })

        search.setOnCloseListener {
            cursor = Lector.busca(db)
            actualiza(cursor, adaptador)
            false
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
            cursor.moveToFirst()
            do {
                adapter.add(cursor.getString(cursor.getColumnIndexOrThrow("title")))
            } while (cursor.moveToNext())
            cursor.moveToFirst()
            adapter.notifyDataSetChanged()
        } else {
            adapter.clear()
            adapter.notifyDataSetChanged()
        }
    }
}