package com.github.dagi.cynicalsoftware;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletResponse;
import java.lang.management.ManagementFactory;

public class Backend implements BackendMBean {
    private static final Logger log = LoggerFactory.getLogger(Backend.class);

    private volatile long thinkTime;
    private final String backendName;
    private volatile boolean useHystrix = false;
    private volatile boolean simulateError = false;

    public Backend(String backendName, long thinkTime) {
        this.thinkTime = thinkTime;
        this.backendName = backendName;
        registerAsMBean(backendName);
    }

    private void registerAsMBean(String backendName) {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName name = new ObjectName("cynical.backend:type=" + backendName);
            mbs.registerMBean(this, name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setThinkTime(long thinkTime) {
        log.info("Backend '{}' has now think time {}", backendName, thinkTime);
        this.thinkTime = thinkTime;
    }

    public void setUseHystrix(boolean useHystrix) {
        log.info("Backend '{}' is {} Hystrix", backendName, (useHystrix) ? "using" : "not using");
        this.useHystrix = useHystrix;
    }

    public void setSimulateError(boolean simulateError) {
        log.info("Backend '{}' {} ", backendName, (simulateError) ? "simulates error " : "behaves correctly");
        this.simulateError = simulateError;
    }

    public int doSomething() {
        if(useHystrix) {
            return new BackendCallWrappedInHystrix(backendName, thinkTime, simulateError).execute();
        } else {
            return simulateBackendCall(thinkTime);
        }
    }


    static int simulateBackendCall(long thinkTime) {
        try {
            Thread.sleep(thinkTime);
            return HttpServletResponse.SC_NO_CONTENT;
        } catch (InterruptedException e) {
            return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        }
    }

    public static class BackendCallWrappedInHystrix extends HystrixCommand<Integer> {
        private static final Logger log = LoggerFactory.getLogger(BackendCallWrappedInHystrix.class);
        private final long thinkTime;
        private final String backendName;
        private final boolean simulatError;

        public BackendCallWrappedInHystrix(String backendName, long thinkTime, boolean simulatError) {
            super(HystrixCommand.Setter
                    .withGroupKey(HystrixCommandGroupKey.Factory.asKey(backendName))
                    .andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter()
                                    .withExecutionIsolationThreadTimeoutInMilliseconds(10000)
                                    .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD))

                    .andThreadPoolPropertiesDefaults(
                            HystrixThreadPoolProperties.Setter()
                                    .withQueueSizeRejectionThreshold(0)
                                    .withCoreSize(5))
                    .andCommandKey(HystrixCommandKey.Factory.asKey(backendName))
            );
            this.thinkTime = thinkTime;
            this.backendName = backendName;
            this.simulatError = simulatError;
        }

        @Override
        protected Integer run() {
            if(simulatError) {
                throw new RuntimeException("Opps something very bad happened");
            }
            return simulateBackendCall(thinkTime);
        }

        @Override
        protected Integer getFallback() {
            log.info("Using fallback for backend {}", backendName);
            return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        }
    }

}
