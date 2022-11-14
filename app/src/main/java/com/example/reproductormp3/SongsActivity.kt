package com.example.reproductormp3

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SongsActivity : AppCompatActivity() {

    private var db: SQLiteDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songs)

        val search: SearchView = findViewById(R.id.searchView)
        val list: ListView = findViewById(R.id.songList)
        val homeBtn: ImageButton = findViewById(R.id.homeButton)
        val items = mutableListOf<String>()

        db = SQLite(this, "musica", null, 1).writableDatabase
        val cursor = Lector.busca(db)
        var searchCursor = Lector.busca(db)
        val hayCanciones = cursor.moveToFirst()

        val adaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        list.adapter = adaptador

        if (hayCanciones) {
            actualiza(cursor, adaptador)
        }

        search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                try {
                    searchCursor = Lector.busca(db, search.query.toString())
                } catch (sqle: SQLiteException) {
                    Toast.makeText(
                        this@SongsActivity,
                        "Hint: song:Ich Will. &. year:>1990. +. " +
                                "artist:Rammstein. &. album:Mutter. &. genre:Industrial Metal",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (searchCursor.count <= 0) {
                    Toast.makeText(
                        this@SongsActivity,
                        "No hay resultados",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    actualiza(searchCursor, adaptador)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!search.query.contains(":"))
                searchCursor = Lector.busca(db, search.query.toString())
                actualiza(searchCursor, adaptador)
                return true
            }
        })

        search.setOnCloseListener {
            actualiza(cursor, adaptador)
            false
        }

        list.setOnItemClickListener { parent, view, position, id ->
            if (search.query.isNotEmpty()) {
                cursor.moveToFirst()
                val songToPlay = list.getItemAtPosition(position)
                var songAtPos = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                while(!songAtPos.equals(songToPlay)) {
                    cursor.moveToNext()
                    songAtPos = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                }
                finzaliza(cursor.position)
            }
            finzaliza(position)
        }

        homeBtn.setOnClickListener{
            finish()
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

    private fun finzaliza(result: Int) {
        val data = Intent()
        data.putExtra("pos", result)
        setResult(Activity.RESULT_OK, data)
        finish()
    }
}