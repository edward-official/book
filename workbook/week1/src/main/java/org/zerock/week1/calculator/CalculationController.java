package org.zerock.week1.calculator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "calculationController", urlPatterns = "/calculator/result")
public class CalculationController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String number1 = req.getParameter("number1");
        String number2 = req.getParameter("number2");

        System.out.println(String.format(" number1: %s", number1));
        System.out.println(String.format(" number2: %s", number2));
    }
}
