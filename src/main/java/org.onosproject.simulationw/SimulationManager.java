package org.onosproject.simulationw;


import org.onosproject.net.DeviceId;
import org.onosproject.net.topology.Topology;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.Map;

@Component(immediate = true)
public class SimulationManager {

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected SatelliteTopologyService satelliteTopologyService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected SatelliteConstellationService satelliteConstellationService;

    @Activate
    public void activate() {

        Map<DeviceId, Map<String, Integer>> satelliteNodeParas = satelliteConstellationService.satelliteNodePara();
        Topology em_looc_Topology = satelliteTopologyService.em_looc(100.0, satelliteNodeParas);

        Integer[] EIZ = {30, -30};
        Topology elb_looc_Topology = satelliteTopologyService.elb_looc(1.5, EIZ, satelliteNodeParas);

        Topology ecb_looc_Topology = satelliteTopologyService.ecb_looc(1.0, EIZ, satelliteNodeParas);
    }
}
