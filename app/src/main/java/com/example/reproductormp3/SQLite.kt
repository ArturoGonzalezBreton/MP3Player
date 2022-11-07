package com.example.reproductormp3
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLite(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db: SQLiteDatabase?) {
        val types = "CREATE TABLE types (" +
                "   id_type               INTEGER PRIMARY KEY," +
                "   description           TEXT" +
                " );"
        val insertPerson = "INSERT INTO types VALUES(0, 'Person');"
        val insertGroup = "INSERT INTO types VALUES(1, 'Group');"
        val insertUnknown = "INSERT INTO types VALUES(2, 'Unknown');"
        val performers = "CREATE TABLE performers (" +
                "   id_performer          INTEGER PRIMARY KEY," +
                "   id_type               INTEGER," +
                "   performer_name                  TEXT," +
                "   FOREIGN KEY (id_type) REFERENCES types(id_type)" +
                ");"
        val persons = "CREATE TABLE persons (" +
                "   id_person             INTEGER PRIMARY KEY," +
                "   stage_name            TEXT," +
                "   real_name             TEXT," +
                "   birth_date            TEXT," +
                "   death_date            TEXT" +
                ");"
        val groups = "CREATE TABLE groups (" +
                "   id_group              INTEGER PRIMARY KEY," +
                "   group_name                  TEXT," +
                "   start_date            TEXT," +
                "   end_date              TEXT" +
                ");"
        val albums = "CREATE TABLE albums (" +
                "   id_album              INTEGER PRIMARY KEY," +
                "   path                  TEXT," +
                "   album_name            TEXT," +
                "   year                  INTEGER" +
                ");"
        val rolas = "CREATE TABLE rolas (" +
                "   id_rola               INTEGER PRIMARY KEY," +
                "   id_performer          INTEGER," +
                "   id_album              INTEGER," +
                "   path                  TEXT," +
                "   title                 TEXT," +
                "   track                 INTEGER," +
                "   year                  INTEGER," +
                "   genre                 TEXT," +
                "   FOREIGN KEY (id_performer) REFERENCES performers(id_performer)," +
                "   FOREIGN KEY (id_album)     REFERENCES albums(id_album)" +
                ");"
        val in_group = "CREATE TABLE in_group (" +
                "   id_person             INTEGER," +
                "   id_group              INTEGER," +
                "   PRIMARY KEY (id_person, id_group)," +
                "   FOREIGN KEY (id_person)    REFERENCES persons(id_person)," +
                "   FOREIGN KEY (id_group)     REFERENCES groups(id_group)" +
                ");"

        db?.execSQL(types)
        db?.execSQL(insertPerson)
        db?.execSQL(insertGroup)
        db?.execSQL(insertUnknown)
        db?.execSQL(performers)
        db?.execSQL(persons)
        db?.execSQL(groups)
        db?.execSQL(albums)
        db?.execSQL(rolas)
        db?.execSQL(in_group)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

}