package com.github.dagi.cynicalsoftware;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class CynicalDemoServer {


    public static void main(String[] args) throws Exception {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(10);
        threadPool.setMaxThreads(10);
        Server server = new Server(threadPool);
        ServerConnector connector=new ServerConnector(server);
        connector.setPort(8080);

        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(new ServletHolder(new Servlet(new Backend("BackendA", 500))), "/feature-a");
        handler.addServletWithMapping(new ServletHolder(new Servlet(new Backend("BackendB", 500))), "/feature-b");
        handler.addServletWithMapping(HystrixMetricsStreamServlet.class, "/hystrix.stream");

        server.setHandler(handler);
        server.setConnectors(new Connector[]{connector});

        server.start();
        server.join();
    }

    private void xx() throws IOException {
        try(FileInputStream f = new FileInputStream(new File("/mnt/..."))) {
            f.read();
        }

    }
}
