package io.github.jiashunx.masker.rest.framework.model;

import java.util.List;

/**
 * @author jiashunx
 */
public class MRestServerConfig {

    private int serverPort;
    private String serverName;
    private int bossThreadNum;
    private int workerThreadNum;
    private boolean connectionKeepAlive;
    private int httpContentMaxMBSize;
    private List<String> classpathResources;

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getBossThreadNum() {
        return bossThreadNum;
    }

    public void setBossThreadNum(int bossThreadNum) {
        this.bossThreadNum = bossThreadNum;
    }

    public int getWorkerThreadNum() {
        return workerThreadNum;
    }

    public void setWorkerThreadNum(int workerThreadNum) {
        this.workerThreadNum = workerThreadNum;
    }

    public boolean isConnectionKeepAlive() {
        return connectionKeepAlive;
    }

    public void setConnectionKeepAlive(boolean connectionKeepAlive) {
        this.connectionKeepAlive = connectionKeepAlive;
    }

    public int getHttpContentMaxMBSize() {
        return httpContentMaxMBSize;
    }

    public void setHttpContentMaxMBSize(int httpContentMaxMBSize) {
        this.httpContentMaxMBSize = httpContentMaxMBSize;
    }

    public List<String> getClasspathResources() {
        return classpathResources;
    }

    public void setClasspathResources(List<String> classpathResources) {
        this.classpathResources = classpathResources;
    }
}
