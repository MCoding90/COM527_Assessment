package com.example.maciejczerwonka

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration

class CreateStopActivity: AppCompatActivity() {
    override fun onCreate(SavedInstanceState: Bundle?){
        super.onCreate(SavedInstanceState)

        // This line sets the user agent, a requirement to download OSM maps
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_create_stop)

        val createStop = findViewById<Button>(R.id.addBusStopbtn)

        createStop.setOnClickListener {
            val etroutenumber = findViewById<EditText>(R.id.etroutenumber)
            val getetroutenumber = etroutenumber.getText().toString()

            val etbuscompany =  findViewById<EditText>(R.id.etbuscompany)
            val getetbuscompany = etbuscompany.getText().toString()

            val etfinaldestination = findViewById<EditText>(R.id.etfinaldestination)
            val getetfinaldestination = etfinaldestination.getText().toString()

            val intent = Intent()

            val bundle = bundleOf("com.example.maciejczerwonka.Routenumber" to getetroutenumber,
                "com.example.maciejczerwonka.BusCompany" to getetbuscompany,
                "com.example.maciejczerwonka.Finaldestination" to getetfinaldestination)
            intent.putExtras(bundle)

            setResult(RESULT_OK, intent)
            finish()
        }
    }
}