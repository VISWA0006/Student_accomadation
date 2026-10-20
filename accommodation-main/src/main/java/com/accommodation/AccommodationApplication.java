package com.accommodation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Main entry point for the Student Accommodation Management System.
 * 
 * College Mini-Project Architecture:
 * - Spring Boot: Simplifies Spring application setup with convention over configuration.
 * - Spring MVC: Model-View-Controller architecture for Thymeleaf web views and REST APIs.
 * - Spring Data JPA & Hibernate: Object-Relational Mapping (ORM) for PostgreSQL.
 * - Spring AOP: Aspect-Oriented Programming for method logging.
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class AccommodationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccommodationApplication.class, args);
    }
}
