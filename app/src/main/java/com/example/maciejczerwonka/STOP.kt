package com.example.maciejczerwonka

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="stop")
data class STOP(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val routenumber: String,
    val buscompany: String,
    val finaldestination: String,
    val latitude: Double,
    val longitude: Double
)