package com.example.weatherfinal

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.concurrent.thread


var places = ArrayList<String>()!! //arrayListOf     arrayListOf<String>()
var set = HashSet<String>()


class MainActivity : AppCompatActivity() {

    var pID = 1000
    val API: String = "9924be9724fd70a935b3f521b109bbe6"
    var city: String = ""
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var cityLocation: TextView
    lateinit var add: Button
    lateinit var time: TextView
    lateinit var new: EditText
    lateinit var go: Button

    lateinit var sp: SharedPreferences
    val SHARED_PREFS: String = "sharedPrefs"
    //val loadList = HashSet<String>()



    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        cityLocation = findViewById(R.id.cityLocation)
        //button = findViewById(R.id.button)
        time = findViewById(R.id.time)
        add = findViewById(R.id.add)
        go = findViewById(R.id.go)
        new = findViewById(R.id.newLocation)
        sp = getSharedPreferences("locationHistory", Context.MODE_PRIVATE)

        //used to get latitude and longitude to be able to get city name and country code.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        val backIntent = getIntent()
        var tapped = backIntent.getStringExtra("tapped")

        if(tapped != null){
            Log.d("see", tapped)
            thread {
                try {
                    var response: String? = ""
                    response = URL("https://api.openweathermap.org/data/2.5/weather?q=${tapped}&units=metric&APPID=$API").readText(Charsets.UTF_8)
                    Log.d("msg", response!!)


                    runOnUiThread { postExecute(response) }
                } catch (e: Exception) {
                    Toast.makeText(this, "Not Valid Location", Toast.LENGTH_SHORT).show()
                }
            }
        }


        preExecute()
        //getNewLocation()
        Log.d("msg", city);


        //var some: ArrayList<String>






        add.setOnClickListener{





            val intent = Intent(this, locationList::class.java)

            //intent.putExtra("Counter", counter)
            intent.putExtra("List", places)
            startActivity(intent)
        }


        var counter: Int = 0

        //maybe put preferences stuff on onDestroy???
        var sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)

        var loadSet = sharedPreferences.getStringSet("key", HashSet<String>())?.toMutableList() as ArrayList<String>
        Log.d("load", loadSet.toString())
        places = loadSet //as ArrayList<String>

        go.setOnClickListener{
                thread {
                    val place = newLocation.text.toString()
                    var stringCounter = counter.toString()

                    if(place !in places){
                        places.add(place)
                        //editor.putStringSet("key", places)
                        //editor.commit()
                        var sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
                        var editor = sharedPreferences.edit()



                        set.add(place)
                        editor.putStringSet("key", set);
                        editor.commit();

                        Log.d("set", set.toString())




                    }
                    counter++
                    try {
                        var response: String? = ""
                        response = URL("https://api.openweathermap.org/data/2.5/weather?q=${newLocation.text.toString()}&units=metric&APPID=$API").readText(Charsets.UTF_8)
                        Log.d("msg", response!!)


                        runOnUiThread { postExecute(response) }

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

    fun preExecute(){
        findViewById<ProgressBar>(R.id.progress).visibility = View.GONE
        findViewById<RelativeLayout>(R.id.mainTing).visibility = View.VISIBLE
        Log.d("preCity", city)
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

        findViewById<RelativeLayout>(R.id.mainTing).visibility = View.VISIBLE
        findViewById<ProgressBar>(R.id.progress).visibility = View.GONE


    }


    @SuppressLint("MissingPermission")
    private fun getLastLocation(){
        if(checkPermissions()){
            if(isLocationEnabled()){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener{ task ->
                    var location: Location? = task.result
                    if(location == null){

                    }else{
                        cityLocation.text = getCityName(location.latitude, location.longitude) + ", " + getCountryCode(location.latitude, location.longitude)
                        city = getCityName(location.latitude, location.longitude) + "," + getCountryCode(location.latitude, location.longitude)
                    }
                }
            }else{
                Toast.makeText(this, "You need to enable location services.", Toast.LENGTH_SHORT).show()
            }
        }else{
            RequestPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation(){
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2
        fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest, locationCallback, Looper.myLooper()
        )
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation: Location = p0.lastLocation
            cityLocation.text = getCityName(lastLocation.latitude, lastLocation.longitude) + ", " + getCountryCode(lastLocation.latitude, lastLocation.longitude)
            city = getCityName(lastLocation.latitude, lastLocation.longitude) + "," + getCountryCode(lastLocation.latitude, lastLocation.longitude)
        }
    }

    private fun updateTime(){
        val currentDateTime = LocalDateTime.now()
        val date = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        time.text = "Last Updated: " + date
    }

    private fun checkPermissions(): Boolean{
        if(
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }
        return false
    }

    private fun RequestPermission(){
        ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), pID
        )
    }

    private fun isLocationEnabled(): Boolean{
        var locationCheck: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationCheck.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationCheck.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == pID){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("MSG", "Permission Good.")
            }
        }
    }

    private fun getCityName(latitude: Double, longitude: Double): String{
        var cityName = ""
        var geoCode = Geocoder(this, Locale.getDefault())
        var address = geoCode.getFromLocation(latitude, longitude, 1)
        cityName = address.get(0).locality

        return cityName
    }

    private fun getCountryCode(latitude: Double, longitude: Double): String{
        var countryCode = ""
        var geoCode = Geocoder(this, Locale.getDefault())
        var address = geoCode.getFromLocation(latitude, longitude, 1)
        countryCode = address.get(0).countryCode

        return countryCode
    }

}





