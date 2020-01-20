package com.example.demo;

import org.thymeleaf.TemplateEngine;

public interface EmailControllerImp {


    void EmailController(EmailSender emailSender, TemplateEngine templateEngine);
}
