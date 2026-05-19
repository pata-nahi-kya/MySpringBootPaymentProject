package com.paymentproject.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main Application Class for the Payment System
 * 
 * This is the entry point of the Spring Boot payment application which
 * provides:
 * - User authentication and authorization using JWT
 * - Money transfer functionality between users
 * - Admin panel for user management
 * - Secure REST API endpoints
 * 
 * The application uses:
 * - Spring Security for authentication and authorization
 * - JPA/Hibernate for database operations
 * - JWT for stateless authentication
 * - DTO pattern for data transfer
 * 
 * @SpringBootApplication annotation includes:
 *                        - @Configuration: Tags the class as a source of bean
 *                        definitions
 *                        - @EnableAutoConfiguration: Adds beans based on
 *                        classpath settings
 *                        - @ComponentScan: Scans for other components,
 *                        configurations, and services
 */
@SpringBootApplication
@EnableCaching
public class PaymentApplication {

	/**
	 * Main method which serves as the entry point for the application
	 * 
	 * @param args Command line arguments passed to the application
	 *             Starts the Spring Boot application context and embedded server
	 */
	public static void main(String[] args) {
		SpringApplication.run(PaymentApplication.class, args);
	}

}
