package org.onosproject.simulationw;


import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.topology.Topology;
import org.osgi.service.component.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(immediate = true)
public class SimulationManager {

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LinkService linkService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected SatelliteTopologyService satelliteTopologyService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected SatelliteConstellationService satelliteConstellationService;

    @Activate
    public void activate() {

        List<Link> links = new ArrayList<>();
        List<Device> devices = new ArrayList<>();
        linkService.getLinks().forEach(links::add);
        deviceService.getDevices().forEach(devices::add);
        Map<DeviceId, Map<String, Integer>> satelliteNodeParas = satelliteConstellationService.satelliteNodePara();
        Topology em_looc_Topology = satelliteTopologyService.em_looc(100.0, satelliteNodeParas, links, devices);

        Integer[] EIZ = {30, -30};
        Topology elb_looc_Topology = satelliteTopologyService.elb_looc(2.5, EIZ, satelliteNodeParas, links, devices);

        Topology ecb_looc_Topology = satelliteTopologyService.ecb_looc(1.0, EIZ, satelliteNodeParas, links, devices);
    }

    @Deactivate
    public void deactivate() {
    }
}
