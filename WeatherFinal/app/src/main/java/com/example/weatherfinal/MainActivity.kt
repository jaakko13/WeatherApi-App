package com.example.weatherfinal

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.concurrent.thread


var places = ArrayList<String>()!! //arrayListOf     arrayListOf<String>()
var set = HashSet<String>()

class MainActivity : AppCompatActivity() {

    val API: String = "9924be9724fd70a935b3f521b109bbe6"
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var cityLocation: TextView
    lateinit var add: Button
    lateinit var time: TextView
    lateinit var new: EditText
    lateinit var go: Button

    lateinit var sp: SharedPreferences
    val SHARED_PREFS: String = "sharedPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityLocation = findViewById(R.id.cityLocation)
        time = findViewById(R.id.time)
        add = findViewById(R.id.add)
        go = findViewById(R.id.go)
        new = findViewById(R.id.newLocation)
        sp = getSharedPreferences("locationHistory", Context.MODE_PRIVATE)

        //used to get latitude and longitude to be able to get city name and country code.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        val backIntent = getIntent()
        var tapped = backIntent.getStringExtra("tapped")


        //Gets click from locationList activity to bring new info to MainActivity
        if(tapped != null){
            Log.d("see", tapped)
            thread {
                try {
                    getDataChecked(tapped)
                } catch (e: Exception) {
                    Toast.makeText(this, "Not Valid Location", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //Goes to locationList activity
        add.setOnClickListener{
            val intent = Intent(this, locationList::class.java)

            intent.putExtra("List", places)
            startActivity(intent)
        }

        //loads inputs from last time and uses to fill locaitonList with last locations
        loadSharedPreferences()

        //gets input and saves it if not a repeat and the gets data from api
        go.setOnClickListener{
                thread {
                    val place = newLocation.text.toString()
                    if(place !in places){
                        saveSharedPreferences(place)
                    }
                    try {
                        getDataNormal()
                    }
                    catch (e: Exception){
                        Looper.prepare()
                        Toast.makeText(this, "Not Valid Location", Toast.LENGTH_SHORT).show()
                        Thread.sleep(2000)
                        places.removeLast()
                    }
            }
        }
    }

    fun postExecute(result: String?) {

        val jsonObj = JSONObject(result)
        Log.d("json", jsonObj.toString())
        val main = jsonObj.getJSONObject("main")
        val sys = jsonObj.getJSONObject("sys")
        val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
        val pressure = main.getString("pressure")
        val humidity = main.getString("humidity")
        val temp = main.getString("temp") + "°C"
        val weatherDescription = weather.getString("description")
        val address = jsonObj.getString("name") + ", " + sys.getString("country")

        updateTime()

        findViewById<TextView>(R.id.cityLocation).text = address
        findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
        findViewById<TextView>(R.id.temp).text = temp
        findViewById<TextView>(R.id.pressure).text = pressure + " mBar"
        findViewById<TextView>(R.id.humidity).text = humidity + "%"
    }

    //update time
    private fun updateTime(){
        val currentDateTime = LocalDateTime.now()
        val date = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        time.text = "Last Updated: " + date
    }

    //gets data from api "normal way". Initial way
    private fun getDataNormal(){
        var response: String? = ""

        response = URL("https://api.openweathermap.org/data/2.5/weather?q=${newLocation.text.toString()}&units=metric&APPID=$API").readText(Charsets.UTF_8)
        Log.d("msg", response!!)
        runOnUiThread { postExecute(response) }
    }

    //gets data if clicked from locationList activity
    private fun getDataChecked(tapped: String){
        var response: String? = ""
        response = URL("https://api.openweathermap.org/data/2.5/weather?q=${tapped}&units=metric&APPID=$API").readText(Charsets.UTF_8)
        Log.d("msg", response!!)
        runOnUiThread { postExecute(response) }
    }

    //loads data from shared Preferences from last time
    private fun loadSharedPreferences(){
        var sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        var loadSet = sharedPreferences.getStringSet("key", HashSet<String>())?.toMutableList() as ArrayList<String>
        Log.d("load", loadSet.toString())
        places = loadSet
    }

    //saves data to shared Preferences
    private fun saveSharedPreferences(place: String){
        places.add(place)
        var sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        var editor = sharedPreferences.edit()

        set.add(place)
        editor.putStringSet("key", set);
        editor.commit();
    }

}





