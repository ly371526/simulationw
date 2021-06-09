package cn.edu.bupt.simulationw;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class SatelliteCoordinate {

    public Double satelliteOrbitRadius(Node satellite) {

        double satelliteOrbitRadius = satellite.satelliteOrbitHeight + 6378.155;

        return satelliteOrbitRadius;
    }

    public Double satellitePerigeeArgument(Node satellite) {

        Integer satelliteIndex = satellite.satelliteIndex;
        Integer satelliteOrbitIndex = satellite.satelliteOrbitIndex;
        Integer satelliteNumberOfSingleOrbit = satellite.satellitesTotal / satellite.satelliteOrbitsTotal;
        Integer satelliteOrbitsTotalNumber = satellite.satelliteOrbitsTotal;
        Integer satellitePhaseFactor = satellite.satellitePhaseFactor;

        DecimalFormat decimalFormat = new DecimalFormat("0.0000");

        Double kS = (satelliteIndex.doubleValue() - 1) / satelliteNumberOfSingleOrbit.doubleValue();
        String kSFormat = decimalFormat.format(kS);
        Double kSDouble = Double.parseDouble(kSFormat);
        Integer iF = (satelliteOrbitIndex - 1) * satellitePhaseFactor;
        Integer PS = satelliteNumberOfSingleOrbit * satelliteOrbitsTotalNumber;
        String iFPS = decimalFormat.format(iF / PS);
        Double iFPSDouble = Double.parseDouble(iFPS);

        Double satellitePerigeeArgument = Math.toRadians(360 * (kSDouble + iFPSDouble));

        return satellitePerigeeArgument;
    }


    public Double satelliteRAAN(Node satellite) {

        DecimalFormat decimalFormat = new DecimalFormat("0.0000");

        Integer satelliteOrbitIndex = satellite.satelliteOrbitIndex;
        Integer satelliteOrbitsTotalNumber = satellite.satelliteOrbitsTotal;
        Double iP = (satelliteOrbitIndex.doubleValue() - 1) / satelliteOrbitsTotalNumber.doubleValue();
        String iPFormat = decimalFormat.format(iP);
        Double iPDouble = Double.parseDouble(iPFormat);

        // StartLink --> 360
        Double satelliteRAAN = Math.toRadians(360 * iPDouble);

        return satelliteRAAN;
    }


    public Double satelliteOrbitPeriod(Node satellite) {

        Double satelliteOrbitRadius = satelliteOrbitRadius(satellite);

        Double satelliteOrbitPeriod = 2 * Math.PI * Math.sqrt(Math.pow(satelliteOrbitRadius, 3) / 398600.5);

        return satelliteOrbitPeriod;
    }


    public Double satelliteMeanAnomaly(Node satellite, Long time) {

        Integer satelliteOrbitHeight = satellite.satelliteOrbitHeight;
        Double satelliteOrbitPeriod = satelliteOrbitPeriod(satellite);

        Double satelliteMeanAnomaly = Math.sqrt(398600.5 / Math.pow(satelliteOrbitHeight + 6378.155, 3)) * (time % satelliteOrbitPeriod);

        return satelliteMeanAnomaly;
    }


    public Double satelliteLatitudeArgument(Node satellite, Long time) {

        Double satellitePerigeeArgument = satellitePerigeeArgument(satellite);
        Double trueAnomaly = satelliteMeanAnomaly(satellite, time);

        Double satelliteLatitudeArgument = satellitePerigeeArgument + trueAnomaly;

        return satelliteLatitudeArgument;

    }


    public Map<String, Double> satelliteCoordinate(Node satellite, Long time) {

        Map<String, Double> coordinateMap = new HashMap<>();

        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#######.00000000");
        Double satelliteOrbitRadius = satelliteOrbitRadius(satellite);
        Double satelliteRAAN = satelliteRAAN(satellite);
        Double satelliteLatitudeArgument = satelliteLatitudeArgument(satellite, time);
        Integer satelliteOrbitInclinationDegrees = satellite.satelliteOrbitInclination;
        Double satelliteOrbitInclination = Math.toRadians(satelliteOrbitInclinationDegrees);

        Double x = satelliteOrbitRadius * (Math.cos(satelliteRAAN) * Math.cos(satelliteLatitudeArgument) -
                Math.sin(satelliteRAAN) * Math.sin(satelliteLatitudeArgument) * Math.cos(satelliteOrbitInclination));
        Double xm = Double.valueOf(decimalFormat.format(x * 1000));
        coordinateMap.put("x", xm);

        Double y = satelliteOrbitRadius * (Math.sin(satelliteRAAN) * Math.cos(satelliteLatitudeArgument) +
                Math.cos(satelliteRAAN) * Math.sin(satelliteLatitudeArgument) * Math.cos(satelliteOrbitInclination));
        Double ym = Double.valueOf(decimalFormat.format(y * 1000));
        coordinateMap.put("y", ym);

        Double z = satelliteOrbitRadius * Math.sin(satelliteLatitudeArgument) * Math.sin(satelliteOrbitInclination);
        Double zm = Double.valueOf(decimalFormat.format(z * 1000));
        coordinateMap.put("z", zm);

        return coordinateMap;
    }


    public Map<String, Double> satelliteSRCSToGcs(Node satellite, Long time) {

        Map<String, Double> satelliteGcsCoordinate = new HashMap<>();

        Map<String, Double> satelliteCoordinate = satelliteCoordinate(satellite, time);

        Double x = satelliteCoordinate.get("x");
        Double y = satelliteCoordinate.get("y");
        Double z = satelliteCoordinate.get("z");

        Double longitude = Math.toDegrees(Math.atan2(y, x));
        Double l = Math.sqrt(x * x + y * y);
        Double latitude = Math.toDegrees(Math.atan2(z, l));
        Double height = z / Math.sin(Math.toRadians(latitude));

        satelliteGcsCoordinate.put("longitude", longitude);
        satelliteGcsCoordinate.put("latitude", latitude);
        satelliteGcsCoordinate.put("height", height);

        return satelliteGcsCoordinate;
    }


    public Double linkDistance(Link link, Long time) {

        Node srcNode = link.getSource();
        Node dstNode = link.getTarget();
        Map<String, Double> srcCoordinate = satelliteCoordinate(srcNode, time);
        Map<String, Double> dstCoordinate = satelliteCoordinate(dstNode, time);

        Double srcX = srcCoordinate.get("x");
        Double srcY = srcCoordinate.get("y");
        Double srcZ = srcCoordinate.get("z");

        Double dstX = dstCoordinate.get("x");
        Double dstY = dstCoordinate.get("y");
        Double dstZ = dstCoordinate.get("z");

        Double distanceM = Math.sqrt(Math.pow(Math.abs(srcX - dstX), 2) + Math.pow(Math.abs(srcY - dstY), 2)
                + Math.pow(Math.abs(srcZ - dstZ), 2));

        Double distanceKm = distanceM / 1000.00;

        return distanceKm;
    }

}
