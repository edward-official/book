package org.zerock.week1.todo;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "todoRegisterController", urlPatterns = "/todo/register")
public class ToDoRegisterController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("set up the page of register");
        RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/todo/register.jsp");
        dispatcher.forward(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("processing the data and redirect to the list page");
        resp.sendRedirect("/todo/list");
    }
}
