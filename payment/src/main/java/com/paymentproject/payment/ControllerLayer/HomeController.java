package com.paymentproject.payment.ControllerLayer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home Controller
 *
 * Serves static HTML pages. Uses @Controller (not @RestController) because
 * the return values are view names / forward directives, not JSON response
 * bodies.
 *
 * --- Bug fixed: Redis side-effect removed from page-serving method ---
 * The original home() method called redisTemplate.opsForValue().set("testKey",
 * "Hello Redis!") every time a user loaded the root page. This was a leftover
 * test. Issues with the original approach:
 *   1. It performs a network call to Redis on every page load, adding latency.
 *   2. It writes a meaningless key to Redis in production.
 *   3. It injects RedisTemplate into a controller, which breaks separation of
 *      concerns — Redis is a data-layer concern, not a web-layer concern.
 *   4. If Redis is unavailable, the home page throws an exception.
 *
 * Redis connectivity should be verified via a health endpoint
 * (Spring Boot Actuator provides /actuator/health out of the box) or in an
 * ApplicationReadyEvent listener at startup, not on every HTTP request.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/index.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }
}
