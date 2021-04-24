# WeatherApi-App    Jaakko Virtanen

# Topic
Android application that shows weather in your location. Able to keep list of multiple cities at once. Using the https://openweathermap.org/api api. Switch between Fahrenheit and Celsius. 

# Taregt
Android

# Language
Kotlin

# Release 1: 2021-04-26 features
User is able to input cities all over the world and get weather description, temperature, humidity, pressure, and time updated.

User inputs are stored using shared preferences and will be stored for the next time the app is used. 

User is able to navigate between Main Activity and list of all locations activity

User is able to select locations from history list to show all data values for that location.

# Release 2: 2021-05-14 features (for the future)
Button to switch between Celsius and farhenheit.

Use phones location api to automatically get weather data for users location.

Fix shared Preferences bug. (1)

# Known bugs
1. If there are objects stored in shared preferences the app is closed and opened the list of locations is stored but if new ones are added the first set may disappear.
