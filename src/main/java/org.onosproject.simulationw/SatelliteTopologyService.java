package org.onosproject.simulationw;

import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.topology.Topology;

import java.util.List;
import java.util.Map;

public interface SatelliteTopologyService {

    Topology em_looc(Integer[] EIZ, Double d_max, Map<DeviceId, Map<String, Integer>> satelliteNodeParas, List<Link> links, List<Device> devices);

    Topology elb_looc(Double o_max, Integer[] EIZ, Map<DeviceId, Map<String, Integer>> satelliteNodeParas, List<Link> links, List<Device> devices);

    Topology ecb_looc(Double c_j, Integer[] EIZ, Map<DeviceId, Map<String, Integer>> satelliteNodeParas, List<Link> links, List<Device> devices);
}
