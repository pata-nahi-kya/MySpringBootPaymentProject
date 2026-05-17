package com.paymentproject.payment.ControllerLayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home Controller
 * 
 * This controller handles static page routing for the application.
 * It serves:
 * - Login page at root ("/") and "/login" endpoints
 * - Allows unauthenticated access to login and registration pages
 * 
 * Note: @Controller is used instead of @RestController because we're returning
 * view names (static HTML), not JSON responses.
 */
@Controller
public class HomeController {

    @Autowired  
    RedisTemplate<String, Object> redisTemplate;


    /**
     * Serve login page as the home page
     * 
     * This endpoint serves index.html when user accesses the root path "/"
     * The .html extension is automatically appended by Spring's template resolution
     * 
     * @return "index" - Maps to src/main/resources/static/index.html
     */
    @GetMapping("/")
    public String home() {
        // save something in redis to test redis connection
        
        redisTemplate.opsForValue().set("testKey", "Hello Redis!");
        return "forward:/index.html";
        
    }

    /**
     * Serve login page at /login endpoint
     * 
     * This endpoint is required by Spring Security's login flow.
     * It forwards to the static login page in /static/index.html.
     * 
     * @return "forward:/index.html"
     */
    @GetMapping("/login")
    public String login() {
        return "forward:/index.html";
    }

    /**
     * Serve dashboard page
     * 
     * @return "forward:/dashboard.html" - Maps to
     *         src/main/resources/static/dashboard.html
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }

}
