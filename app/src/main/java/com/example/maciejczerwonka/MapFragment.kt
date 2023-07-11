package com.example.maciejczerwonka

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem

class MapFragment:androidx.fragment.app.Fragment(R.layout.map_fragment) {
    val viewModel: StopViewModel by activityViewModels()
    lateinit var map1: MapView
    lateinit var overlay_items: ItemizedIconOverlay<OverlayItem>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        map1 = view.findViewById<MapView>(R.id.map1)
        map1.controller.setZoom(12.0)
        map1.controller.setCenter(GeoPoint(50.90, -1.40))
        overlay_items = ItemizedIconOverlay(activity, arrayListOf<OverlayItem>(), null)
        map1.overlays.add(overlay_items)

        viewModel.getAllStops().observe(this.viewLifecycleOwner, Observer {
            for(stop in it) {
                val newStop = OverlayItem(stop.routenumber, "${stop.buscompany}: ${stop.finaldestination}", GeoPoint(stop.latitude, stop.longitude))
                overlay_items.addItem(newStop)
            }
            map1.invalidate()
        })

        val markerGestureListener = object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
            override fun onItemLongPress(i: Int, item: OverlayItem): Boolean {
                Toast.makeText(activity, item.snippet, Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onItemSingleTapUp(i: Int, item: OverlayItem): Boolean {
                Toast.makeText(activity, item.snippet, Toast.LENGTH_SHORT).show()
                return true
            }
        }

        overlay_items = ItemizedIconOverlay(activity, arrayListOf<OverlayItem>(), markerGestureListener)
        map1.overlays.add(overlay_items)
    }
}