package cn.edu.bupt.simulationw;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Coordinates {
    private static final Long time = 100L;
    private static final Integer SATELLITE_ORBIT_HEIGHT = 540;
    private static final Integer SATELLITE_ORBIT_INCLINATION = 53;
    private static final Integer SATELLITES_TOTAL = 1584;
    private static final Integer SATELLITE_ORBITS_TOTAL = 66;
    private static final Integer SATELLITE_PHASE_FACTOR = 0;
    public static void main(String[] args) {
        Graph<Node, Link> topology = createInitTopology();
        Coordinates c = new Coordinates();
        SatelliteCoordinate satelliteCoordinate = new SatelliteCoordinate();
        topology.vertexSet().forEach(node -> {
            Map<String, Double> coordinate = satelliteCoordinate.satelliteSRCSToGcs(node, time);
            c.saveAsFileWriter((node.satelliteId - 1) + ","  + (node.satelliteOrbitIndex - 1) + "," + (node.satelliteIndex - 1) + ","
                    + coordinate.get("longitude").toString() + "," + coordinate.get("latitude") + ","
                    + (coordinate.get("height") - 6378.155));
            c.saveAsFileWriter("\n");
        });
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

    private void saveAsFileWriter(String content) {
        FileWriter fileWriter = null;
        File file = new File("");
        try {
            // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
            String p = file.getCanonicalPath();
            String path = p + "/src/main/resources/Coordinates-66*24.txt";
            fileWriter = new FileWriter(path, true);
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
