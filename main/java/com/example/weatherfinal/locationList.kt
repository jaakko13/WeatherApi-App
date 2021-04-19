package com.example.weatherfinal

import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.graphics.Insets.add
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.OneShotPreDrawListener.add
import org.w3c.dom.ls.LSException
import kotlin.properties.Delegates

class locationList : AppCompatActivity() {

    lateinit var list: ListView
    //private var items = arrayListOf<String>()
    private var arrayAdapter: ArrayAdapter<String> ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_list)

        list = findViewById<ListView>(R.id.locationList)

        val intent = getIntent()
        val backIntent = Intent(this, MainActivity::class.java)


        //var place: String = intent.getStringExtra("Location").toString()
        //var counter: Int = intent.getIntExtra("Counter", 1)
        var places: ArrayList<String> = intent.getStringArrayListExtra("List") as ArrayList<String>

        arrayAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, places)
        list?.adapter = arrayAdapter

        list.onItemClickListener = object : AdapterView.OnItemClickListener {

            override fun onItemClick(parent: AdapterView<*>, view: View,
                                     position: Int, id: Long) {
                val tappedString = places[position]
                //Log.d("msg", tappedString)
                backIntent.putExtra("tapped", tappedString)
                backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // makes sure NOT to add new instace to stack
                startActivity(backIntent)
            }
        }
    }
}