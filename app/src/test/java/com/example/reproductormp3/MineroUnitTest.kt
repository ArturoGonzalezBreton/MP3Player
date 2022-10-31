package com.example.reproductormp3

import org.junit.Test
import org.junit.Assert.*

/**
 * Pruebas unitarias para el minero de canciones.
 */
class MineroUnitTest {
    private val minero = Minero()
    @Test
    fun testMina() {
        var songs = mutableListOf<String>()
        songs.add("./sampledata/song1.mp3")
        songs.add("./sampledata/song2.mp3")
        songs.add("./sampledata/canciones/song3.mp3")
        songs.add("./sampledata/canciones/song4.mp3")
        songs.add("./sampledata/canciones/musica/song5.mp3")
        songs.add("./sampledata/canciones/musica/song6.mp3")
        songs.add("./sampledata/canciones/musica/song7.mp3")
        var paths = minero.mina("./sampledata")
        assertTrue(songs.containsAll(paths))
        assertTrue(paths.containsAll(songs))
    }
}