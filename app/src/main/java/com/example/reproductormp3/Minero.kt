package com.example.reproductormp3

import android.database.sqlite.SQLiteDatabase
import android.media.MediaMetadataRetriever
import android.util.Log
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
    fun mina(path: String, bdd: SQLiteDatabase?) {
        val lector = MediaMetadataRetriever()
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
                    agregaAlbum(album, time, bdd)

                if (!existePath(it.toString(), bdd) && !existeCancion(title, bdd))
                    agregaCancion(artist, album, it.toString(), title, track, time, genre, bdd)

            } catch (iae: IllegalArgumentException) {
            } catch (e: RuntimeException) {
            }
        }
    }

    /**
     * Determina si existe el artista en la base de datos.
     */
    private fun existeArtista(artista: String?, bdd: SQLiteDatabase?): Boolean {
        if (bdd == null) {
            throw IllegalAccessException("La base de datos es nula")
        }
        val cursor = bdd.rawQuery("SELECT * FROM performers WHERE performer_name='$artista'",null)
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
        val cursor = bdd.rawQuery("SELECT * FROM albums WHERE album_name='$album'",null)
        if (cursor.count > 0) {
            cursor.close()
            return true
        }
        cursor.close()
        return false
    }

    /**
     * Determina si existe la canción en la base de datos.
     */
    private fun existeCancion(song: String?, bdd: SQLiteDatabase?): Boolean {
        if (bdd == null) {
            throw IllegalAccessException("La base de datos es nula")
        }
        val cursor = bdd.rawQuery("SELECT * FROM rolas WHERE title='$song'",null)
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
        val cursor = bdd.rawQuery("SELECT * FROM rolas WHERE path='$path'", null)
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
    private fun agregaAlbum(album: String?, time: String?, bdd: SQLiteDatabase?) {
        if (bdd == null) {
            throw IllegalAccessException("La base de datos es nula")
        }
        bdd.execSQL("INSERT INTO albums (path, album_name, year) " +
                        "VALUES(null, '$album', $time)")
        if (album != null) {
            Log.d("album_insertions", album)
        }
    }

    /**
     * Agrega un artista a la base de datos.
     */
    private fun agregaArtista(artista: String?, bdd: SQLiteDatabase?) {
        if (bdd == null) {
            throw IllegalAccessException("La base de datos es nula")
        }
        bdd.execSQL("INSERT INTO performers (id_type, performer_name) " +
                        "VALUES(2, '$artista')")
        if (artista != null) {
            Log.d("art_insertions", artista)
        }
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

        var cursor = bdd.rawQuery("SELECT id_performer FROM performers WHERE " +
                                        "performer_name='$artist'", null)
        var idPerformer: String?
        idPerformer = "null"
        //Log.d("ids_album", cursor.getString(cursor.getColumnIndexOrThrow("id_performer")))
        if (cursor.moveToNext()) {
            idPerformer = cursor.getString(cursor.getColumnIndexOrThrow("id_performer"))
        }

        cursor = bdd.rawQuery("SELECT id_album FROM albums WHERE " +
                                    "album_name='$album'", null)
        var idAlbum: String?
        idAlbum = "null"
        //Log.d("ids_performer", cursor.getString(cursor.getColumnIndexOrThrow("id_album")))
        if (cursor.moveToNext()) {
            idAlbum = cursor.getString(cursor.getColumnIndexOrThrow("id_album"))
        }
        cursor.close()

        bdd.execSQL("INSERT INTO rolas (id_performer, " +
                                            "id_album," +
                                            "path, " +
                                            "title, " +
                                            "track, " +
                                            "year, " +
                                            "genre)" +
                                                "VALUES($idPerformer, " +
                                                "$idAlbum, " +
                                                "'$path', " +
                                                "'$title'," +
                                                "$track," +
                                                "$time," +
                                                "'$genre')")
        if (path != null) {
            Log.d("song_insertions", path+" "+title)
        }
    }
}