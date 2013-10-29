package com.github.dagi.cynicalsoftware;

public interface BackendMBean {
   public void setThinkTime(long thinkTime);
   public void setUseHystrix(boolean useHystrix);
   public void setSimulateError(boolean simulateError);
}
