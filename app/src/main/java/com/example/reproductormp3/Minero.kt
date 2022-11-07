package com.example.reproductormp3

import android.database.sqlite.SQLiteDatabase
import android.media.MediaMetadataRetriever
import java.io.File

/**
 * Clase para buscar canciones en el dispositivo y agregarlas a la
 * base de datos.
 */
class Minero {

    /**
     * Busca canciones en el directorio recibido y sus
     * subdirectorios.
     */
    fun mina(path: String, bdd: SQLiteDatabase?): MutableList<String?> {
        val lector = MediaMetadataRetriever()
        val songs = mutableListOf<String?>()
        File(path).walk().forEach {
            try {
                lector.setDataSource(it.toString())
                var artist = lector.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                var title = lector.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                var album = lector.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                var time = lector.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
                var genre = lector.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                var track =
                    lector.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)

                if (artist == null)
                    artist = "Unknown"
                if (album == null)
                    album = "Unknown"
                if (title == null)
                    title = "Unknown"
                if (time == null)
                    time = "0"
                if (genre == null)
                    genre = "Unknown"
                if (track == null)
                    track = "0"

                artist = artist.replace("\"", "")
                album = album.replace("\"", "")
                title = title.replace("\"", "")
                artist = artist.replace("\'", "")
                album = album.replace("\'", "")
                title = title.replace("\'", "")

                if (!existeArtista(artist, bdd))
                    agregaArtista(artist, bdd)

                if (!existeAlbum(album, bdd))
                    agregaAlbum(album, bdd)

                if (!existePath(it.toString(), bdd))
                    agregaCancion(artist, album, it.toString(), title, track, time, genre, bdd)
            } catch (iae: IllegalArgumentException) {
            } catch (e: RuntimeException) {

            }
        }
        return songs
    }

    /**
     * Determina si existe el artista en la base de datos.
     */
    private fun existeArtista(artista: String?, bdd: SQLiteDatabase?): Boolean {
        if (bdd == null) {
            throw IllegalAccessException("La base de datos es nula")
        }
        val cursor = bdd.rawQuery("SELECT performer_name FROM performers WHERE ?=?",
                                        arrayOf("performer_name", artista))
        if (cursor.count > 0) {
            cursor.close()
            return true
        }
        cursor.close()
        return false
    }

    /**
    * Determina si existe el álbum en la base de datos.
    */
    private fun existeAlbum(album: String?, bdd: SQLiteDatabase?): Boolean {
        if (bdd == null) {
            throw IllegalAccessException("La base de datos es nula")
        }
        val cursor = bdd.rawQuery("SELECT album_name FROM albums WHERE ?=?",
                                        arrayOf("album_name", album))
        if (cursor.count > 0) {
            cursor.close()
            return true
        }
        cursor.close()
        return false
    }

    /**
     * Determina si el archivo ya está en la base de datos.
     */
    private fun existePath(path: String?, bdd: SQLiteDatabase?): Boolean {
        if (bdd == null) {
            throw IllegalAccessException("La base de datos es nula")
        }
        val cursor = bdd.rawQuery("SELECT path FROM rolas WHERE ?=?",
                                        arrayOf("path", path))
        if (cursor.count > 0) {
            cursor.close()
            return true
        }
        cursor.close()
        return false
    }

    /**
     * Agrega un álbum a la base de datos.
     */
    private fun agregaAlbum(album: String?, bdd: SQLiteDatabase?) {
        if (bdd == null) {
            throw IllegalAccessException("La base de datos es nula")
        }
        val albums = bdd.rawQuery("SELECT album_name FROM albums", emptyArray())
        val numAlbums = albums.count
        bdd.execSQL("INSERT INTO albums VALUES($numAlbums, null, 2, '$album')")
        albums.close()
    }

    /**
     * Agrega un artista a la base de datos.
     */
    private fun agregaArtista(artista: String?, bdd: SQLiteDatabase?) {
        if (bdd == null) {
            throw IllegalAccessException("La base de datos es nula")
        }
        val performers = bdd.rawQuery("SELECT performer_name FROM performers", emptyArray())
        val numPerformers = performers.count
        bdd.execSQL("INSERT INTO performers VALUES($numPerformers, 2, '$artista')")
        performers.close()
    }

    /**
     * Agrega una canción a la base de datos.
     */
    private fun agregaCancion(artist: String?,
                              album: String?,
                              path: String?,
                              title: String?,
                              track: String?,
                              time: String?,
                              genre: String?, bdd: SQLiteDatabase?) {
        if (bdd == null) {
            throw IllegalAccessException("La base de datos es nula")
        }
        var cursor = bdd.rawQuery("SELECT id_rola FROM rolas", emptyArray())
        val numSongs = cursor.count

        cursor = bdd.rawQuery("SELECT id_performer FROM performers WHERE ?=?",
                                    arrayOf("performer_name", artist))
        var idPerformer: String?
        idPerformer = "null"
        if (cursor.moveToNext()) {
            idPerformer = cursor.getString(cursor.getColumnIndexOrThrow("id_performer"))
        }

        cursor = bdd.rawQuery("SELECT id_album FROM albums WHERE ?=?",
            arrayOf("album_name", album))
        var idAlbum: String?
        idAlbum = "null"
        if (cursor.moveToNext()) {
            idAlbum = cursor.getString(cursor.getColumnIndexOrThrow("id_album"))
        }
        cursor.close()

        bdd.execSQL("INSERT INTO rolas VALUES($numSongs, " +
                                                "$idPerformer, " +
                                                "$idAlbum, " +
                                                "'$path', " +
                                                "'$title'," +
                                                "$track," +
                                                "$time," +
                                                "'$genre')")
    }
}