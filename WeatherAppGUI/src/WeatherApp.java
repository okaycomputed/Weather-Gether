import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/* Retrieves weather data from API */
public class WeatherApp {
    public static String getCurrLocation() {
        // Building API request
        String urlString = "http://ip-api.com/json/?fields=status,message,country,regionName,city,query";

        try {
            // Call the API and get a response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // Check for response status
            // 200 - means that the connection was a success
            if(conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }
            // Store the json data from the API
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNext()) {
                // Reads and stores into the string builder
                resultJson.append(scanner.nextLine());
            }

            scanner.close();
            conn.disconnect();

            // Parse resultJson
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            //Retrieve the city name of the current IP address entered
            return (String) resultJsonObj.get("city");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Fetch weather data for given location
    public static JSONObject getWeatherData(String locationName) {
        JSONArray locationData = getLocationData(locationName);

        // Extract latitude and longitude data
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // Extract name of the location and country of the location
        String cityName = (String) location.get("name");
        String countryName = (String) location.get("country");
        String locationString = cityName;

        // Check if the countryName is null
        if(countryName != null) {
            // Form the location string to be displayed
            locationString = cityName + ", " + countryName;
        }

        // Build API request URL with location coordinates
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Asia%2FSingapore";

        try {
            // Call the API and get a response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // Check for response status
            // 200 - means that the connection was a success
            if(conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }
            // Store the json data from the API
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNext()) {
                // Reads and stores into the string builder
                resultJson.append(scanner.nextLine());
            }

            scanner.close();
            conn.disconnect();

            // Parse resultJson
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // Retrieve hourly weather data
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

            // Get current hour's data and retrieve INDEX of the current hour
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            // Get temperature
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            // Get weather code
            JSONArray weatherCode = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeatherCode((long) weatherCode.get(index));

            // Get humidity
            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            // Get wind speed
            JSONArray windSpeedData = (JSONArray) hourly.get("wind_speed_10m");
            double windspeed = (double) windSpeedData.get(index);

            // Building the weather JSON object to access in frontend
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);
            weatherData.put("location_name", locationString);

            return weatherData;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray getLocationData(String locationName) {
        // Replace any whitespace in location name to '+' to adhere to API's request format
        locationName = locationName.replaceAll(" ", "+");

        // build API url with location parameter
        // When calling the API, it will use the location entered
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + locationName + "&count=10&language=en&format=json";

        try {
            // Call the API and get a response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // Check response
            // 200 -> Successful connection
            if(conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
            }
            else {
                // Store the API results
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                // Read and store resulting json data into the string builder
                while(scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }

                // Close scanner
                scanner.close();

                // Close URL connection
                conn.disconnect();

                // Parse JSON string into JSON object
                JSONParser parser = new JSONParser();
                JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                // Get the list of location data generated from the API from the location name
                JSONArray locationData = (JSONArray) resultJsonObj.get("results");
                return locationData;
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null; // Could not find location
    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            // Attempt to create a connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set request method to "get"
            conn.setRequestMethod("GET");

            // Connect to API
            conn.connect();
            return conn;
        } catch(IOException e) {
            e.printStackTrace();
        }

        // Could not make a connection
        return null;
    }

    public static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();

        // Iterate through time list and see which one matches the current time
        for(int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if(time.equalsIgnoreCase(currentTime)) {
                // Returns the index
                return i;
            }
        }
        return 0; // Dummy value
    }

    private static String getCurrentTime() {
        // Get current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Format date to be 2024-09-02T00:00 (This is how it is read inside the API)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        // Format and print the current date and time
        return currentDateTime.format(formatter);
    }

    // Converts weather code to a readable string
    private static String convertWeatherCode(long weatherCode) {
        String weatherCondition = " ";
        // Clear
        if(weatherCode == 0L) {
            weatherCondition = "Clear";
        }

        // Cloudy
        else if(weatherCode <= 3L && weatherCode > 0L) {
            weatherCondition = "Cloudy";
        }

        // Rain
        else if((weatherCode >= 51L && weatherCode <= 67L ) || (weatherCode >= 80L && weatherCode <= 99L)) {
            weatherCondition = "Rainy";
        }

        // Snow
        else if(weatherCode >= 71L && weatherCode <= 77L) {
            weatherCondition = "Snow";
        }

        return weatherCondition;
    }
}
