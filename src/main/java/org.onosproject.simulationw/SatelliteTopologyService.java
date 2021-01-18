package org.onosproject.simulationw;

import org.onosproject.net.DeviceId;
import org.onosproject.net.topology.Topology;

import java.util.Map;

public interface SatelliteTopologyService {

    Topology em_looc(Double d_max, Map<DeviceId, Map<String, Integer>> satelliteNodeParas);

    Topology elb_looc(Double o_max, Integer[] EIZ, Map<DeviceId, Map<String, Integer>> satelliteNodeParas);

    Topology ecb_looc(Double c_j, Integer[] EIZ, Map<DeviceId, Map<String, Integer>> satelliteNodeParas);
}
