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
            return bdd.rawQuery("SELECT title," +
                        " album_name, " +
                        " performer_name, " +
                        " rolas.year, " +
                        " genre, " +
                        " rolas.path FROM rolas" +
                        " JOIN albums" +
                        " JOIN performers ON" +
                        " rolas.id_album = albums.id_album AND" +
                        " rolas.id_performer = performers.id_performer",
                emptyArray())
        }

        fun busca(bdd: SQLiteDatabase?, busqueda: String): Cursor {
            if (bdd == null)
                throw IllegalArgumentException("la base de datos es nula")
            var sql = "SELECT title," +
                    " album_name, " +
                    " performer_name, " +
                    " rolas.year, " +
                    " genre, " +
                    " rolas.path FROM rolas" +
                    " JOIN albums" +
                    " JOIN performers ON" +
                    " rolas.id_album = albums.id_album AND" +
                    " rolas.id_performer = performers.id_performer WHERE "
            if (!busqueda.contains(":")) {
                sql += " title LIKE '%$busqueda%'"
            } else {
                val pattern = ";".toRegex()
                val criterios = pattern.split(busqueda)
                criterios.forEach {
                    when (it.substringBefore(":").trim()) {
                        "song", "title", "name" -> {
                            sql += " title LIKE '%${it.substringAfter(":").trim()}%'"
                        }
                        "year" -> {
                            if (it.contains(">") ||
                                it.contains("<") ||
                                it.contains("=")) {
                                sql += " rolas.year ${it.substringAfter(":").trim()}"
                            } else {
                                sql += " rolas.year = ${it.substringAfter(":").trim()}"
                            }
                        }
                        "artist", "performer" -> {
                            sql += " performer_name LIKE '%${it.substringAfter(":").trim()}%'"
                        }
                        "album" -> {
                            sql += " album_name LIKE '%${it.substringAfter(":").trim()}%'"
                        }
                        "genre" -> {
                            sql += " genre LIKE '%${it.substringAfter(":").trim()}%'"
                        }
                        "&" -> {
                            sql += " AND "
                        }
                        "|" -> {
                            sql += " OR "
                        }
                    }
                }
            }
            return bdd.rawQuery(sql, null)
        }
    }
}