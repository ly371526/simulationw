package cn.edu.bupt.simulationw;

import org.jgrapht.GraphPath;

public class SatelliteService {

    private Node srcNode;
    private Node dstNode;
    private Double serviceBandwidth;
    private GraphPath<Node, Link> servicePath;
    private Double delay;

    public SatelliteService(Node srcNode, Node dstNode, Double serviceBandwidth, GraphPath<Node, Link> servicePath, Double delay) {
        this.srcNode = srcNode;
        this.dstNode = dstNode;
        this.serviceBandwidth = serviceBandwidth;
        this.servicePath = servicePath;
        this.delay = delay;
    }

    public Node getSrcNode() {
        return srcNode;
    }

    public Node getDstNode() {
        return dstNode;
    }

    public Double getServiceBandwidth() {
        return serviceBandwidth;
    }

    public GraphPath<Node, Link> getServicePath() {
        return servicePath;
    }

    public Double getDelay() {
        return delay;
    }

    public void setSrcNode(Node srcNode) {
        this.srcNode = srcNode;
    }

    public void setDstNode(Node dstNode) {
        this.dstNode = dstNode;
    }

    public void setServiceBandwidth(Double serviceBandwidth) {
        this.serviceBandwidth = serviceBandwidth;
    }

    public void setServicePath(GraphPath<Node, Link> servicePath) {
        this.servicePath = servicePath;
    }

    public void setDelay(Double delay) {
        this.delay = delay;
    }
}
