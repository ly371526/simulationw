package org.onosproject.simulationw;

import org.onosproject.common.DefaultTopology;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.link.LinkStore;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.topology.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.math.BigDecimal;
import java.util.*;

@Component(immediate = true, service = SatelliteTopologyService.class)
public class SatelliteTopology implements SatelliteTopologyService {

    private static final String SATELLITE_INDEX = "satelliteIndex";
    private static final String SATELLITE_ORBIT_INDEX = "satelliteOrbitIndex";
    private static final String SATELLITE_ORBITS_TOTAL_NUMBER = "satelliteOrbitsTotalNumber";


    private static final Long time = 10L;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected TopologyService topologyService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected SatelliteConstellationService satelliteConstellationService;


    @Override
    public Topology em_looc(Double d_max, Map<DeviceId, Map<String, Integer>> satelliteNodeParas,
                            List<Link> links, List<Device> devices) {


        List<Link> linkList = new ArrayList<>();
        links.spliterator().forEachRemaining(link -> {
            Integer linkSrcNodeOrbitIndex = satelliteNodeParas.get(link.src().deviceId()).get(SATELLITE_ORBIT_INDEX);
            Integer linkDstNodeOrbitIndex = satelliteNodeParas.get(link.dst().deviceId()).get(SATELLITE_ORBIT_INDEX);
            if (!linkSrcNodeOrbitIndex.equals(linkDstNodeOrbitIndex)) {
                Double linkDistance = satelliteConstellationService.linkDistance(link, satelliteNodeParas, time);
                if (linkDistance <= d_max) {
                    linkList.add(link);
                }
            } else {
                linkList.add(link);
            }
        });

        Topology topology = buildTopology(devices, linkList);

        return topology;
    }

