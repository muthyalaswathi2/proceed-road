package de.procceed.cloud.roadtocloudnative.controller;

import de.procceed.cloud.roadtocloudnative.model.WeatherData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;

import redis.clients.jedis.Jedis;

@Controller
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private Environment env;

    private final String defaultLocation = "Nürnberg";

    @Value("${TARGET:World}")
    String target;

    @GetMapping("v1/weather")
    public String getWeather(Model model, @RequestParam(name = "location") Optional<String> optLocation) {

        String location = optLocation.orElse(defaultLocation);

        // Fetch temperature and condition from Redis
        String temperature = redisTemplate.opsForHash().get("weather", location + ":temperature");
        String condition = redisTemplate.opsForHash().get("weather", location + ":condition");

        // If weather data is available, display it
        if (temperature != null && condition != null) {
            model.addAttribute("weatherDataAvailable", true);
            model.addAttribute("temperature", temperature);
            model.addAttribute("condition", condition);
        } else {
            model.addAttribute("weatherDataAvailable", false);
        }

        model.addAttribute("location", location);
        model.addAttribute("hostname", env.getProperty("hostname"));

        return "main";
    }

    @GetMapping("/")
    public String hello() {
        return "Hello " + target + "!";
    }
}
    

