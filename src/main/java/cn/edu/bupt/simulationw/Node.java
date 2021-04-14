package cn.edu.bupt.simulationw;

public class Node {

    Integer satelliteId;

    Integer satelliteIndex;

    Integer satelliteOrbitIndex;

    Integer satelliteOrbitHeight;

    Integer satelliteOrbitInclination;

    Integer satellitesTotal;

    Integer satelliteOrbitsTotal;

    Integer satellitePhaseFactor;

    Node(Integer satelliteId, Integer satelliteIndex, Integer satelliteOrbitIndex, Integer satelliteOrbitHeight,
         Integer satelliteOrbitInclination, Integer satellitesTotal, Integer satelliteOrbitsTotal, Integer satellitePhaseFactor) {
        this.satelliteId = satelliteId;
        this.satelliteIndex = satelliteIndex;
        this.satelliteOrbitIndex = satelliteOrbitIndex;
        this.satelliteOrbitHeight = satelliteOrbitHeight;
        this.satelliteOrbitInclination = satelliteOrbitInclination;
        this.satellitesTotal = satellitesTotal;
        this.satelliteOrbitsTotal = satelliteOrbitsTotal;
        this.satellitePhaseFactor = satellitePhaseFactor;
    }

    public Integer getSatelliteId() {
        return satelliteId;
    }

    public Integer getSatelliteIndex() {
        return satelliteIndex;
    }

    public Integer getSatelliteOrbitIndex() {
        return satelliteOrbitIndex;
    }

    public Integer getSatelliteOrbitHeight() {
        return satelliteOrbitHeight;
    }

    public Integer getSatelliteOrbitInclination() {
        return satelliteOrbitInclination;
    }

    public Integer getSatellitesTotal() {
        return satellitesTotal;
    }

    public Integer getSatelliteOrbitsTotal() {
        return satelliteOrbitsTotal;
    }

    public Integer getSatellitePhaseFactor() {
        return satellitePhaseFactor;
    }

    public void setSatelliteId(Integer satelliteId) {
        this.satelliteId = satelliteId;
    }

    public void setSatelliteIndex(Integer satelliteIndex) {
        this.satelliteIndex = satelliteIndex;
    }

    public void setSatelliteOrbitIndex(Integer satelliteOrbitIndex) {
        this.satelliteOrbitIndex = satelliteOrbitIndex;
    }

    public void setSatelliteOrbitHeight(Integer satelliteOrbitHeight) {
        this.satelliteOrbitHeight = satelliteOrbitHeight;
    }

    public void setSatelliteOrbitInclination(Integer satelliteOrbitInclination) {
        this.satelliteOrbitInclination = satelliteOrbitInclination;
    }

    public void setSatellitesTotal(Integer satellitesTotal) {
        this.satellitesTotal = satellitesTotal;
    }

    public void setSatelliteOrbitsTotal(Integer satelliteOrbitsTotal) {
        this.satelliteOrbitsTotal = satelliteOrbitsTotal;
    }

    public void setSatellitePhaseFactor(Integer satellitePhaseFactor) {
        this.satellitePhaseFactor = satellitePhaseFactor;
    }
}
