package com.silvercare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan // ensure @WebServlet annotated servlets are discovered
@SpringBootApplication

public class SilverCareApplication {
    public static void main(String[] args) {
        SpringApplication.run(SilverCareApplication.class, args);
    }
}