    @Override
    public Topology elb_looc(Double o_max, Integer[] EIZ, Map<DeviceId, Map<String, Integer>> satelliteNodeParas,
                             List<Link> links, List<Device> devices) {

        List<DeviceId> EIZSatelliteList = getEIZSatellite(EIZ, satelliteNodeParas);

        List<Link> L_INIT = new ArrayList<>();
        List<Link> L_ON = new ArrayList<>();
        links.spliterator().forEachRemaining(link -> {
            if (EIZSatelliteList.contains(link.src().deviceId()) && EIZSatelliteList.contains(link.dst().deviceId())) {
                Integer linkSrcNodeOrbitIndex = satelliteNodeParas.get(link.src().deviceId()).get(SATELLITE_ORBIT_INDEX);
                Integer linkDstNodeOrbitIndex = satelliteNodeParas.get(link.dst().deviceId()).get(SATELLITE_ORBIT_INDEX);
                if (!linkSrcNodeOrbitIndex.equals(linkDstNodeOrbitIndex)) {
                    L_INIT.add(link);
                } else {
                    L_ON.add(link);
                }
            } else {
                L_ON.add(link);
            }
        });

        List<Link> L_OFF = new ArrayList<>();
        int L_INITSize = L_INIT.size();
        List<Link> L_INITCopy = new ArrayList<>(L_INIT);
        A:
        for (int i = 0; i < L_INITSize; i++) {
            if (L_INIT.isEmpty()) {
                break A;
            }
            Link l_cand = l_cand(L_INIT, satelliteNodeParas);
            Link l_cand_reverse = getReverseLink(l_cand, L_INITCopy);
            List<Link> linksCopy = new ArrayList<>(links);
            linksCopy.remove(l_cand);
            linksCopy.remove(l_cand_reverse);
            Topology topology = buildTopology(devices, linksCopy);
            DeviceId src = l_cand.src().deviceId();
            DeviceId dst = l_cand.dst().deviceId();
            List<Path> DP = new ArrayList<>(defaultTopology(topology).getKShortestPaths(src, dst, 5));

            Map<Path, Double> dPathOverhead = new HashMap<>();
            List<Path> DPCopy = new ArrayList<>(DP);
            DPCopy.spliterator().forEachRemaining(path -> {
                List<Link> linkList = path.links();
                Double dpLength = 0.0;
                for (int j = 0; j < linkList.size(); j++) {
                    Link link = linkList.get(j);
                    Double distance = satelliteConstellationService.linkDistance(link, satelliteNodeParas, time);
                    dpLength = dpLength + distance;
                }
                Double longestLinkLength = satelliteConstellationService.linkDistance(l_cand, satelliteNodeParas, time);
                Double overhead = dpLength / longestLinkLength;
                dPathOverhead.put(path, overhead);
                if (overhead > o_max) {
                    DP.remove(path);
                }
            });

            for (int j = 0; j < DP.size(); j++) {
                Path path = DP.get(j);
                List<Link> pathLinks = path.links();
                B:
                for (int k = 0; k < pathLinks.size(); k++) {
                    Link link = pathLinks.get(k);
                    if (L_OFF.contains(link)) {
                        DP.remove(path);
                        break B;
                    }
                }
            }

            if (DP.isEmpty()) {
                L_ON.add(l_cand);
                L_ON.add(l_cand_reverse);
                L_INIT.remove(l_cand);
                L_INIT.remove(l_cand_reverse);
            } else if (DP.size() == 1  && L_ON.containsAll(DP.get(0).links())) {
                L_OFF.add(l_cand);
                L_OFF.add(l_cand_reverse);
                L_INIT.remove(l_cand);
                L_INIT.remove(l_cand_reverse);
            } else {
                List<Double> dpOverhead = new ArrayList<>();
                DP.forEach(path -> {
                    dpOverhead.add(dPathOverhead.get(path));
                });
                Collections.sort(dpOverhead);
                Path p_dt = null;
                C:
                for (int j = 0; j < DP.size(); j++) {
                    Path path = DP.get(j);
                    if (dPathOverhead.get(path).equals(dpOverhead.get(0))) {
                        p_dt = path;
                        break C;
                    }
                }

                for (int j = 0; j < p_dt.links().size(); j++) {
                    Link link = p_dt.links().get(j);
                    if (!L_ON.contains(link)) {
                        Link link_reverse = getReverseLink(link, L_INITCopy);
                        L_ON.add(link);
                        L_ON.add(link_reverse);
                        L_INIT.remove(link);
                        L_INIT.remove(link_reverse);
                    }
                }
                L_INIT.remove(l_cand);
                L_INIT.remove(l_cand_reverse);
                L_OFF.add(l_cand);
                L_OFF.add(l_cand_reverse);
            }

        }

        return buildTopology(devices, L_ON);
    }

