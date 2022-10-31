package com.example.reproductormp3

import java.io.File

class Minero {

    fun mina(path: String): MutableList<String> {
        var songs = mutableListOf<String>()
        File(path).walk().forEach {
            songs.add(it.toString())
        }
        return songs
    }
}