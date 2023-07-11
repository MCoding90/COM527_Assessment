package com.example.maciejczerwonka

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Pass in the Application object
class StopViewModel (app: Application): AndroidViewModel(app) {
    var longitude = 0.0
    var latitude = 0.0

    // Get a reference to the database, using the Application object
    var db = STOPDatabase.getDatabase(app)
    var stops: LiveData<List<STOP>>

    // When we initialise the ViewModel, get the LiveData from the DAO
    // The variable 'stops' will always contain the latest LiveData.
    init {
        stops = db.stopDAO().getAllStops()
    }

    // Return the LiveData, so it can be observed, e.g. from the MainActivity
    fun getAllStops(): LiveData<List<STOP>> {
        return stops
    }

    fun addStop(newStop: STOP) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                db.stopDAO().insert(newStop)
            }
        }
    }
}