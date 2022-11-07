package com.example.reproductormp3

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * Clase para leer de una base de datos.
 */
class Lector {
    fun busca(tablas: MutableList<String>,
              columnas: MutableList<String>,
              criterios : Map<String, String>,
              bdd: SQLiteDatabase?): Cursor {
        if (bdd == null) {
            throw IllegalAccessException("La base de datos es nula")
        }
        var sql = "SELECT *"
        sql = sql.substring(0, sql.length - 1)
        columnas.forEach {
            sql += it
            sql += ", "
        }
        sql = sql.substring(0, sql.length - 2)
        sql += " FROM "
        tablas.forEach {
            sql += it
            sql += " JOIN "
        }
        sql = sql.substring(0, sql.length - 5)
        sql += "ON "
        criterios.forEach {
            sql += it.key + "=" + it.value
            sql += " AND "
        }
        sql = sql.substring(0, sql.length - 4)
        //return bdd.rawQuery("SELECT title," +
        //                        " album_name, " +
        //                        " performer_name, " +
        //                        " rolas.year, " +
        //                        " genre, " +
        //                        " rolas.path FROM rolas" +
        //                        " JOIN albums" +
        //                        " JOIN performers ON" +
        //                        " rolas.id_album = albums.id_album AND " +
        //                        " rolas.id_performer = performers.id_performer",
        //                        emptyArray())

        return bdd.rawQuery("SELECT title," +
                " album_name, " +
                " performer_name, " +
                " rolas.year, " +
                " genre, " +
                " rolas.path FROM rolas" +
                " JOIN albums" +
                " JOIN performers",
            emptyArray())
    }
}