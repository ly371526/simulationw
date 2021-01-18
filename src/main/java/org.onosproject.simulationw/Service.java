package org.onosproject.simulationw;

import org.onosproject.net.DeviceId;
import org.onosproject.net.Path;

public interface Service {

    DeviceId srcNode();

    DeviceId dstNode();

    Double serviceBandwidth();

    Path servicePath();

}
