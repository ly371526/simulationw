package cn.edu.bupt.simulationw;

import org.jgrapht.Graph;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServiceGenerator {

    public List<SatelliteService> serviceGenerator(Integer servicesTotal, List<Node> satellites, Integer[] EIZ) {
        List<SatelliteService> services = new ArrayList<>();
//        LOOCAlgorithm loocAlgorithm = new LOOCAlgorithm();
//
//        List<Node> EIZSatellites = loocAlgorithm.getEIZSatellite(EIZ, satellites);
        List<Node> srcNodes = new ArrayList<>(satellites);

        Random random = new Random();

        for (int i = 0; i < servicesTotal; i++) {
            Integer srcIndex = random.nextInt(srcNodes.size());
            Node srcNode = srcNodes.get(srcIndex);
            List<Node> dstNodes = new ArrayList<>(satellites);
//            Integer srcNodeIndex = srcNode.satelliteIndex;
            dstNodes.remove(srcNode);
//            List<Node> dstNodeCircle = new ArrayList<>();
//            dstNodes.forEach(node -> {
//                if (node.satelliteIndex.equals(srcNodeIndex)) {
//                    dstNodeCircle.add(node);
//                }
//            });
            Integer dstIndex = random.nextInt(dstNodes.size());
            Node dstNode = dstNodes.get(dstIndex);
            dstNodes.clear();

//            Double serviceBandwidthD = new BigDecimal(random.nextDouble()).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
            Integer serviceBandwidthI = random.nextInt(6);
            Double serviceBandwidth = (double) serviceBandwidthI / 10;

            SatelliteService service = new SatelliteService(srcNode, dstNode, serviceBandwidth, null, null);
            services.add(service);
        }

        return services;
    }

}
