package com.example.maciejczerwonka
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem

class MainActivity : AppCompatActivity(), LocationListener {

    lateinit var map1: MapView
    lateinit var overlay_items : ItemizedIconOverlay<OverlayItem>
    var longitude = 0.0
    var latitude = 0.0
    var stop_List = ArrayList<STOP>()
    var webStop_List = ArrayList<STOP>()
    var checkbox = false

    val addStoplauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {


        val returnIntent: Intent? = it.data

        if(it.resultCode ==  RESULT_OK) {
            it.data?.apply {
                val route = this.getStringExtra("com.example.maciejczerwonka.Routenumber").toString()
                val company = this.getStringExtra("com.example.maciejczerwonka.BusCompany").toString()
                val destination =  this.getStringExtra("com.example.maciejczerwonka.Finaldestination").toString()

                val newStop =  OverlayItem(route, "$company: $destination", GeoPoint(latitude, longitude))
                overlay_items.addItem(newStop)

                val newStopObject = STOP(0, route, company, destination, latitude, longitude)
                stop_List.add(newStopObject)

                if(checkbox == true){
                    // Define the URL for the web API endpoint
                    val url = "http://10.0.2.2:3000/busstop/create"

                    // Create a list of POST parameters to send to the web API
                    val parameters = listOf("route" to route, "operator" to company, "destination" to destination, "lat" to newStopObject.latitude, "lon" to newStopObject.longitude)

                    // Perform a POST request to the web API with the specified URL and parameters
                    url.httpPost(parameters).response{ request, response, result ->
                        when(result){
                            is Result.Success -> {
                                // If the POST request is successful, display a success message
                                Log.d("HttpPost", "POST request successful: ${result.get().decodeToString()}")
                                Toast.makeText(this@MainActivity, result.get().decodeToString(), Toast.LENGTH_LONG).show()
                            }
                            is Result.Failure -> {
                                // If the POST request failed, display an error message
                                Log.e("HttpPost", "POST request failed: ${result.error.message}")
                                Toast.makeText(this@MainActivity, result.error.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This line sets the user agent, a requirement to download OSM maps
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_main)
        checkPermissions()
        //call to checkPermissions function
        map1 = findViewById<MapView>(R.id.map1)
        map1.controller.setZoom(12.0)
        map1.controller.setCenter(GeoPoint(50.90, -1.40))
        // sets default location and zoom
        overlay_items =  ItemizedIconOverlay(this, arrayListOf<OverlayItem>(), null)
        map1.overlays.add(overlay_items)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.addBusStop -> {

                val intent = Intent(this, CreateStopActivity::class.java)

                addStoplauncher.launch(intent)
                return true
            }

            R.id.saveAllStops -> {

                val db = STOPDatabase.getDatabase(application)
                for (stop in stop_List) {

                    lifecycleScope.launch {
                        // Read in the bus stop Details from each STOP object within the stop_list.
                        val routenumber = stop.routenumber
                        val buscompany = stop.buscompany
                        val finaldestination = stop.finaldestination
                        val latitude = stop.latitude
                        val longitude = stop.longitude

                        // Variable to hold the ID allocated to the new record
                        var insertId = 0L

                        withContext(Dispatchers.IO) {
                            val savedSTOP = STOP(
                                0,
                                routenumber,
                                buscompany,
                                finaldestination,
                                latitude,
                                longitude
                            )

                            insertId = db.stopDAO().insert(savedSTOP)
                        }
                    }
                }
                stop_List.clear()
            }

            R.id.preferences -> {

                val intent = Intent(this, MyPreferencesActivity::class.java)

                startActivity(intent)
                return true
            }

            R.id.loadStops -> {
                //Clear all unsaved Stops in the stop_List before loading locally saved Stops
                stop_List.clear()
                //Clear any markers loaded from web to show markers saved locally
                overlay_items.removeAllItems()
                val db = STOPDatabase.getDatabase(application)
                lifecycleScope.launch {

                    var savedStops: List<STOP>

                    withContext(Dispatchers.IO) {
                        //Retrieve all Bus Stops from Database
                        savedStops = db.stopDAO().getAllStops()
                    }
                    //iterate through saved stops
                    for (savedStop in savedStops) {
                        val routenumber = savedStop.routenumber
                        val buscompanmy = savedStop.buscompany
                        val finaldestination = savedStop.finaldestination
                        val latitude = savedStop.latitude
                        val longitude = savedStop.longitude
                        //Assign temporary saved stop
                        val templocalStop = OverlayItem(
                            routenumber,
                            buscompanmy,
                            finaldestination,
                            GeoPoint(latitude, longitude)
                        )
                        overlay_items.addItem(templocalStop)
                    }
                }
            }

            R.id.loadWebStops -> {
                //Clear all unsaved Stops in the stop_List before loading web saved Stops
                stop_List.clear()

                //Clear any markers loaded locally to show only web saved markers
                overlay_items.removeAllItems()

                val url = "http://10.0.2.2:3000/busstops/all"

                url.httpGet().responseJson { request, response, result ->
                    when (result) {
                        is Result.Success -> {
                            val jsonArray = result.get().array()
                            var str = ""

                            for (i in 0 until jsonArray.length()) {
                                val currentSTOPObject = jsonArray.getJSONObject(i)
                                val routenumber = currentSTOPObject.getString("route")
                                val buscompany = currentSTOPObject.getString("company")
                                val destination = currentSTOPObject.getString("destination")
                                val longitude = currentSTOPObject.getDouble("lon")
                                val latitude = currentSTOPObject.getDouble("lat")

                                val webStop =
                                    STOP(0, routenumber, buscompany, destination, latitude, longitude)
                                webStop_List.add(webStop)
                            }

                            for (webStop in webStop_List) {
                                val tempWebStop = OverlayItem(
                                    webStop.routenumber,
                                    webStop.buscompany,
                                    webStop.finaldestination,
                                    GeoPoint(webStop.latitude, webStop.longitude)
                                )
                                overlay_items.addItem(tempWebStop)
                            }
                        }

                        is Result.Failure -> {
                          //  Log.e("LoadWebStops", "Error loading web stops: ${e.message}")
                            Toast.makeText(
                                this@MainActivity,
                                result.error.message,
                                Toast.LENGTH_LONG

                            ).show()
                        }
                    }
                }
            }
        }
        return false
    }

    fun checkPermissions(){
        // Check to see if GPS permission has been granted already
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==  PackageManager.PERMISSION_GRANTED){
            requestLocation()
        } else {
            //If the permission hasn't been granted yet, request it from the user.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }
    }

    fun requestLocation() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==  PackageManager.PERMISSION_GRANTED){
            // note the use of 'as' to perform type casting in Kotlin
            // getSystemService() returns a superclass type of LocationManager,
            // so we need to cast it to LocationManager.
            val mgr = getSystemService(LOCATION_SERVICE) as LocationManager

            mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0f, this)

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions:Array<String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            0 -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    requestLocation()
                }
                else{
                    AlertDialog.Builder(this)
                        .setPositiveButton("OK", null) // add an OK button with an optional event handler
                        .setMessage("You have not accepted request for Location.") // set the message
                        .show() // show the dialog
                }
            }
        }
    }

