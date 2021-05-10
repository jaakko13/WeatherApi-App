package javi.toxic.weatherfinal

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


var set = HashSet<String>()
var betterUnits: Boolean = true //used to keep desired units.


class MainActivity : AppCompatActivity() {

    val PID: Int = 1000
    val API: String = "9924be9724fd70a935b3f521b109bbe6"
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var cityLocation: TextView
    lateinit var add: Button
    lateinit var time: TextView
    lateinit var new: EditText
    lateinit var go: Button
    lateinit var celsius: Button
    lateinit var fahrenheit: Button
    lateinit var myLocation: String
    lateinit var myLocButton: Button
    var places = ArrayList<String>()!! //arrayListOf     arrayListOf<String>()

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
        celsius = findViewById(R.id.celsius)
        fahrenheit = findViewById(R.id.fahrenheit)
        myLocButton = findViewById(R.id.myLocButton)

        //used to get latitude and longitude to be able to get city name and country code.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        val backIntent = getIntent()
        val tapped = backIntent.getStringExtra("tapped")



        //loads inputs from last time and uses to fill locationList with last locations
        loadSharedPreferences()
        
        //Gets click from locationList activity to bring new info to MainActivity
        if(tapped != null){
            Log.d("see", tapped)
            //loadSharedPreferences()
            thread {
                try {
                    //getDataChecked(tapped)
                    if (betterUnits == true){
                        getDataNormal(tapped, "metric")
                    }else if(betterUnits == false){
                        getDataNormal(tapped, "imperial")
                    }
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



        //gets input and saves it if not a repeat and the gets data from api
        go.setOnClickListener{
            val place = newLocation.text.toString()
                thread {
                    try {
                        if (betterUnits == true) {
                            getDataNormal(place, "metric")
                        } else if (betterUnits == false) {
                            getDataNormal(place, "imperial")
                        }
                        if(place !in places) {
                            saveSharedPreferences(place)
                        }
                    } catch (e: Exception) {
                        Looper.prepare()
                        Toast.makeText(this, "Not Valid Location", Toast.LENGTH_SHORT).show()
                        Thread.sleep(2000)
                    }

            }
        }

        celsius.setOnClickListener{
            if (cityLocation.text == "Location"){
                Toast.makeText(this, "Choose a Location First", Toast.LENGTH_SHORT).show()
            }else{
                betterUnits = true
                thread {
                    getDataNormal(cityLocation.text.toString(), "metric")
                }
            }
        }

        fahrenheit.setOnClickListener{
            if (cityLocation.text == "Location"){
                Toast.makeText(this, "Choose a Location First", Toast.LENGTH_SHORT).show()
            }else{
                betterUnits = false
                thread {
                    getDataNormal(cityLocation.text.toString(), "imperial")
                }
            }
        }

        myLocButton.setOnClickListener{
            getLastLocation()
        }
    }

    //All UI changes made here
    fun postExecute(result: String?, unit: String) {

        val jsonObj = JSONObject(result)
        Log.d("json", jsonObj.toString())
        val main = jsonObj.getJSONObject("main")
        val sys = jsonObj.getJSONObject("sys")
        val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
        val pressure = main.getString("pressure")
        val humidity = main.getString("humidity")
        val temp = main.getString("temp") + unit
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
    private fun getDataNormal(location: String, unit: String){
        var response: String? = ""

        response = URL("https://api.openweathermap.org/data/2.5/weather?q=${location}&units=${unit}&APPID=$API").readText(Charsets.UTF_8)
        Log.d("msg", response!!)

        if(betterUnits == true){
            runOnUiThread { postExecute(response, "°C") }
        }else{
            runOnUiThread { postExecute(response, "°F") }
        }
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

    @SuppressLint("MissingPermission")
    fun getLastLocation(){
        if(CheckPermission()){
            if(isLocationEnabled()){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {task->
                    var location: Location? = task.result
                    if(location == null){
                        NewLocationData()
                    }else{
                        myLocation = getCityName(location.latitude,location.longitude)

                        thread {
                            if (betterUnits == true) {
                                getDataNormal(myLocation, "metric")
                            } else if (betterUnits == false) {
                                getDataNormal(myLocation, "imperial")
                            }
                        }

                    }
                }
            }else{
                Toast.makeText(this,"Location Services are off.",Toast.LENGTH_SHORT).show()
            }
        }else{
            RequestPermission()
        }
    }

    @SuppressLint("MissingPermission")
    fun NewLocationData(){
        var locationRequest =  LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest,locationCallback,Looper.myLooper()
        )
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation
            myLocation = getCityName(lastLocation.latitude, lastLocation.longitude)

            thread {
                if (betterUnits == true) {
                    getDataNormal(myLocation, "metric")
                } else if (betterUnits == false) {
                    getDataNormal(myLocation, "imperial")
                }
            }
        }
    }

    //checks if permission has been given for location service
    private fun CheckPermission():Boolean{
        if(
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }
        return false
    }

    //requests permission for location service
    fun RequestPermission(){
        ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION),
                PID
        )
    }

    //Checks if location service is enabled
    fun isLocationEnabled():Boolean{
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    //gets name of city
    private fun getCityName(lat: Double,long: Double):String{
        var cityName:String = ""
        var geoCoder = Geocoder(this, Locale.getDefault())
        var address = geoCoder.getFromLocation(lat,long,3)

        cityName = address.get(0).locality

        return cityName
    }

}





