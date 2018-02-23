package cd.go.contrib.elasticagents.docker.models;

import com.google.gson.annotations.Expose;

import java.util.List;

public class StatusReport {

    @Expose
    private final Integer cpus;
    @Expose
    private final String memory;
    @Expose
    private final String os;
    @Expose
    private final String architecture;
    @Expose
    private final String dockerVersion;
    @Expose
    private final List<ContainerStatusReport> containerStatusReports;

    public StatusReport(String os, String architecture, String dockerVersion, Integer cpus, String memory,
                        List<ContainerStatusReport> containerStatusReports) {
        this.os = os;
        this.architecture = architecture;
        this.dockerVersion = dockerVersion;
        this.cpus = cpus;
        this.memory = memory;
        this.containerStatusReports = containerStatusReports;
    }

    public Integer getCpus() {
        return cpus;
    }

    public String getMemory() {
        return memory;
    }

    public String getOs() {
        return os;
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getDockerVersion() {
        return dockerVersion;
    }

    public List<ContainerStatusReport> getContainerStatusReports() {
        return containerStatusReports;
    }
}
