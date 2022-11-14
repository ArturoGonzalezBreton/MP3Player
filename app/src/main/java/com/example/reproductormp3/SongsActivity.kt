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
    private var items = mutableListOf<String>()
    private lateinit var search: SearchView
    private lateinit var list: ListView
    private lateinit var homeBtn: ImageButton
    private lateinit var adaptador: ArrayAdapter<String>
    private lateinit var cursor: Cursor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songs)

        search = findViewById(R.id.searchView)
        list = findViewById(R.id.songList)
        homeBtn = findViewById(R.id.homeButton)

        db = SQLite(this, "musica", null, 1).writableDatabase
        cursor = Lector.busca(db)
        var searchCursor = Lector.busca(db)

        adaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        list.adapter = adaptador

        actualizaAdaptador(cursor)

        search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                try {
                    searchCursor = Lector.busca(db, search.query.toString())
                } catch (sqle: SQLiteException) {
                    Toast.makeText(
                        this@SongsActivity,
                        "Hint: song:Ich Will; &; year:>1990; +; " +
                                "artist:Rammstein; &; album:Mutter; &; genre:Industrial Metal",
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
                    actualizaAdaptador(searchCursor)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!search.query.contains(":"))
                searchCursor = Lector.busca(db, search.query.toString())
                actualizaAdaptador(searchCursor)
                return true
            }
        })

        search.setOnCloseListener {
            actualizaAdaptador(cursor)
            false
        }

        list.setOnItemClickListener { _, _, position, _ ->
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

    private fun actualizaAdaptador(cursor: Cursor) {
        if (cursor.count > 0) {
            adaptador.clear()
            cursor.moveToFirst()
            do {
                adaptador.add(cursor.getString(cursor.getColumnIndexOrThrow("title")))
            } while (cursor.moveToNext())
            cursor.moveToFirst()
            adaptador.notifyDataSetChanged()
        } else {
            adaptador.clear()
            adaptador.notifyDataSetChanged()
        }
    }

    private fun finzaliza(result: Int) {
        val data = Intent()
        data.putExtra("pos", result)
        setResult(Activity.RESULT_OK, data)
        finish()
    }
}