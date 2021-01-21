package org.onosproject.simulationw;

import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.device.DeviceService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

@Component(immediate = true, service = SatelliteConstellationService.class)
public class SatelliteConstellation implements SatelliteConstellationService {

    private static final String SATELLITE_ORBIT_HEIGHT = "satelliteOrbitHeight";
    private static final String SATELLITE_ORBIT_INCLINATION = "satelliteOrbitInclination";
    private static final String SATELLITES_TOTAL_NUMBER = "satellitesTotalNumber";
    private static final String SATELLITE_INDEX = "satelliteIndex";
    private static final String SATELLITE_ORBIT_INDEX = "satelliteOrbitIndex";
    private static final String SATELLITE_ORBITS_TOTAL_NUMBER = "satelliteOrbitsTotalNumber";
    private static final String SATELLITES_NUMBER_OF_SINGLE_ORBIT = "satellitesNumberOfSingleOrbit";
    private static final String SATELLITES_PHASE_FACTOR = "satellitePhaseFactor";
    private static final Integer satelliteOrbitHeight = 550;
    private static final Integer LEOSatelliteTotal = 66;
    private static final Integer LEOSatelliteOrbitTotal = 6;
    private static final Integer satelliteOrbitInclination = 53;
    private static final Integer satellitePhaseFactor = 0;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;


    @Override
    public Map<DeviceId, Map<String, Integer>> satelliteNodePara() {

        Map<DeviceId, Map<String, Integer>> satellitesNodeParaMap = new HashMap<>();

        Iterable<Device> devices = deviceService.getDevices();
        devices.forEach(device -> {
            /*
             用于存放单颗卫星的轨道参数：
             该颗卫星在其轨道内的编号 --> satelliteIndex
             该颗卫星所在轨道数 --> satelliteOrbitIndex
             该颗卫星轨道高度(km) --> satelliteOrbitHeight
             该颗卫星轨道倾角(°) --> satelliteOrbitInclination
             该类卫星总数 --> satellitesTotalNumber
             该类卫星轨道总数 --> satelliteOrbitsTotalNumber
             该类卫星每个轨道上的卫星数量 --> satelliteNumberOfSingleOrbit
             该类卫星相位因子 --> satellitePhaseFactor
             */
            Map<String, Integer> satelliteParaMap = new HashMap<>();
            String deviceIdString = device.id().toString();
            String s = deviceIdString.substring((deviceIdString.length()) - 3);
            Integer satelliteId = Integer.parseInt(s, 16);

            Integer LEOSatelliteTotalOfSingle = LEOSatelliteTotal / LEOSatelliteOrbitTotal;
            Integer index = satelliteId % LEOSatelliteTotalOfSingle;
            if (index == 0) {
                Integer satelliteIndex = LEOSatelliteTotal / LEOSatelliteOrbitTotal;
                satelliteParaMap.put(SATELLITE_INDEX, satelliteIndex);
            } else {
                Integer satelliteIndex = index;
                satelliteParaMap.put(SATELLITE_INDEX, satelliteIndex);
            }

            for (int i = 1; i <= LEOSatelliteOrbitTotal; i++) {
                if (satelliteId > LEOSatelliteTotalOfSingle * (i - 1) && satelliteId <= LEOSatelliteTotalOfSingle * i) {
                    satelliteParaMap.put(SATELLITE_ORBIT_INDEX, i);
                }
            }

            satelliteParaMap.put(SATELLITE_ORBIT_HEIGHT, satelliteOrbitHeight);
            satelliteParaMap.put(SATELLITE_ORBIT_INCLINATION, satelliteOrbitInclination);
            satelliteParaMap.put(SATELLITES_TOTAL_NUMBER, LEOSatelliteTotal);
            satelliteParaMap.put(SATELLITE_ORBITS_TOTAL_NUMBER, LEOSatelliteOrbitTotal);
            satelliteParaMap.put(SATELLITES_NUMBER_OF_SINGLE_ORBIT, LEOSatelliteTotalOfSingle);
            satelliteParaMap.put(SATELLITES_PHASE_FACTOR, satellitePhaseFactor);

            satellitesNodeParaMap.put(device.id(), satelliteParaMap);
        });

        return satellitesNodeParaMap;

    }

    public Double satelliteOrbitRadius(DeviceId deviceId, Map<DeviceId, Map<String, Integer>> satellitesParaMap) {
        double satelliteOrbitRadius = satellitesParaMap.get(deviceId).get(SATELLITE_ORBIT_HEIGHT) + 6378.155;
        return satelliteOrbitRadius;
    }


