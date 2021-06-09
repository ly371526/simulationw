import cn.edu.bupt.simulationw.Link;
import cn.edu.bupt.simulationw.Node;
import cn.edu.bupt.simulationw.SatelliteCoordinate;
import cn.edu.bupt.simulationw.SimulationManager;
import org.jgrapht.Graph;

import java.beans.SimpleBeanInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        SimulationManager simulationManager = new SimulationManager();
        Graph<Node, Link> topology = simulationManager.getInitTopology();
        SatelliteCoordinate satelliteCoordinate = new SatelliteCoordinate();
        List<Map<String, Double>> orbitCoordinate = new ArrayList<>();
        List<Map<String, Double>> orbitCoordinateSRCS = new ArrayList<>();
        topology.vertexSet().forEach(node -> {
            if (node.getSatelliteOrbitIndex() == 1) {
                orbitCoordinate.add(satelliteCoordinate.satelliteSRCSToGcs(node, 100L));
                orbitCoordinateSRCS.add(satelliteCoordinate.satelliteCoordinate(node, 100L));
            }
        });

        List<Double> distances = new ArrayList<>();
        for (int i = 0; i < orbitCoordinate.size() - 1; i++) {
            Map<String, Double> srcCoordinate = orbitCoordinate.get(i);
            Map<String, Double> dstCoordinate = orbitCoordinate.get(i + 1);

            Double src_long = srcCoordinate.get("longitude");
            Double src_lat = srcCoordinate.get("latitude");
            Double src_height = srcCoordinate.get("height");

            Double dst_long = dstCoordinate.get("longitude");
            Double dst_lat = dstCoordinate.get("latitude");
            Double dst_height = dstCoordinate.get("height");

            Double srcX = src_height * Math.cos(Math.toRadians(src_long)) * Math.cos(Math.toRadians(src_lat));
            Double srcY = src_height * Math.cos(Math.toRadians(src_lat)) * Math.sin(Math.toRadians(src_long));
            Double srcZ = src_height * Math.sin(Math.toRadians(src_lat));

            Double dstX = dst_height * Math.cos(Math.toRadians(dst_long)) * Math.cos(Math.toRadians(dst_lat));
            Double dstY = dst_height * Math.cos(Math.toRadians(dst_lat)) * Math.sin(Math.toRadians(dst_long));
            Double dstZ = dst_height * Math.sin(Math.toRadians(dst_lat));

            Double distanceM = Math.sqrt(Math.pow(Math.abs(srcX - dstX), 2) + Math.pow(Math.abs(srcY - dstY), 2)
                    + Math.pow(Math.abs(srcZ - dstZ), 2));

            Double distanceKm = distanceM / 1000.00;
            distances.add(distanceKm);
        }
        System.out.println("11");

//        //空间直角坐标系(xyz)转换大地坐标系(经纬度)
//        double a = 6378137.000;//长半轴
//        double f = 1 / 298.3;//扁率
//        double b = a - a * f;//短半轴
//        double ec = 1 - Math.pow(b, 2) / Math.pow(a, 2);//第一偏心率平方
//        double ecc= ec / (1-ec);//第二偏心率平方
//        double X =-2177502.7;
//        double Y =4388847.863;
//        double Z =4070140.921;
//        double L = Math.atan(Y / X);
//        if (X < 0)
//            L += Math.PI;
//        double r = Math.sqrt(X * X + Y * Y);
//        double B1 = Math.atan(Z / r);
//        double B2;
//        while (true)
//        {
//            double W1 = Math.sqrt(1 - ec * (Math.sin(B1) * Math.sin(B1)));
//            double N1 = a / W1;
//            B2 = Math.atan((Z + N1 * ec * Math.sin(B1)) / r);
//            if (Math.abs(B2 - B1) <= 0.0000000001)
//                break;
//            B1 = B2;
//        }
//        double B = B2;
//        double W = Math.sqrt(1 - ec * (Math.sin(B2) * Math.sin(B2)));
//        double N = a / W;
//        double H = r / Math.cos(B2) - N;
//        L = Math.toDegrees(L);
//        B = Math.toDegrees(B);
//        System.out.println(L+","+B+","+H);

    }



}
