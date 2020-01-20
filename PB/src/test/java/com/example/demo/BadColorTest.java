package com.example.demo;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {DemoApplication.class})
@WebAppConfiguration
@IntegrationTest
public class BadColorTest {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;


    @Test
    public void testHappyPath() {

        // Start process instance
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("fullName", "John Doe");
        variables.put("email", "john.doe@activiti.com");
        variables.put("numOfPrints", "100");
        variables.put("color", "2");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

        // First,'wykonywalnosc' should be active
        Task wy_task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();

        // Completion with success should trigger new task
        Map<String, Object> taskVariables = new HashMap<String, Object>();
        taskVariables.put("Wykonywalny", false);
        taskService.complete(wy_task.getId(), taskVariables);
        System.out.println("proces 1.1 wykonany, user przypisany, projekt wykonywalny");

        //new user task- send email
        Task m_task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();
        Map<String, Object> task2Variables = new HashMap<String, Object>();

        Context context = new Context();
        context.setVariable("header", "Status zamówienia");
        context.setVariable("title", "Twoje zamówienie zostało odrzucone");
        context.setVariable("description", "Niestety twoje zamówienie jest niewykonywalne.");
        String body = templateEngine.process("odmowa", context);
        emailSender.sendEmail("jkloc96@gmail.com", "4Dzban zamówienie", body);

        taskService.complete(m_task.getId(), taskVariables);

        Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());

    }

}