    public Double satellitePerigeeArgument(DeviceId deviceId, Map<DeviceId, Map<String, Integer>> satellitesParaMap) {

        Integer satelliteIndex = satellitesParaMap.get(deviceId).get(SATELLITE_INDEX);
        Integer satelliteOrbitIndex = satellitesParaMap.get(deviceId).get(SATELLITE_ORBIT_INDEX);
        Integer satelliteNumberOfSingleOrbit = satellitesParaMap.get(deviceId).get(SATELLITES_NUMBER_OF_SINGLE_ORBIT);
        Integer satelliteOrbitsTotalNumber = satellitesParaMap.get(deviceId).get(SATELLITE_ORBITS_TOTAL_NUMBER);
        Integer satellitePhaseFactor = satellitesParaMap.get(deviceId).get(SATELLITES_PHASE_FACTOR);

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


    public Double satelliteRAAN(DeviceId deviceId, Map<DeviceId, Map<String, Integer>> satellitesParaMap) {

        DecimalFormat decimalFormat = new DecimalFormat("0.0000");

        Integer satelliteOrbitIndex = satellitesParaMap.get(deviceId).get(SATELLITE_ORBIT_INDEX);
        Integer satelliteOrbitsTotalNumber = satellitesParaMap.get(deviceId).get(SATELLITE_ORBITS_TOTAL_NUMBER);
        Double iP = (satelliteOrbitIndex.doubleValue() - 1) / satelliteOrbitsTotalNumber.doubleValue();
        String iPFormat = decimalFormat.format(iP);
        Double iPDouble = Double.parseDouble(iPFormat);

        // StartLink --> 360
        Double satelliteRAAN = Math.toRadians(360 * iPDouble);

        return satelliteRAAN;
    }


    public Double satelliteOrbitPeriod(DeviceId deviceId, Map<DeviceId, Map<String, Integer>> satellitesParaMap) {

        Double satelliteOrbitRadius = satelliteOrbitRadius(deviceId, satellitesParaMap);

        Double satelliteOrbitPeriod = 2 * Math.PI * Math.sqrt(Math.pow(satelliteOrbitRadius, 3) / 398600.5);

        return satelliteOrbitPeriod;
    }


    public Double satelliteMeanAnomaly(DeviceId deviceId, Map<DeviceId, Map<String, Integer>> satellitesParaMap,
                                       Long timeDifference) {

        Integer satelliteOrbitHeight = satellitesParaMap.get(deviceId).get(SATELLITE_ORBIT_HEIGHT);
        Double satelliteOrbitPeriod = satelliteOrbitPeriod(deviceId, satellitesParaMap);

        Double satelliteMeanAnomaly = Math.sqrt(398600.5 / Math.pow(satelliteOrbitHeight + 6378.155, 3)) * (timeDifference % satelliteOrbitPeriod);

        return satelliteMeanAnomaly;
    }


    public Double satelliteLatitudeArgument(DeviceId deviceId, Map<DeviceId, Map<String, Integer>> satellitesParaMap,
                                            Long timeDifference) {

        Double satellitePerigeeArgument = satellitePerigeeArgument(deviceId, satellitesParaMap);
        Double trueAnomaly = satelliteMeanAnomaly(deviceId, satellitesParaMap, timeDifference);

        Double satelliteLatitudeArgument = satellitePerigeeArgument + trueAnomaly;

        return satelliteLatitudeArgument;

    }


    public Map<String, Double> satelliteCoordinate(DeviceId deviceId, Map<DeviceId, Map<String, Integer>> satellitesParaMap,
                                                   Long timeDifference) {

        Map<String, Double> coordinateMap = new HashMap<>();

        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#######.00000000");
        Double satelliteOrbitRadius = satelliteOrbitRadius(deviceId, satellitesParaMap);
        Double satelliteRAAN = satelliteRAAN(deviceId, satellitesParaMap);
        Double satelliteLatitudeArgument = satelliteLatitudeArgument(deviceId, satellitesParaMap, timeDifference);
        Integer satelliteOrbitInclinationDegrees = satellitesParaMap.get(deviceId).get(SATELLITE_ORBIT_INCLINATION);
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

    @Override
    public Map<String, Double> satelliteSRCSToGcs(DeviceId deviceId, Map<DeviceId, Map<String, Integer>> satellitesParaMap,
                                                  Long timeDifference) {

        Map<String, Double> satelliteGcsCoordinate = new HashMap<>();

        Map<String, Double> satelliteCoordinate = satelliteCoordinate(deviceId, satellitesParaMap, timeDifference);

        Double x = satelliteCoordinate.get("x");
        Double y = satelliteCoordinate.get("y");
        Double z = satelliteCoordinate.get("z");

        Double longitude = Math.toDegrees(Math.atan2(y, x));
        Double l = Math.sqrt(x * x + y * y);
        Double latitude = Math.toDegrees(Math.atan2(z, l));
        Double height = satelliteOrbitRadius(deviceId, satellitesParaMap);

        satelliteGcsCoordinate.put("longitude", longitude);
        satelliteGcsCoordinate.put("latitude", latitude);
        satelliteGcsCoordinate.put("height", height);

        return satelliteGcsCoordinate;
    }


    @Override
    public Double linkDistance(Link link, Map<DeviceId, Map<String, Integer>> satelliteNodePara, Long timeDifference) {

        DeviceId srcDevice = link.src().deviceId();
        DeviceId dstDevice = link.dst().deviceId();
        Map<String, Double> srcCoordinate = satelliteCoordinate(srcDevice, satelliteNodePara, timeDifference);
        Map<String, Double> dstCoordinate = satelliteCoordinate(dstDevice, satelliteNodePara, timeDifference);

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
