package org.zerock.week1.calculator;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "inputController", urlPatterns = "/calculator/input")
public class InputController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("InputController...doGet...");
        RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/calculator/input.jsp");
        dispatcher.forward(req,resp);
    }
}
