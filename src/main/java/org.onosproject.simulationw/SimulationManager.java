package org.onosproject.simulationw;


import org.onosproject.common.DefaultTopology;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.topology.Topology;
import org.onosproject.net.topology.TopologyService;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component(immediate = true)
public class SimulationManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LinkService linkService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected TopologyService topologyService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected SatelliteTopologyService satelliteTopologyService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected SatelliteConstellationService satelliteConstellationService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ServiceGenerateService serviceGenerateService;

    private static final String EM_LOOC_TOPOLOGY = "EM_LOOC";
    private static final String ELB_LOOC_TOPOLOGY = "ELB_LOOC";
    private static final String ECB_LOOC_TOPOLOGY = "ECB_LOOC";
    private static final Long time = 100L;
    private static final Double lightSpeed = 300000.0;
    private static final Double laserLinkBandwidth = 10.0;
    private static final Integer servicesTotal = 1000;
    private static final Integer[] EIZ = {30, -30};

    @Activate
    public void activate() {

        log.info("Started");
        List<Link> links = new ArrayList<>();
        List<Device> devices = new ArrayList<>();
        linkService.getLinks().forEach(links::add);
        deviceService.getDevices().forEach(devices::add);

        Map<DeviceId, Map<String, Integer>> satelliteNodeParas = satelliteConstellationService.satelliteNodePara();
        Integer[][] EIZs = {{15, -15}, {20, -20}, {35, -35}, {40, -40}, {45, -45}};
        for (int i = 0; i < EIZs.length; i++) {
            Integer[] EIZ = EIZs[i];
            Map<String, Topology> topologies = new HashMap<>();
            log.info("计算EM_LOOC拓扑");
            Topology em_looc_Topology = satelliteTopologyService.em_looc(EIZ,100.0, satelliteNodeParas, links, devices);
            topologies.put(EM_LOOC_TOPOLOGY, em_looc_Topology);
            log.info("计算ELB_LOOC拓扑");
            Topology elb_looc_Topology = satelliteTopologyService.elb_looc(2.0, EIZ, satelliteNodeParas, links, devices);
            topologies.put(ELB_LOOC_TOPOLOGY, elb_looc_Topology);
            log.info("计算ECB_LOOC拓扑");
            Topology ecb_looc_Topology = satelliteTopologyService.ecb_looc(1.0, EIZ, satelliteNodeParas, links, devices);
            topologies.put(ECB_LOOC_TOPOLOGY, ecb_looc_Topology);

            log.info("生成业务");
            List<Service> services = serviceGenerateService.serviceGenerator(servicesTotal);

            log.info("测试业务时延");
            serviceDelay(services, topologies, satelliteNodeParas, EIZ);
            log.info("测试业务阻塞率");
            serviceBlockRate(services, topologies, links, EIZ);
        }


    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
    }

    public void serviceDelay(List<Service> services, Map<String, Topology> topologies,
                             Map<DeviceId, Map<String, Integer>> satelliteNodeParas, Integer[] EIZ) {

        topologies.keySet().forEach(topologyName -> {
            DefaultTopology topology = (DefaultTopology) topologies.get(topologyName);

            services.spliterator().forEachRemaining(service -> {
                DeviceId srcNode = service.srcNode();
                DeviceId dstNode = service.dstNode();

                Set<Path> paths = topology.getKShortestPaths(srcNode, dstNode, 1);
                List<Path> pathList = new ArrayList<>(paths);
                List<Double> distances = new ArrayList<>();
                for (int i = 0; i < pathList.size(); i++) {
                    Path path = pathList.get(i);
                    Double pathLength = pathDistance(path, satelliteNodeParas);
                    distances.add(pathLength);
                }
                Collections.sort(distances);
                Double minDelay = distances.get(0) / lightSpeed;
                service.setDelay(minDelay);
            });

            Double delaySum = 0.0;
            for (int i = 0; i < services.size(); i++) {
                delaySum = delaySum + services.get(i).delay();
            }
            Double serviceSizeD = (double) services.size();
            Double averageDelay = new BigDecimal(delaySum / serviceSizeD)
                    .setScale(5, RoundingMode.HALF_DOWN).doubleValue();
            String simulationResult = "EIZ=" + Arrays.toString(EIZ) + "拓扑方案：" + topologyName + " --> 时延：" + averageDelay.toString() + "\n";
            saveAsFileWriter(simulationResult);
            log.info(simulationResult);
        });
    }


    public void serviceBlockRate(List<Service> services, Map<String, Topology> topologies, List<Link> links, Integer[] EIZ) {

        topologies.keySet().forEach(topologyName -> {
            DefaultTopology topology = (DefaultTopology) topologies.get(topologyName);
            Map<Link, Double> linkBandwidthMap = initAllLinkBandwidth(links);
            List<Service> blockServices = new ArrayList<>();
            List<Service> successServices = new ArrayList<>();

            services.spliterator().forEachRemaining(service -> {

                DeviceId srcNode = service.srcNode();
                DeviceId dstNode = service.dstNode();
                Double requestBandwidth = service.serviceBandwidth();
                Set<Path> paths = topology.getKShortestPaths(srcNode, dstNode, 3);
                Path path = paths.iterator().next();
                List<Double> pathRemainBandwidth = new ArrayList<>();
                for (int j = 0; j < path.links().size(); j++) {
                    Link link = path.links().get(j);
                    pathRemainBandwidth.add(linkBandwidthMap.get(link));
                }
                Collections.sort(pathRemainBandwidth);
                Double pathMinRemainBandwidth = pathRemainBandwidth.get(0);
                if (requestBandwidth <= pathMinRemainBandwidth) {
                    successServices.add(service);
                    path.links().forEach(link -> {
                        Double remainBandwidth = linkBandwidthMap.get(link) - requestBandwidth;
                        linkBandwidthMap.replace(link, remainBandwidth);
                    });
                } else {
                    blockServices.add(service);
                }
            });

            Double blockServicesTotal = (double) blockServices.size();
            Double servicesTotal = (double) services.size();
            Double serviceBlockRate = new BigDecimal(blockServicesTotal / servicesTotal)
                    .setScale(5, RoundingMode.HALF_DOWN).doubleValue();
            String simulationResult = "EIZ=" + Arrays.toString(EIZ) + "拓扑方案：" + topologyName + " --> 阻塞率：" + serviceBlockRate.toString() + "\n";
            saveAsFileWriter(simulationResult);
            log.info(simulationResult);
        });
    }

    public Double pathDistance(Path path, Map<DeviceId, Map<String, Integer>> satelliteNodeParas) {
        List<Link> linkList = path.links();
        Double pathLength = 0.0;
        for (int j = 0; j < linkList.size(); j++) {
            Link link = linkList.get(j);
            Double distance = satelliteConstellationService.linkDistance(link, satelliteNodeParas, time);
            pathLength = pathLength + distance;
        }

        return pathLength;
    }

    public Map<Link, Double> initAllLinkBandwidth(List<Link> links) {

        Map<Link, Double> linkBandwidthMap = new HashMap<>();
        links.spliterator().forEachRemaining(link -> {
            linkBandwidthMap.put(link, laserLinkBandwidth);
        });

        return linkBandwidthMap;
    }

    private static void saveAsFileWriter(String content) {
        FileWriter fileWriter = null;
        try {
            // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
            fileWriter = new FileWriter("/home/yang/onos/apps/simulationw/src/main/resource/simulationResult.txt", true);
            fileWriter.write(content);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
