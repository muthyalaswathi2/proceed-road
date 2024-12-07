package de.procceed.cloud.roadtocloudnative.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("v1")
public class WeatherApiController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private Environment env;

    private final String defaultLocation = "Nürnberg";

    @Value("${TARGET:World}")
    String target;

    @GetMapping("weather")
    public Map<String, Object> getWeather(@RequestParam(name = "location") Optional<String> optLocation) {
        System.out.println("API controller handling request for /v1/weather");

        // Decode location to handle special characters like "ü"
        String location = URLDecoder.decode(optLocation.orElse(defaultLocation), StandardCharsets.UTF_8);
        System.out.println("Location requested: " + location);

        // Generate Redis keys
        String keyTemp = "weather:" + location + ":temperature";
        String keyCond = "weather:" + location + ":condition";

        // Fetch data from Redis
        String temperature = (String) redisTemplate.opsForHash().get("weather", keyTemp);
        String condition = (String) redisTemplate.opsForHash().get("weather", keyCond);

        // Logging fetched data
        System.out.println("Generated Redis key for temperature: " + keyTemp);
        System.out.println("Generated Redis key for condition: " + keyCond);
        System.out.println("Fetched temperature: " + temperature);
        System.out.println("Fetched condition: " + condition);


        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("location", location);
        response.put("temperature", temperature);
        response.put("condition", condition);
        response.put("hostname", env.getProperty("hostname"));

        if (temperature != null && condition != null) {
            response.put("weatherDataAvailable", true);
            response.put("temperature", temperature);
            response.put("condition", condition);
        } else {
            response.put("weatherDataAvailable", false);
            System.out.println("Weather data not available for location: " + location);
        }

        return response;
    }

    @GetMapping("/")
    public String hello() {
        return "Hello " + target + "!";
    }
}


