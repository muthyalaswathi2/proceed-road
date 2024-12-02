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

@Controller
public class WeatherController {
    private final String defaultLocation = "Nürberg";

    @Component
public class RedisDataLoader implements CommandLineRunner {
    
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private Environment env;

  @PostConstruct
    public void loadData() {
        redisTemplate.opsForHash().put("weather", "Nürnberg:temperature", "25.0");
        redisTemplate.opsForHash().put("weather", "Nürnberg:condition", "cloudless");
        redisTemplate.opsForHash().put("weather", "Fürth:temperature", "-5.3");
        redisTemplate.opsForHash().put("weather", "Fürth:condition", "rainy");
    }
}

    @Value("${TARGET:World}")
    String target;

    );

    @GetMapping("v1/weather")
    public String getWeather(Model model, @RequestParam(name = "location") Optional<String> optLocation) {

        String location = optLocation.orElse(defaultLocation);
        
        String temperature = redisTemplate.opsForHash().get("weather", location + ":temperature");
        String condition = redisTemplate.opsForHash().get("weather", location + ":condition");

        if (temperature != null && condition != null) {
            model.addAttribute("weatherDataAvailable", true);
            model.addAttribute("weatherData", weatherDataMap.get(location));
        } else {
            model.addAttribute("weatherDataAvailable", false);
        }

        model.addAttribute("location", location);
        model.addAttribute("hostname", env.getProperty("hostname"));

        return "main";
    }

    @GetMapping("/")
    String hello() {
        return "Hello " + target + "!";
    }

}
