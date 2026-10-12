package io.github.jiashunx.masker.rest.framework.model;

import java.util.ArrayList;
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
    private int multipartMaxFileCount;
    private int httpIdleTimeoutSeconds;
    private boolean fingerprintHeadersEnabled;
    private List<String> classpathResources;

    public MRestServerConfig() {}

    /**
     * 拷贝构造, 用于对外返回默认配置副本, 避免调用方修改全局默认配置.
     * @param other 源配置
     */
    public MRestServerConfig(MRestServerConfig other) {
        this.serverPort = other.serverPort;
        this.serverName = other.serverName;
        this.bossThreadNum = other.bossThreadNum;
        this.workerThreadNum = other.workerThreadNum;
        this.connectionKeepAlive = other.connectionKeepAlive;
        this.httpContentMaxMBSize = other.httpContentMaxMBSize;
        this.multipartMaxFileCount = other.multipartMaxFileCount;
        this.httpIdleTimeoutSeconds = other.httpIdleTimeoutSeconds;
        this.fingerprintHeadersEnabled = other.fingerprintHeadersEnabled;
        this.classpathResources = other.classpathResources == null ? null : new ArrayList<>(other.classpathResources);
    }

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

    public int getMultipartMaxFileCount() {
        return multipartMaxFileCount;
    }

    public void setMultipartMaxFileCount(int multipartMaxFileCount) {
        this.multipartMaxFileCount = multipartMaxFileCount;
    }

    public int getHttpIdleTimeoutSeconds() {
        return httpIdleTimeoutSeconds;
    }

    public void setHttpIdleTimeoutSeconds(int httpIdleTimeoutSeconds) {
        this.httpIdleTimeoutSeconds = httpIdleTimeoutSeconds;
    }

    public boolean isFingerprintHeadersEnabled() {
        return fingerprintHeadersEnabled;
    }

    public void setFingerprintHeadersEnabled(boolean fingerprintHeadersEnabled) {
        this.fingerprintHeadersEnabled = fingerprintHeadersEnabled;
    }

    public List<String> getClasspathResources() {
        return classpathResources;
    }

    public void setClasspathResources(List<String> classpathResources) {
        this.classpathResources = classpathResources;
    }
}
