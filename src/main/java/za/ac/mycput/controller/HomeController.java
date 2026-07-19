/*
 * HomeController.java
 * Index/Home controller for the Property Management System.
 * Author: Jonathan Munday (230677797)
 * Date: 19 July 2026
 */
package za.ac.mycput.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String home() {
        return "Welcome to the Property Management System!";
    }
}
