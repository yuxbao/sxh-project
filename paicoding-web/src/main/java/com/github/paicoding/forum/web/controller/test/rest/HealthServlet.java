package com.github.paicoding.forum.web.controller.test.rest;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 校验服务健康情况的servlet
 * localhost:8081/check 返回ok
 * @author XuYifei
 * @date 2024-07-12
 */
@WebServlet(urlPatterns = "/check")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter writer = resp.getWriter();
        writer.write("ok");
        writer.flush();
        writer.close();
    }
}