    override fun onLocationChanged(newLoc: Location) {
        val map1 = findViewById<MapView>(R.id.map1)
        map1.controller.setZoom(12.0)
        map1.controller.setCenter(GeoPoint(newLoc.latitude, newLoc.longitude))

        latitude = newLoc.latitude
        longitude = newLoc.longitude

        Toast.makeText (this, "Location=${newLoc.latitude},${newLoc.longitude}", Toast.LENGTH_LONG).show()
    }

   override fun onProviderDisabled(provider: String) {
        Toast.makeText (this, "Provider disabled", Toast.LENGTH_LONG).show()
    }

   override fun onProviderEnabled(provider: String) {
        Toast.makeText (this, "Provider enabled", Toast.LENGTH_LONG).show()
    }

    // Deprecated at API level 29, but must still be included, otherwise your
    // app will crash on lower-API devices as their API will try and call it
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onStart(){
        super.onStart()
        Log.d("lifecycle", "onStart")
    }

    override fun onResume() {
        super.onResume()

        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        val checkBox = prefs.getBoolean("uploadStopsToWeb", false)

        if(checkBox){
            Log.d("lifecycle", "Checkbox Preference successfully implemented")
        }
    }

    override fun onStop(){
        super.onStop()
        Log.d("lifecycle", "onStop")
    }

    override fun onPause(){
        super.onPause()
        Log.d("lifecycle", "onPause")
    }

    override fun onDestroy(){
        super.onDestroy()
        Log.d("lifecycle", "onDestroy")
    }
}