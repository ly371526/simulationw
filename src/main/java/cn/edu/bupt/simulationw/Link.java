package cn.edu.bupt.simulationw;

import org.jgrapht.graph.DefaultEdge;

public class Link extends DefaultEdge {

    public Node getSource() {
        return (Node) super.getSource();
    }

    public Node getTarget() {
        return (Node) super.getTarget();
    }

    @Override
    public String toString() {
        return "(" + this.getSource().satelliteId + "-->" + this.getTarget().satelliteId + ")";
    }
}
