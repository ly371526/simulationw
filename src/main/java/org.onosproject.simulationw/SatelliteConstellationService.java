package org.onosproject.simulationw;

import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;

import java.util.Map;

public interface SatelliteConstellationService {

    Map<DeviceId, Map<String, Integer>> satelliteNodePara();

    Map<String, Double> satelliteSRCSToGcs(DeviceId deviceId, Map<DeviceId, Map<String, Integer>> satellitesParaMap,
                                           Long timeDifference);

    Double linkDistance(Link link,Map<DeviceId, Map<String, Integer>> satelliteNodePara, Long timeDifference);
}
