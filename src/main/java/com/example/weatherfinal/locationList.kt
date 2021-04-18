package com.example.weatherfinal

import android.content.Context
import android.content.Intent.getIntent
import android.graphics.Insets.add
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
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


        var place: String = intent.getStringExtra("Location").toString()
        var counter: Int = intent.getIntExtra("Counter", 1)
        var places: ArrayList<String> = intent.getStringArrayListExtra("List") as ArrayList<String>

        arrayAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, places)
        list?.adapter = arrayAdapter

        //list.adapter = adapter(this, counter, place, places)
    }

    /*
    private class adapter(context: Context, counter: Int, place: String, places: ArrayList<String>): BaseAdapter(){

        val mContext: Context
        //var mcounter by Delegates.notNull<Int>()
        var mcounter: Int
        lateinit var mplace: String
        lateinit var mplaces: ArrayList<String>

        init{
            mContext = context
            mcounter = counter
            mplace = place
            mplaces = places
        }


        override fun getCount(): Int {
            return mcounter
        }

        override fun getItem(position: Int): Any {
            return "Test String"
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val textView = TextView(mContext)
            textView.text = mplaces.toString()
            return textView

        }

    }

     */
}