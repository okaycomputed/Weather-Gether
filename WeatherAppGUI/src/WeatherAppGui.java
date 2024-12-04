import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;

    public WeatherAppGui() {
        // Setting up GUI and adding a title
        super("Weather App");

        // Configures GUI to end after the program has been closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Sets the size of the GUI (in pixels)
        setSize(450, 730);

        // Loads GUI at the center of the screen
        setLocationRelativeTo(null);

        // Set null in order to manually positions components within GUI
        setLayout(null);

        // Prevents resizing
        setResizable(false);

        addGuiComponents();
    }

    private void updateGuiComponents(JTextField searchTextField, JLabel locationNameText, JLabel weatherConditionImage,
                                     JLabel temperatureText, JLabel weatherConditionDesc, JLabel humidityText, JLabel windspeedText) {
        // Shows the weather of the user's current location when the application boots up
        weatherData = WeatherApp.getWeatherData(WeatherApp.getCurrLocation());

        // If the search text field is not empty, use the user input as a parameter for the API call
        if(!searchTextField.getText().isEmpty()) {
            // Get location from user
            String userInput = searchTextField.getText();

            // Validate input - remove whitespace to ensure non-empty text
            if (userInput.replaceAll("\\s", "").isEmpty()) {
                return;
            }
            // Retrieve weather data
            weatherData = WeatherApp.getWeatherData(userInput);
        }

        // Updates the GUI
        // Update location name text
        String locationName = (String) weatherData.get("location_name");
        locationNameText.setText(locationName);

        // Update weather image
        String weatherCondition = (String) weatherData.get("weather_condition");

        // Depending on the condition, update the weather image that corresponds with the condition
        switch(weatherCondition) {
            case "Clear":
                weatherConditionImage.setIcon(loadImage("src/assets/sunny2.png"));
                weatherConditionImage.setBounds(0, 130, 450, 415);
                break;

            case "Cloudy":
                weatherConditionImage.setIcon(loadImage("src/assets/cloudy2.png"));
                weatherConditionImage.setBounds(0, 185, 450, 235);
                break;

            case "Rainy":
                weatherConditionImage.setIcon(loadImage("src/assets/rainy3.png"));
                weatherConditionImage.setBounds(0, 150, 450, 350);
                break;

            case "Snow":
                weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                weatherConditionImage.setBounds(0, 205, 450, 235);
                break;
        }

        // Update temperature text
        double temperature = (double) weatherData.get("temperature");
        temperatureText.setText(temperature + " C");

        // Update weather condition text
        weatherConditionDesc.setText(weatherCondition);

        // Update humidity text
        long humidity = (long) weatherData.get("humidity");
        humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");

        // Update windspeed text
        double windspeed = (double) weatherData.get("windspeed");
        windspeedText.setText("<html><b>Wind Speed</b> " + windspeed + "km/h</html>");
    }

    private void addGuiComponents() {
        // Searching field
        JTextField searchTextField = new JTextField();

        // Sets location and size of component
        searchTextField.setBounds(15, 15, 351, 45);

        // Change font style and size
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        add(searchTextField);

        /* Adding GUI components but using dummy values first to specify: font, size, and alignment */
        // Location name text
        JLabel locationNameText = new JLabel("Tokyo, Japan");
        locationNameText.setBounds(0, 90, 450, 54);
        locationNameText.setFont(new Font("Dialog", Font.BOLD, 35));
        locationNameText.setHorizontalAlignment(SwingConstants.CENTER);
        add(locationNameText);

        // Weather image
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/rainy3.png"));
        weatherConditionImage.setBounds(0, 150, 450, 350);
        add(weatherConditionImage);

        // Temperature text
        JLabel temperatureText = new JLabel("10 C");
        temperatureText.setBounds(0, 450, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));
        // Center the text
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // Weather condition description
        JLabel weatherConditionDesc = new JLabel("Rainy");
        weatherConditionDesc.setBounds(0, 510, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        // Humidity image
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity3.png"));
        humidityImage.setBounds(15, 570, 74, 66);
        add(humidityImage);

        // Humidity text
        JLabel humidityText = new JLabel("<html><b>Humidity<b></b> 51%</html>");
        humidityText.setBounds(90, 570, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        // Windspeed image
        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(220, 570, 74, 66);
        add(windspeedImage);

        // Windspeed text
        JLabel windspeedText = new JLabel("<html><b>Wind Speed<b></b> 49km/h</html>");
        windspeedText.setBounds(310, 570, 95, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        // Adding search button
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));

        // Change the cursor to a hand cursor when hovering over the search button
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);

        // Before the search button is pressed, use the geolocation API to get the current location's weather data
        // and update the GUI accordingly
        updateGuiComponents(searchTextField, locationNameText, weatherConditionImage,
                temperatureText, weatherConditionDesc, humidityText, windspeedText);

        // After the search button is pressed, the program will begin to make API calls
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGuiComponents(searchTextField, locationNameText, weatherConditionImage,
                        temperatureText, weatherConditionDesc, humidityText, windspeedText);
            }
        });
        add(searchButton);
    }

    private ImageIcon loadImage(String resourcePath) {
        try {
            // Reads the image file from the path given
            BufferedImage image = ImageIO.read(new File(resourcePath));

            // Returns an image icon so the component can render it
            return new ImageIcon(image);
        } catch(IOException e) {
            e.printStackTrace();
        }
        System.out.println("Could not find resource");
        return null;
    }
}