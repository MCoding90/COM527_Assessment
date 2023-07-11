package com.example.maciejczerwonka

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface stopDAO {
    @Query("SELECT * FROM stop WHERE id= :id")
    fun getStopByID(id: Long): STOP

    @Query("SELECT* FROM stop")
    fun getAllStops(): LiveData<List<STOP>>

    @Query("DELETE FROM stop")
    fun deleteAllStops()

    @Insert
    fun insert(STOP: STOP) : Long

    @Update
    fun update(STOP: STOP): Int

    @Delete
    fun delete(STOP: STOP): Int

    }