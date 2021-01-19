package org.onosproject.simulationw;

import org.onosproject.net.DeviceId;
import org.onosproject.net.Path;

public interface Service {

    DeviceId srcNode();

    DeviceId dstNode();

    Double serviceBandwidth();

    Path servicePath();

    Double delay();

    void setSrcNode(DeviceId srcNode);

    void setDstNode(DeviceId dstNode);

    void setServiceBandwidth(Double serviceBandwidth);

    void setServicePath(Path servicePath);

    void setDelay(Double delay);
}