    @Override
    public Topology ecb_looc(Double c_j, Integer[] EIZ, Map<DeviceId, Map<String, Integer>> satelliteNodeParas,
                             List<Link> links, List<Device> devices) {

        Integer N_orbit = satelliteNodeParas.get(satelliteNodeParas.keySet().iterator().next())
                .get(SATELLITE_ORBITS_TOTAL_NUMBER);

        List<Boolean> OOB_base = new ArrayList<>();
        for (int i = 0; i < N_orbit; i++) {
            OOB_base.add(false);
        }

        List<DeviceId> EIZSatelliteList = getEIZSatellite(EIZ, satelliteNodeParas);

        Map<Integer, List<DeviceId>> eachCircleSatelliteEIZ = new HashMap<>();
        EIZSatelliteList.forEach(deviceId -> {
            Integer satelliteIndex = satelliteNodeParas.get(deviceId).get(SATELLITE_INDEX);
            if (eachCircleSatelliteEIZ.keySet().contains(satelliteIndex)) {
                eachCircleSatelliteEIZ.get(satelliteIndex).add(deviceId);
            } else {
                List<DeviceId> orbitSatellite = new ArrayList<>();
                orbitSatellite.add(deviceId);
                eachCircleSatelliteEIZ.put(satelliteIndex, orbitSatellite);
            }
        });

        Integer n_e = eachCircleSatelliteEIZ.size();
        List<Integer> offsetList = new ArrayList<>();
        for (int i = 0; i < n_e; i++) {
            offsetList.add(0);
        }

        Long n_a = Math.round(c_j * N_orbit / 2);
        int average = Math.round((float) N_orbit / n_a);
        for (int i = 0; i < n_a; i++) {
            int position = average * i;
            if (position <= N_orbit) {
                OOB_base.add(position, true);
                OOB_base.remove(position + 1);
            } else {
                OOB_base.add(OOB_base.size() -1 , true);
                OOB_base.remove(OOB_base.size() - 1);
            }
        }

        Double averageOffset = new BigDecimal(N_orbit / n_a / n_e).setScale(2).doubleValue();

        if (averageOffset <= 1) {
            Collections.replaceAll(offsetList, 0, 1);
            offsetList.remove(0);
            offsetList.add(0, 0);
        } else {
            int offset = (int) Math.floor(Math.round((float) N_orbit / n_a) / (n_e - 1));
            int lastOffset = offset + (Math.round((float) N_orbit / n_a) - offset * (n_e - 1));
            Collections.replaceAll(offsetList, 0, offset);
            offsetList.remove(0);
            offsetList.add(0, 0);
            offsetList.remove(offsetList.size() - 1);
            offsetList.add(offsetList.size() - 1, lastOffset);
        }

        Map<Integer, List<Boolean>> OOBs = new HashMap<>();
        List<Boolean> OOB_next = new ArrayList<>(OOB_base);
        for (int i = 0; i < offsetList.size(); i++) {
            int offset = offsetList.get(i);
            List<Integer> keyList = new ArrayList<>(eachCircleSatelliteEIZ.keySet());
            Collections.sort(keyList);
            List<Boolean> OOB = new ArrayList<>();
            for (int j = 0; j < OOB_next.size(); j++) {
                Integer index = (j + offset) % OOB_next.size();
                if (index >= OOB.size()) {
                    OOB.add(OOB_next.get(j));
                } else {
                    OOB.add(index, OOB_next.get(j));
                }

            }
            OOBs.put(keyList.get(i), OOB);
            OOB_next = OOB;
        }

        List<Integer> keyList = new ArrayList<>(eachCircleSatelliteEIZ.keySet());
        for (int i = 0; i < eachCircleSatelliteEIZ.size(); i++) {
            List<DeviceId> circleSatellites = eachCircleSatelliteEIZ.get(keyList.get(i));
            Map<Integer, DeviceId> circleSatelliteMap = new HashMap<>();
            circleSatellites.forEach(deviceId -> {
                Integer satelliteOrbitIndex = satelliteNodeParas.get(deviceId).get(SATELLITE_ORBIT_INDEX);
                circleSatelliteMap.put(satelliteOrbitIndex - 1, deviceId);
            });
            List<DeviceId> circleSatelliteList = new ArrayList<>();
            List<Integer> keys = new ArrayList<>(circleSatelliteMap.keySet());
            Collections.sort(keys);
            keys.forEach(key -> {
                circleSatelliteList.add(circleSatelliteMap.get(key));
            });
            eachCircleSatelliteEIZ.replace(keyList.get(i), circleSatelliteList);
        }

        List<Link> EIZ_ISL = getEIZ_ISL(EIZSatelliteList, satelliteNodeParas, links);
        List<Link> L_ON = new ArrayList<>();
        List<Link> L_OFF = new ArrayList<>();
        links.spliterator().forEachRemaining(link -> {
            if (!EIZ_ISL.contains(link)) {
                L_ON.add(link);
            }
        });

        eachCircleSatelliteEIZ.keySet().forEach(circleIndex -> {
            List<DeviceId> circleSatellites = eachCircleSatelliteEIZ.get(circleIndex);
            List<Boolean> circleOOB = OOBs.get(circleIndex);
            for (int i = 0; i < circleSatellites.size(); i++) {
                int j = (i + 1) % circleSatellites.size();
                DeviceId linkEnd_1 = circleSatellites.get(i);
                DeviceId linkEnd_2 = circleSatellites.get(j);
                Boolean linkStatus = circleOOB.get(i);
                EIZ_ISL.forEach(link -> {
                    if ((link.src().deviceId().equals(linkEnd_1) && link.dst().deviceId().equals(linkEnd_2)) ||
                            (link.src().deviceId().equals(linkEnd_2) && link.dst().deviceId().equals(linkEnd_1))) {
                        if (linkStatus.equals(true)) {
                            L_ON.add(link);
                        } else {
                            L_OFF.add(link);
                        }
                    }
                });
            }
        });

        Topology topology = buildTopology(devices, L_ON);

        return topology;
    }
    /**
     * 构建拓扑
     * @param deviceList 节点列表
     * @param linkList 链路列表
     * @return 拓扑
     */
    public Topology buildTopology(List<Device> deviceList, List<Link> linkList) {
        GraphDescription graphDescription = new DefaultGraphDescription(
                System.nanoTime(),
                System.currentTimeMillis(),
                deviceList,
                linkList);
        Topology topology = new DefaultTopology(ProviderId.NONE, graphDescription);

        return topology;
    }

