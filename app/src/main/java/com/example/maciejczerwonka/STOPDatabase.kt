package com.example.maciejczerwonka

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(STOP::class), version = 1, exportSchema =  false)
abstract class STOPDatabase: RoomDatabase() {
    abstract fun stopDAO(): stopDAO

    companion object {
        private var instance: STOPDatabase? = null

        fun getDatabase(ctx:Context) : STOPDatabase {
            var tmpInstance =  instance

            if(tmpInstance == null){
                tmpInstance = Room.databaseBuilder(
                    ctx.applicationContext,
                    STOPDatabase::class.java,
                    "stopDatabase"
                ).build()
                instance = tmpInstance
            }
            return tmpInstance
        }
    }

}