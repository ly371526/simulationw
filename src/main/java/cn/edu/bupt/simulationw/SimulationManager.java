package cn.edu.bupt.simulationw;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.YenKShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.util.*;

public class SimulationManager {

    private SimulationManager(){}

    SatelliteCoordinate satelliteCoordinate = new SatelliteCoordinate();
    private static final Long time = 10L;
    private static final Double lightSpeed = 300000.0;
    private static final Double laserLinkBandwidth = 20.0;
    private static final Integer servicesTotal = 10000;
    private static final Integer SATELLITE_ORBIT_HEIGHT = 540;
    private static final Integer SATELLITE_ORBIT_INCLINATION = 53;
    private static final Integer SATELLITES_TOTAL = 1584;
    private static final Integer SATELLITE_ORBITS_TOTAL = 66;
    private static final Integer SATELLITE_PHASE_FACTOR = 0;


    public static void main(String[] args) throws URISyntaxException {

        SimulationManager simulationManager = new SimulationManager();
        Graph<Node, Link> initTopology = createInitTopology();
        List<Node> satellites = new ArrayList<>(initTopology.vertexSet());
        List<Link> links = new ArrayList<>(initTopology.edgeSet());

        Integer[][] EIZs = {{5, -5}, {10, -10}, {15, -15}, {20, -20}, {25, -25}, {30, -30}, {35, -35}, {40, -40}, {45, -45}, {50, -50}};
        Double[] O_max = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0};
        Double[] C_j = {0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0};


