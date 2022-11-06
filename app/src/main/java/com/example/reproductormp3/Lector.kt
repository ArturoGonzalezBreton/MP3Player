package com.example.reproductormp3

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

/**
 * Clase para leer de una base de datos.
 */
class Lector(private val bdd: SQLiteDatabase?) {
    fun busca(tablas: MutableList<String>,
              columnas: MutableList<String>,
              criterios : Map<String, String>,
              bdd: SQLiteDatabase): Cursor {
        var sql = "SELECT *"
        sql.dropLast(1)
        columnas.forEach {
            sql += it
            sql += ", "
        }
        sql.dropLast(2)
        sql += " FROM "
        tablas.forEach {
            sql += it
            sql += " JOIN "
        }
        sql.dropLast(5)
        sql += "ON "
        criterios.forEach {
            sql += it.key + "=" + it.value
            sql += " AND "
        }
        sql.dropLast(5)
        sql += ";"
        return bdd.rawQuery(sql, emptyArray())
    }
}