    public DefaultTopology defaultTopology(Topology topology) {
        return (DefaultTopology) topology;
    }

    public Link l_cand(List<Link> L_INIT, Map<DeviceId, Map<String, Integer>> satelliteNodeParas) {

        Link longestLink = L_INIT.get(0);
        Double maxDistance = satelliteConstellationService.linkDistance(longestLink, satelliteNodeParas, time);
        for (int i = 1; i < L_INIT.size(); i++) {
            Link link = L_INIT.get(i);
            Double distance = satelliteConstellationService.linkDistance(link, satelliteNodeParas, time);
            if (distance > maxDistance) {
                longestLink = link;
                maxDistance = distance;
            }
        }

        return longestLink;
    }

    public List<DeviceId> getEIZSatellite(Integer[] EIZ, Map<DeviceId, Map<String, Integer>> satelliteNodeParas) {

        Integer EIZ_up = EIZ[0];
        Integer EIZ_down = EIZ[1];
        List<DeviceId> EIZSatelliteList = new ArrayList<>();
        deviceService.getDevices().spliterator().forEachRemaining(device -> {
            Map<String, Double> satelliteNodeCoordinate = satelliteConstellationService.satelliteSRCSToGcs(device.id(),
                    satelliteNodeParas, time);
            Double satelliteLatitude = satelliteNodeCoordinate.get("latitude");
            if (satelliteLatitude <= EIZ_up && satelliteLatitude >= EIZ_down) {
                EIZSatelliteList.add(device.id());
            }
        });

        return EIZSatelliteList;
    }

    public List<Link> getEIZ_ISL(List<DeviceId> EIZSatelliteList, Map<DeviceId, Map<String, Integer>> satelliteNodeParas,
                                 List<Link> links) {

        List<Link> EIZ_ISL = new ArrayList<>();
        links.spliterator().forEachRemaining(link -> {
            if (EIZSatelliteList.contains(link.src().deviceId()) && EIZSatelliteList.contains(link.dst().deviceId())) {
                Integer linkSrcNodeOrbitIndex = satelliteNodeParas.get(link.src().deviceId()).get(SATELLITE_ORBIT_INDEX);
                Integer linkDstNodeOrbitIndex = satelliteNodeParas.get(link.dst().deviceId()).get(SATELLITE_ORBIT_INDEX);
                if (!linkSrcNodeOrbitIndex.equals(linkDstNodeOrbitIndex)) {
                    EIZ_ISL.add(link);
                }
            }
        });

        return EIZ_ISL;
    }

    public Link getReverseLink(Link link, List<Link> links) {

        Link reverseLink = null;
        for (int i = 0; i < links.size(); i++) {
            Link link1 = links.get(i);
            if (link1.src().deviceId().equals(link.dst().deviceId()) &&
            link1.dst().deviceId().equals(link.src().deviceId())) {
                reverseLink = link1;
                break;
            }
        }

        return reverseLink;
    }
}