        for (int i = 0; i < EIZs.length; i++) {
            Integer[] EIZ = EIZs[i];
            System.out.println("EIZ : " + Arrays.toString(EIZ));
            Map<String, Graph<Node, Link>> topologies = new HashMap<>();
            LOOCAlgorithm loocAlgorithm = new LOOCAlgorithm();
//            Graph<Node, Link> em_looc_Topology = loocAlgorithm.em_looc(EIZ,100.0, satellites, links);
//            topologies.put("EM_LOOC", em_looc_Topology);
//            Graph<Node, Link> elb_looc_Topology = loocAlgorithm.elb_looc(10.0, EIZ, satellites, links);
//            topologies.put("ELB_LOOC", elb_looc_Topology);
//            Graph<Node, Link> ecb_looc_Topology = loocAlgorithm.ecb_looc(1.0, EIZ, satellites, links);
//            topologies.put("ECB_LOOC", ecb_looc_Topology);
            topologies.put("初始拓扑", initTopology);

            topologies.keySet().forEach(topologyIndex -> {
                ServiceGenerator serviceGenerator = new ServiceGenerator();
                List<SatelliteService> services = serviceGenerator.serviceGenerator(servicesTotal, satellites, EIZ);
                Graph<Node, Link> topology = topologies.get(topologyIndex);
//                System.out.println("正在测试阻塞率 - " + topologyIndex + " - " + topology.edgeSet().size());
//                List<SatelliteService> successServices = simulationManager.serviceBlockRate(services, topology, EIZ);
//                System.out.println("正在测试传输时延 - " + topologyIndex + " - " + topology.edgeSet().size());
//                simulationManager.successServiceDelay(successServices, topology, EIZ);
                System.out.println("全网功耗 - " + topologyIndex + " - " + topology.edgeSet().size());
                simulationManager.energyConsumption(topology, EIZ);
            });
        }

//        for (int i = 0; i < O_max.length; i++) {
//            Double o_max = O_max[i];
//            Integer[] EIZ = {30, -30};
//            System.out.println("o_max = " + o_max.toString());
//            Map<String, Graph<Node, Link>> topologies = new HashMap<>();
//            LOOCAlgorithm loocAlgorithm = new LOOCAlgorithm();
////            Graph<Node, Link> em_looc_Topology = loocAlgorithm.em_looc(EIZ,100.0, satellites, links);
////            topologies.put("EM_LOOC", em_looc_Topology);
////            Graph<Node, Link> elb_looc_Topology = loocAlgorithm.elb_looc(o_max, EIZ, satellites, links);
////            topologies.put("ELB_LOOC", elb_looc_Topology);
////            Graph<Node, Link> ecb_looc_Topology = loocAlgorithm.ecb_looc(1.0, EIZ, satellites, links);
////            topologies.put("ECB_LOOC", ecb_looc_Topology);
//            topologies.put("初始拓扑", initTopology);
//
//            topologies.keySet().forEach(topologyIndex -> {
//                ServiceGenerator serviceGenerator = new ServiceGenerator();
//                List<SatelliteService> services = serviceGenerator.serviceGenerator(servicesTotal, satellites, EIZ);
//                Graph<Node, Link> topology = topologies.get(topologyIndex);
////                System.out.println("正在测试阻塞率 - " + topologyIndex + " - " + topology.edgeSet().size());
////                List<SatelliteService> successServices = simulationManager.serviceBlockRate(services, topology, EIZ);
////                System.out.println("正在测试传输时延 - " + topologyIndex + " - " + topology.edgeSet().size());
////                simulationManager.successServiceDelay(successServices, topology, EIZ);
//                System.out.println("全网功耗 - " + topologyIndex + " - " + topology.edgeSet().size());
//                simulationManager.energyConsumption(topology, EIZ);
//            });
//        }
//
//        for (int i = 0; i < C_j.length; i++) {
//            Double c_j = C_j[i];
//            Integer[] EIZ = {30, -30};
//            Double o_max = 10.0;
//            System.out.println("c_j = " + c_j.toString());
//            Map<String, Graph<Node, Link>> topologies = new HashMap<>();
//            LOOCAlgorithm loocAlgorithm = new LOOCAlgorithm();
////            Graph<Node, Link> em_looc_Topology = loocAlgorithm.em_looc(EIZ,100.0, satellites, links);
////            topologies.put("EM_LOOC", em_looc_Topology);
////            Graph<Node, Link> elb_looc_Topology = loocAlgorithm.elb_looc(o_max, EIZ, satellites, links);
////            topologies.put("ELB_LOOC", elb_looc_Topology);
////            Graph<Node, Link> ecb_looc_Topology = loocAlgorithm.ecb_looc(c_j, EIZ, satellites, links);
////            topologies.put("ECB_LOOC", ecb_looc_Topology);
//            topologies.put("初始拓扑", initTopology);
//
//            topologies.keySet().forEach(topologyIndex -> {
//                ServiceGenerator serviceGenerator = new ServiceGenerator();
//                List<SatelliteService> services = serviceGenerator.serviceGenerator(servicesTotal, satellites, EIZ);
//                Graph<Node, Link> topology = topologies.get(topologyIndex);
////                System.out.println("正在测试阻塞率 - " + topologyIndex + " - " + topology.edgeSet().size());
////                List<SatelliteService> successServices = simulationManager.serviceBlockRate(services, topology, EIZ);
////                System.out.println("正在测试传输时延 - " + topologyIndex + " - " + topology.edgeSet().size());
////                simulationManager.successServiceDelay(successServices, topology, EIZ);
//                System.out.println("全网功耗 - " + topologyIndex + " - " + topology.edgeSet().size());
//                simulationManager.energyConsumption(topology, EIZ);
//            });
//        }




    }

    public void successServiceDelay(List<SatelliteService> successServices, Graph<Node, Link> topology, Integer[] EIZ) {

        LOOCAlgorithm loocAlgorithm = new LOOCAlgorithm();
        YenKShortestPath<Node, Link> yKSPAlg = new YenKShortestPath<>(topology);
        List<Node> satellites = new ArrayList<>(topology.vertexSet());
        List<Node> EIZSatelliteList = loocAlgorithm.getEIZSatellite(EIZ, satellites);

        List<Double> pathDelays = new ArrayList<>();
        for (int i = 0; i < successServices.size(); i++) {
            SatelliteService satelliteService = successServices.get(i);
            if (EIZSatelliteList.contains(satelliteService.getSrcNode())
                    && EIZSatelliteList.contains(satelliteService.getDstNode())) {
                Double pathDistance = pathDistance(satelliteService.getServicePath());
                Double pathDelay = pathDistance / lightSpeed;
                pathDelays.add(pathDelay);
            }
        }

        Double pathDelaysSum = 0.0;
        for (int i = 0; i < pathDelays.size(); i++) {
            pathDelaysSum = pathDelaysSum + pathDelays.get(i);
        }
        Double averageDelay = pathDelaysSum / (double) pathDelays.size() * 1000;
        String simulationResult = "EIZ=" + Arrays.toString(EIZ) + " --> 时延：" + averageDelay.toString() + " ms";
//        saveAsFileWriter(simulationResult + "\n");
        System.out.println(simulationResult);
    }

    public void serviceDelay(List<SatelliteService> services, Graph<Node, Link> topology, Integer[] EIZ) {

        LOOCAlgorithm loocAlgorithm = new LOOCAlgorithm();
        YenKShortestPath<Node, Link> yKSPAlg = new YenKShortestPath<>(topology);
        List<Node> satellites = new ArrayList<>(topology.vertexSet());
        List<Node> EIZSatelliteList = loocAlgorithm.getEIZSatellite(EIZ, satellites);

        services.spliterator().forEachRemaining(service -> {
            Node srcNode = service.getSrcNode();
            Node dstNode = service.getDstNode();

            List<GraphPath<Node, Link>> pathList = yKSPAlg.getPaths(srcNode, dstNode, 8);
            List<Double> distances = new ArrayList<>();
            for (int i = 0; i < pathList.size(); i++) {
                GraphPath<Node, Link> path = pathList.get(i);
                Double pathLength = pathDistance(path);
                distances.add(pathLength);
            }
            Collections.sort(distances);
            Double minDelay = distances.get(0) / lightSpeed;
            service.setDelay(minDelay);
        });

        Integer EIZServiceTotal = 0;
        Double delaySum = 0.0;
        for (int i = 0; i < services.size(); i++) {
            if (EIZSatelliteList.contains(services.get(i).getSrcNode()) || EIZSatelliteList.contains(services.get(i).getDstNode())) {
                delaySum = delaySum + services.get(i).getDelay();
                EIZServiceTotal++;
            }
        }
        Double serviceSizeD = (double) EIZServiceTotal;
        Double averageDelay = new BigDecimal(delaySum / serviceSizeD)
                .setScale(5, RoundingMode.HALF_DOWN).doubleValue();
        String simulationResult = "EIZ=" + Arrays.toString(EIZ) + " --> 时延：" + averageDelay.toString();
//        saveAsFileWriter(simulationResult + "\n");
        System.out.println(simulationResult);

    }


    public List<SatelliteService> serviceBlockRate(List<SatelliteService> services, Graph<Node, Link> topology, Integer[] EIZ) {

        YenKShortestPath<Node, Link> yKSPAlg = new YenKShortestPath<>(topology);

        Set<Link> links = topology.edgeSet();
        Map<Link, Double> linkBandwidthMap = initAllLinkBandwidth(links);
        List<SatelliteService> blockServices = new ArrayList<>();
        List<SatelliteService> successServices = new ArrayList<>();

        services.spliterator().forEachRemaining(service -> {

            Node srcNode = service.getSrcNode();
            Node dstNode = service.getDstNode();
            Double requestBandwidth = service.getServiceBandwidth();
            List<GraphPath<Node, Link>> paths = yKSPAlg.getPaths(srcNode, dstNode, 10);

            for (int i = 0; i < paths.size(); i++) {
                GraphPath<Node, Link> path = paths.get(i);
                List<Double> pathRemainBandwidth = new ArrayList<>();
                for (int j = 0; j < path.getEdgeList().size(); j++) {
                    Link link = path.getEdgeList().get(j);
                    Double linkRemainBandwidth = linkBandwidthMap.get(link);
                    pathRemainBandwidth.add(linkRemainBandwidth);
                }

                Double pathMinRemainBandwidth = Collections.min(pathRemainBandwidth);
                if (requestBandwidth <= pathMinRemainBandwidth) {
                    successServices.add(service);
                    path.getEdgeList().forEach(link -> {
                        Double remainBandwidth = linkBandwidthMap.get(link) - requestBandwidth;
                        linkBandwidthMap.replace(link, remainBandwidth);
                    });
                    service.setServicePath(path);
                    break;
                }
            }

            if (!successServices.contains(service)) {
                blockServices.add(service);
            }

        });

        Double remainingBandwidthSum = 0.0;
        List<Link> links1 = new ArrayList<>(linkBandwidthMap.keySet());
        for (int i = 0; i < links1.size(); i++) {
            remainingBandwidthSum = remainingBandwidthSum + linkBandwidthMap.get(links1.get(i));
        }

        Double bandwidthUtilization = (1 - (remainingBandwidthSum / (links.size() * laserLinkBandwidth))) * 100;

        Double blockServicesTotal = (double) blockServices.size();
        Double servicesTotal = (double) services.size();
        Double serviceBlockRate = new BigDecimal(blockServicesTotal / servicesTotal * 100)
                .setScale(5, RoundingMode.HALF_DOWN).doubleValue();
        String simulationResult = "EIZ=" + Arrays.toString(EIZ) + " --> 阻塞率：" + serviceBlockRate.toString() + " %"
                + " --> 利用率：" + bandwidthUtilization.toString() + " %";
//        saveAsFileWriter(simulationResult + "\n");
        System.out.println(simulationResult);

        return successServices;
//        Double delaySum = 0.0;
//        for (int i = 0; i < successServices.size(); i++) {
//            GraphPath<Node, Link> path = successServices.get(i).getServicePath();
//            Double distance = pathDistance(path);
//            delaySum = delaySum + distance / lightSpeed;
//        }
//        Double serviceSizeD = (double) successServices.size();
//        Double averageDelay = new BigDecimal(delaySum / serviceSizeD)
//                .setScale(5, RoundingMode.HALF_DOWN).doubleValue();
//        String simulationDelayResult = "EIZ=" + Arrays.toString(EIZ) + " --> 时延：" + averageDelay.toString();
////        saveAsFileWriter(simulationResult + "\n");
//        System.out.println(simulationDelayResult);

    }

    public void energyConsumption(Graph<Node, Link> topology, Integer[] EIZ) {

        LOOCAlgorithm loocAlgorithm = new LOOCAlgorithm();
        List<Link> links = new ArrayList<>(topology.edgeSet());
        List<Node> satellites = new ArrayList<>(topology.vertexSet());
        List<Node> EIZSatellites = loocAlgorithm.getEIZSatellite(EIZ, satellites);
        List<Link> EIZ_ISLs = loocAlgorithm.getEIZ_ISL(EIZSatellites, links);

        Integer non_EIZ_ISLs = 0;
        Integer inOrbitLinks = 0;
        List<Double> EIZ_ISLs_EC = new ArrayList<>();
        List<Double> non_EIZ_ISLs_EC = new ArrayList<>();
        List<Double> in_OrbitLinks_EC = new ArrayList<>();
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            if (!link.getSource().satelliteOrbitIndex.equals(link.getTarget().satelliteOrbitIndex)) {
                if (EIZ_ISLs.contains(link)) {
                    Double linkEC = linkEnergyConsumption(link);
                    EIZ_ISLs_EC.add(linkEC);
                } else {
                    Double linkEC = linkEnergyConsumption(link);
                    non_EIZ_ISLs_EC.add(linkEC);
                    non_EIZ_ISLs++;
                }
            } else {
                Double linkEC = linkEnergyConsumption(link);
                in_OrbitLinks_EC.add(linkEC);
                inOrbitLinks++;
            }
        }
        Collections.sort(EIZ_ISLs_EC);
        Collections.sort(non_EIZ_ISLs_EC);
        Collections.sort(in_OrbitLinks_EC);

        Double energyConsumption = 0.0;
        if (EIZ_ISLs_EC.isEmpty()) {
            energyConsumption = non_EIZ_ISLs_EC.get(non_EIZ_ISLs_EC.size() - 1) * non_EIZ_ISLs;
        } else if (non_EIZ_ISLs_EC.isEmpty()) {
            energyConsumption = EIZ_ISLs_EC.get(EIZ_ISLs_EC.size() - 1) * EIZ_ISLs.size();
        } else {
            energyConsumption = EIZ_ISLs_EC.get(EIZ_ISLs_EC.size() - 1) * EIZ_ISLs.size()
                    + non_EIZ_ISLs_EC.get(non_EIZ_ISLs_EC.size() - 1) * non_EIZ_ISLs;
        }


        String simulationResult = "全网功耗：" + energyConsumption.toString();
        System.out.println(simulationResult);
        System.out.println("平均功耗：" + energyConsumption / (links.size() - inOrbitLinks));


    }

    public Double pathDistance(GraphPath<Node, Link> path) {
        List<Link> linkList = path.getEdgeList();
        Double pathLength = 0.0;
        for (int j = 0; j < linkList.size(); j++) {
            Link link = linkList.get(j);
            Double distance = satelliteCoordinate.linkDistance(link, time);
            pathLength = pathLength + distance;
        }

        return pathLength;
    }

    public Map<Link, Double> initAllLinkBandwidth(Set<Link> links) {

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
            fileWriter = new FileWriter("/Users/liuyue/IdeaProjects/Simulationw/src/main/java/resources/simulationResult.txt", true);
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



    private static Graph<Node, Link> createInitTopology() {

        Graph<Node, Link> topology = new DefaultDirectedGraph<>(Link.class);

        Integer satellitesTotalOfSingleOrbit = SATELLITES_TOTAL / SATELLITE_ORBITS_TOTAL;
        List<Node> satellites = new ArrayList<>();
        for (int i = 0; i < SATELLITES_TOTAL; i++) {

            Integer satelliteId = i + 1;
            Integer index = satelliteId % satellitesTotalOfSingleOrbit;
            Integer satelliteIndex = null;
            if (index == 0) {
                satelliteIndex = satellitesTotalOfSingleOrbit;
            } else {
                satelliteIndex = index;
            }

            Integer satelliteOrbitIndex = null;
            for (int j = 1; j <= SATELLITE_ORBITS_TOTAL; j++) {
                if (satelliteId > satellitesTotalOfSingleOrbit * (j - 1) && satelliteId <= satellitesTotalOfSingleOrbit * j) {
                    satelliteOrbitIndex = j;
                }
            }

            Node satellite = new Node(satelliteId, satelliteIndex, satelliteOrbitIndex, SATELLITE_ORBIT_HEIGHT,
                    SATELLITE_ORBIT_INCLINATION, SATELLITES_TOTAL, SATELLITE_ORBITS_TOTAL, SATELLITE_PHASE_FACTOR);
            satellites.add(satellite);

            topology.addVertex(satellite);
        }

        satellites.forEach(satellite -> {
            Integer index = satellites.indexOf(satellite);
            Integer satellitesTotal = SATELLITES_TOTAL;

            for (int i = index + 1; i < satellites.size(); i++) {
                int x = i - index;
                if (x == satellitesTotalOfSingleOrbit) {
                    topology.addEdge(satellite, satellites.get(i));
                    topology.addEdge(satellites.get(i), satellite);
                } else if (x == satellitesTotalOfSingleOrbit * (SATELLITE_ORBITS_TOTAL - 1)) {
                    topology.addEdge(satellite, satellites.get(i));
                    topology.addEdge(satellites.get(i), satellite);
                } else if (x == 1 && (index + 1) % satellitesTotalOfSingleOrbit != 0) {
                    topology.addEdge(satellite, satellites.get(i));
                    topology.addEdge(satellites.get(i), satellite);
                } else if (x == satellitesTotalOfSingleOrbit - 1 && (i + 1) % satellitesTotalOfSingleOrbit == 0) {
                    topology.addEdge(satellite, satellites.get(i));
                    topology.addEdge(satellites.get(i), satellite);
                }
            }
        });

        return topology;
    }


    public Double linkEnergyConsumption(Link link) {

        Double linkLength = satelliteCoordinate.linkDistance(link, time) * 1000;
        Double SNR = 1000.0;
        Double lbd = 1.064 * Math.pow(10, -6);
        Double BW = 10 * Math.pow(10, 9);
        Double N0 = Math.pow(10, -17);
        Double pi = Math.PI;
        Double dr = 0.125;
        Double waist = 6 * Math.pow(10, -3);

        Double linkEnergyConsumption = SNR * BW * N0 / (1 - Math.exp(-2 * Math.pow(dr, 2) * Math.pow(pi, 2) * Math.pow(waist, 2) /
                (Math.pow(pi, 2) * Math.pow(waist, 4) + Math.pow(linkLength, 2) * Math.pow(lbd, 2))));

        return linkEnergyConsumption;
    }

}
