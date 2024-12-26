package de.procceed.cloud.roadtocloudnative.controller;

import de.procceed.cloud.roadtocloudnative.model.WeatherData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Controller
public class WeatherController {
    private final String defaultLocation = "Nürnberg";

    @Autowired
    private Environment env;

    @Value("${TARGET:World}")
    String target;

    @Value("${echo.server.url:http://localhost:8081/echo}")
    private String echoServerUrl;

    @Autowired
    private RestTemplate restTemplate;

    Map<String, WeatherData> weatherDataMap = Map.of(
            "Nürnberg", new WeatherData(25.0, "cloudless"),
            "Fürth", new WeatherData(-5.3, "rainy")
    );

    @GetMapping("v1/weather")
    public String getWeather(Model model, @RequestParam(name = "location") Optional<String> optLocation) {
        String location = optLocation.orElse(defaultLocation);

        if (weatherDataMap.containsKey(location)) {
            WeatherData weatherData = weatherDataMap.get(location);

            // Send weather data to the Echo server
            String echoResponse = restTemplate.postForObject(echoServerUrl, weatherData, String.class);

            model.addAttribute("weatherDataAvailable", true);
            model.addAttribute("weatherData", weatherData);
            model.addAttribute("echoResponse", echoResponse); // Include response from Echo server
        } else {
            model.addAttribute("weatherDataAvailable", false);
        }

        model.addAttribute("location", location);
        model.addAttribute("hostname", env.getProperty("hostname"));

        return "main"; // Render the "main.html" template
    }

    @GetMapping("/")
    String hello() {
        return "Hello " + target + "!";
    }
}

package de.procceed.cloud.roadtocloudnative.model;

import java.io.Serializable;

public class WeatherData implements Serializable {
    private double temperature;
    private String description;

    // Constructors
    public WeatherData(double temperature, String description) {
        this.temperature = temperature;
        this.description = description;
    }

    public WeatherData() {}

    // Getters and setters
    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "temperature=" + temperature +
                ", description='" + description + '\'' +
                '}';
    }
}

package de.procceed.cloud.roadtocloudnative.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@RestController
public class EchoController {

    @PostMapping("/echo")
    public String echoWeatherData(@RequestBody WeatherData weatherData) {
        return "Echo: " + weatherData.toString();
    }
}
