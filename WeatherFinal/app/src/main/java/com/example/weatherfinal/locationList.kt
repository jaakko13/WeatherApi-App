package com.example.weatherfinal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*


class locationList : AppCompatActivity() {

    lateinit var list: ListView
    private var arrayAdapter: ArrayAdapter<String> ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_list)

        list = findViewById<ListView>(R.id.locationList)

        val intent = getIntent()
        val backIntent = Intent(this, MainActivity::class.java)
        var places: ArrayList<String> = intent.getStringArrayListExtra("List") as ArrayList<String> //gets data from MainActivity

        //used to make changes to list in ListView
        arrayAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, places)
        list?.adapter = arrayAdapter

        list.onItemClickListener = object : AdapterView.OnItemClickListener {

            //sends new locaiton back to MainActivity
            override fun onItemClick(parent: AdapterView<*>, view: View,
                                     position: Int, id: Long) {
                val tappedString = places[position]
                backIntent.putExtra("tapped", tappedString)
                backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // makes sure NOT to add new instace to stack
                startActivity(backIntent)
            }
        }
    }
}