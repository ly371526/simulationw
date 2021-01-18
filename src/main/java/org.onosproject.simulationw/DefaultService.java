package org.onosproject.simulationw;

import org.onosproject.net.DeviceId;
import org.onosproject.net.Path;

public class DefaultService implements Service {

    private DeviceId srcNode;
    private DeviceId dstNode;
    private Double serviceBandwidth;
    private Path servicePath;

    DefaultService(DeviceId srcNode, DeviceId dstNode, Double serviceBandwidth, Path servicePath) {
        this.srcNode = srcNode;
        this.dstNode = dstNode;
        this.serviceBandwidth = serviceBandwidth;
        this.servicePath = servicePath;
    }

    @Override
    public DeviceId srcNode() {
        return srcNode;
    }

    @Override
    public DeviceId dstNode() {
        return dstNode;
    }


    @Override
    public Double serviceBandwidth() {
        return serviceBandwidth;
    }

    @Override
    public Path servicePath() {
        return servicePath;
    }

    public void setSrcNode(DeviceId srcNode) {
        this.srcNode = srcNode;
    }

    public void setDstNode(DeviceId dstNode) {
        this.dstNode = dstNode;
    }

    public void setServiceBandwidth(Double serviceBandwidth) {
        this.serviceBandwidth = serviceBandwidth;
    }

    public void setServicePath(Path servicePath) {
        this.servicePath = servicePath;
    }
}
