package com.github.dagi.cynicalsoftware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Servlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(Servlet.class);
    private final Backend backend;

    public Servlet(Backend backend) {
        this.backend = backend;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        int responseStatus = -1;
        try {
            responseStatus = backend.doSomething();
            response.setStatus(responseStatus);
        } finally {
            log.info("{} status={} time={}", request.getRequestURI(),responseStatus , System.currentTimeMillis() - start);
        }

    }
}
