package com.example.reproductormp3

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

/**
 * Clase para leer de una base de datos.
 */
class Lector {
    companion object {
        fun busca(bdd: SQLiteDatabase?): Cursor {
            if (bdd == null) {
                throw IllegalArgumentException("la base de datos es nula")
            }
            return bdd.rawQuery(
                "SELECT title," +
                        " album_name, " +
                        " performer_name, " +
                        " rolas.year, " +
                        " genre, " +
                        " rolas.path FROM rolas" +
                        " JOIN albums" +
                        " JOIN performers ON" +
                        " rolas.id_album = albums.id_album AND " +
                        " rolas.id_performer = performers.id_performer",
                emptyArray()
            )
        }
    }
}