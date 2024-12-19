package de.procceed.cloud.roadtocloudnative.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private RestTemplate restTemplate;

    private final String defaultLocation = "Nürnberg";

    @Value("${TARGET:World}")
    private String target;

    @Value("${external.weather.api.url:https://api.weather.com/data}")
    private String externalApiUrl;

    @GetMapping("weather/debug")
    public Map<String, Object> debugWeather(@RequestParam(name = "location") Optional<String> optLocation) {
        RedisConnection connection = redisConnectionFactory.getConnection();
        String location = optLocation.orElse("Berlin");
        byte[] tempKey = ("weather:" + location + ":temperature").getBytes();
        byte[] condKey = ("weather:" + location + ":condition").getBytes();

        String temperature = connection.get(tempKey) != null ? new String(connection.get(tempKey)) : null;
        String condition = connection.get(condKey) != null ? new String(connection.get(condKey)) : null;

        Map<String, Object> response = new HashMap<>();
        response.put("temperature", temperature);
        response.put("condition", condition);

        System.out.println("Direct Redis Debug: " + response);
        return response;
    }

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

        if (temperature == null || condition == null) {
            // Fetch from external API if data is not in Redis
            String apiUrl = String.format("%s?location=%s", externalApiUrl, location);
            Map<String, String> externalData = restTemplate.getForObject(apiUrl, Map.class);

            if (externalData != null) {
                temperature = externalData.get("temperature");
                condition = externalData.get("condition");

                // Store data in Redis for future requests
                redisTemplate.opsForHash().put("weather", keyTemp, temperature);
                redisTemplate.opsForHash().put("weather", keyCond, condition);
            }
        }

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("location", location);
        response.put("temperature", temperature);
        response.put("condition", condition);
        response.put("hostname", env.getProperty("hostname"));
        response.put("weatherDataAvailable", temperature != null && condition != null);

        if (temperature == null || condition == null) {
            System.out.println("Weather data not available for location: " + location);
        }

        return response;
    }

    @GetMapping("/")
    public String hello() {
        return "Hello " + target + "!";
    }
}
