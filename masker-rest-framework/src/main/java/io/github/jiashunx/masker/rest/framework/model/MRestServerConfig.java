package io.github.jiashunx.masker.rest.framework.model;

import java.util.List;

/**
 * @author jiashunx
 */
public class MRestServerConfig {

    private int serverPort;
    private String serverName;
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

    public List<String> getClasspathResources() {
        return classpathResources;
    }

    public void setClasspathResources(List<String> classpathResources) {
        this.classpathResources = classpathResources;
    }
}
