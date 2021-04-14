package cn.edu.bupt.simulationw;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.YenKShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.math.BigDecimal;
import java.util.*;

public class LOOCAlgorithm {

    private static final Long time = 10L;

    public Graph<Node, Link> em_looc(Integer[] EIZ, Double d_max, List<Node> satellites, List<Link> links) {


        SatelliteCoordinate satelliteCoordinate = new SatelliteCoordinate();
        List<Link> linkList = new ArrayList<>(links);

        List<Node> EIZSatellites = getEIZSatellite(EIZ, satellites);
        List<Link> EIZ_ISL = getEIZ_ISL(EIZSatellites, links);
        List<Double> distances = new ArrayList<>();
        EIZ_ISL.forEach(link -> {
            Double distance = satelliteCoordinate.linkDistance(link, time);
            distances.add(distance);
        });
        linkList.removeAll(EIZ_ISL);

        return buildTopology(satellites, linkList);
    }


    public Graph<Node, Link> elb_looc(Double o_max, Integer[] EIZ, List<Node> satellites, List<Link> links) {

        SatelliteCoordinate satelliteCoordinate = new SatelliteCoordinate();
        List<Node> EIZSatelliteList = getEIZSatellite(EIZ, satellites);

        List<Link> L_INIT = new ArrayList<>();
        List<Link> L_ON = new ArrayList<>();
        links.spliterator().forEachRemaining(link -> {
            if (EIZSatelliteList.contains(link.getSource()) && EIZSatelliteList.contains(link.getTarget())) {
                Integer linkSrcNodeOrbitIndex = link.getSource().satelliteOrbitIndex;
                Integer linkDstNodeOrbitIndex = link.getTarget().satelliteOrbitIndex;
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
            Link l_cand = l_cand(L_INIT);
            Link l_cand_reverse = getReverseLink(l_cand, links);
            Graph<Node, Link> partialTopology = getPartialTopology(l_cand, satellites, links);
            Node src = l_cand.getSource();
            Node dst = l_cand.getTarget();
            YenKShortestPath<Node, Link> yKSPAlg = new YenKShortestPath<Node, Link>(partialTopology);
            List<GraphPath<Node, Link>> DP = yKSPAlg.getPaths(src, dst, 6);

            Map<GraphPath<Node, Link>, Double> dPathOverhead = new HashMap<>();
            List<GraphPath<Node, Link>> DPCopy = new ArrayList<>(DP);
            DPCopy.spliterator().forEachRemaining(path -> {
                List<Link> linkList = path.getEdgeList();
                Double dpLength = 0.0;
                for (int j = 0; j < linkList.size(); j++) {
                    Link link = linkList.get(j);
                    Double distance = satelliteCoordinate.linkDistance(link, time);
                    dpLength = dpLength + distance;
                }
                Double longestLinkLength = satelliteCoordinate.linkDistance(l_cand, time);
                Double overhead = dpLength / longestLinkLength;
                dPathOverhead.put(path, overhead);
                if (overhead > o_max) {
                    DP.remove(path);
                }
            });

            for (int j = 0; j < DP.size(); j++) {
                GraphPath<Node, Link> path = DP.get(j);
                List<Link> pathLinks = path.getEdgeList();
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
            } else {

                Boolean step3 = true;
                for (int j = 0; j < DP.size(); j++) {
                    GraphPath<Node, Link> path = DP.get(j);
                    List<Link> pathLinks = path.getEdgeList();
                    if (L_ON.containsAll(pathLinks)) {
                        step3 = false;
                        break;
                    }
                }

                if (step3) {
                    List<Double> dpOverhead = new ArrayList<>();
                    DP.forEach(path -> {
                        dpOverhead.add(dPathOverhead.get(path));
                    });
                    Collections.sort(dpOverhead);
                    GraphPath<Node, Link> p_dt = null;
                    C:
                    for (int j = 0; j < DP.size(); j++) {
                        GraphPath<Node, Link> path = DP.get(j);
                        if (dPathOverhead.get(path).equals(dpOverhead.get(dpOverhead.size() - 1))) {
                            p_dt = path;
                            break C;
                        }
                    }

                    for (int j = 0; j < p_dt.getEdgeList().size(); j++) {
                        Link link = p_dt.getEdgeList().get(j);
                        Link link_reverse = getReverseLink(link, links);
                        if (!L_ON.contains(link)) {
                            L_ON.add(link);
                            L_ON.add(link_reverse);
                            L_INIT.remove(link);
                            L_INIT.remove(l_cand_reverse);
                        }
                    }
                }
                L_OFF.add(l_cand);
                L_OFF.add(l_cand_reverse);
                L_INIT.remove(l_cand);
                L_INIT.remove(l_cand_reverse);
            }

        }

        return buildTopology(satellites, L_ON);
    }


    public Graph<Node, Link> ecb_looc(Double c_j, Integer[] EIZ, List<Node> satellites, List<Link> links) {

        Integer N_orbit = satellites.iterator().next().satelliteOrbitsTotal;

        List<Boolean> OOB_base = new ArrayList<>();
        for (int i = 0; i < N_orbit; i++) {
            OOB_base.add(false);
        }

        List<Node> EIZSatelliteList = getEIZSatellite(EIZ, satellites);

        Map<Integer, List<Node>> eachCircleSatelliteEIZ = new HashMap<>();
        EIZSatelliteList.forEach(satellite -> {
            Integer satelliteIndex = satellite.satelliteIndex;
            if (eachCircleSatelliteEIZ.keySet().contains(satelliteIndex)) {
                eachCircleSatelliteEIZ.get(satelliteIndex).add(satellite);
            } else {
                List<Node> orbitSatellite = new ArrayList<>();
                orbitSatellite.add(satellite);
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
            if (position < N_orbit) {
                OOB_base.add(position, true);
                OOB_base.remove(position + 1);
            } else {
                A:
                for (int j = 1; j < OOB_base.size(); j++) {
                    Boolean b = OOB_base.get(OOB_base.size() - (average / 2) - 3 * (j - 1));
                    if (!b) {
                        OOB_base.add(OOB_base.size() - (average / 2) - 3 * (j - 1) , true);
                        OOB_base.remove(OOB_base.size() - (average / 2) - 3 * (j - 1));
                        break A;
                    }
                }
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
            List<Node> circleSatellites = eachCircleSatelliteEIZ.get(keyList.get(i));
            Map<Integer, Node> circleSatelliteMap = new HashMap<>();
            circleSatellites.forEach(satellite -> {
                Integer satelliteOrbitIndex = satellite.satelliteOrbitIndex;
                circleSatelliteMap.put(satelliteOrbitIndex - 1, satellite);
            });
            List<Node> circleSatelliteList = new ArrayList<>();
            List<Integer> keys = new ArrayList<>(circleSatelliteMap.keySet());
            Collections.sort(keys);
            keys.forEach(key -> {
                circleSatelliteList.add(circleSatelliteMap.get(key));
            });
            eachCircleSatelliteEIZ.replace(keyList.get(i), circleSatelliteList);
        }

        List<Link> EIZ_ISL = getEIZ_ISL(EIZSatelliteList, links);
        List<Link> L_ON = new ArrayList<>();
        List<Link> L_OFF = new ArrayList<>();
        links.spliterator().forEachRemaining(link -> {
            if (!EIZ_ISL.contains(link)) {
                L_ON.add(link);
            }
        });

        eachCircleSatelliteEIZ.keySet().forEach(circleIndex -> {
            List<Node> circleSatellites = eachCircleSatelliteEIZ.get(circleIndex);
            List<Boolean> circleOOB = OOBs.get(circleIndex);
            for (int i = 0; i < circleSatellites.size(); i++) {
                int j = (i + 1) % circleSatellites.size();
                Node linkEnd_1 = circleSatellites.get(i);
                Node linkEnd_2 = circleSatellites.get(j);
                Boolean linkStatus = circleOOB.get(i);
                EIZ_ISL.forEach(link -> {
                    if ((link.getSource().equals(linkEnd_1) && link.getTarget().equals(linkEnd_2)) ||
                            (link.getSource().equals(linkEnd_2) && link.getTarget().equals(linkEnd_1))) {
                        if (linkStatus.equals(true)) {
                            L_ON.add(link);
                        } else {
                            L_OFF.add(link);
                        }
                    }
                });
            }
        });

        return buildTopology(satellites, L_ON);
    }

    public Graph<Node, Link> buildTopology(List<Node> satellites, List<Link> actionLink) {

        Graph<Node, Link> topology = new DefaultDirectedGraph<Node, Link>(Link.class);
        satellites.spliterator().forEachRemaining(topology::addVertex);
        actionLink.spliterator().forEachRemaining(link -> topology.addEdge(link.getSource(), link.getTarget()));

        return topology;
    }

    public Link l_cand(List<Link> L_INIT) {

        SatelliteCoordinate satelliteCoordinate = new SatelliteCoordinate();
        Link longestLink = L_INIT.get(0);
        Double maxDistance = satelliteCoordinate.linkDistance(longestLink, time);
        for (int i = 1; i < L_INIT.size(); i++) {
            Link link = L_INIT.get(i);
            Double distance = satelliteCoordinate.linkDistance(link, time);
            if (distance > maxDistance) {
                longestLink = link;
                maxDistance = distance;
            }
        }

        return longestLink;
    }

    public Graph<Node, Link> getPartialTopology(Link l_cand, List<Node> satellites, List<Link> links) {

        Integer linkSrcNodeOrbitIndex = l_cand.getSource().satelliteOrbitIndex;
        Integer linkDstNodeOrbitIndex = l_cand.getTarget().satelliteOrbitIndex;

        Graph<Node, Link> partialTopology = new DefaultDirectedGraph<Node, Link>(Link.class);
        List<Node> partialSatellites = new ArrayList<>();
        satellites.spliterator().forEachRemaining(satellite -> {
            Integer satelliteOrbitIndex = satellite.satelliteOrbitIndex;
            if (satelliteOrbitIndex.equals(linkSrcNodeOrbitIndex) || satelliteOrbitIndex.equals(linkDstNodeOrbitIndex)) {
                partialSatellites.add(satellite);
                partialTopology.addVertex(satellite);
            }
        });

        links.spliterator().forEachRemaining(link -> {
            if (partialSatellites.contains(link.getSource()) && partialSatellites.contains(link.getTarget())) {
                if (!link.equals(l_cand)) {
                    partialTopology.addEdge(link.getSource(), link.getTarget(), link);
                }
            }
        });


        return partialTopology;
    }


    public List<Node> getEIZSatellite(Integer[] EIZ, List<Node> satellites) {

        Integer EIZ_up = EIZ[0];
        Integer EIZ_down = EIZ[1];
        SatelliteCoordinate satelliteCoordinate = new SatelliteCoordinate();
        List<Node> EIZSatelliteList = new ArrayList<>();
        satellites.spliterator().forEachRemaining(satellite -> {

            try {
                Map<String, Double> satelliteNodeCoordinate = satelliteCoordinate.satelliteSRCSToGcs(satellite, time);
                Double satelliteLatitude = satelliteNodeCoordinate.get("latitude");
                if (satelliteLatitude <= EIZ_up && satelliteLatitude >= EIZ_down) {
                    EIZSatelliteList.add(satellite);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        });

        return EIZSatelliteList;
    }

    public List<Link> getEIZ_ISL(List<Node> EIZSatelliteList, List<Link> links) {

        List<Link> EIZ_ISL = new ArrayList<>();
        links.spliterator().forEachRemaining(link -> {
            if (EIZSatelliteList.contains(link.getSource()) && EIZSatelliteList.contains(link.getTarget())) {
                Integer linkSrcNodeOrbitIndex = link.getSource().satelliteOrbitIndex;
                Integer linkDstNodeOrbitIndex = link.getTarget().satelliteOrbitIndex;
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
            if (link1.getSource().equals(link.getTarget()) &&
                    link1.getTarget().equals(link.getSource())) {
                reverseLink = link1;
                break;
            }
        }

        return reverseLink;
    }
}
