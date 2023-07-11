package com.example.maciejczerwonka

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration

class CreateStopFragment:androidx.fragment.app.Fragment(R.layout.create_stop_fragment) {
    val viewModel : StopViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        Configuration.getInstance().load(requireActivity(), PreferenceManager.getDefaultSharedPreferences(requireActivity()))

        view.findViewById<Button>(R.id.addBusStopbtn).setOnClickListener {
            val etroutenumber = view.findViewById<EditText>(R.id.etroutenumber)
            val getetroutenumber = etroutenumber.getText().toString()

            val etbuscompany = view.findViewById<EditText>(R.id.etbuscompany)
            val getetbuscompany = etbuscompany.getText().toString()

            val etfinaldestination = view.findViewById<EditText>(R.id.etfinaldestination)
            val getetfinaldestination = etfinaldestination.getText().toString()

            val newStop = STOP(0,getetroutenumber, getetbuscompany, getetfinaldestination,viewModel.latitude, viewModel.longitude)

            viewModel.addStop(newStop)
        }
    }
}
