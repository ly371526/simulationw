package org.onosproject.simulationw;

import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component(immediate = true, service = ServiceGenerateService.class)
public class ServiceGenerator implements ServiceGenerateService {

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected SatelliteConstellationService satelliteConstellationService;


    @Override
    public List<Service> serviceGenerator(Integer servicesTotal) {

        List<Service> services = new ArrayList<>();
//        Integer[] band = {50, -50};
//        List<DeviceId> devices = satelliteTopologyService.getEIZSatellite(band, satelliteConstellationService.satelliteNodePara());
        List<Device> devices = new ArrayList<>();
        deviceService.getDevices().spliterator().forEachRemaining(devices::add);

        Random random = new Random();

        for (int i = 0; i < servicesTotal; i++) {
            Integer srcIndex = random.nextInt(devices.size());
            DeviceId srcNode = devices.get(srcIndex).id();
            List<Device> dstDevices = new ArrayList<>(devices);
            dstDevices.remove(devices.get(srcIndex));
            Integer dstIndex = random.nextInt(dstDevices.size());
            DeviceId dstNode = dstDevices.get(dstIndex).id();
            dstDevices.clear();

            Double serviceBandwidthD = new BigDecimal(random.nextDouble()).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
            Integer serviceBandwidthI = random.nextInt(2);
            Double serviceBandwidth = serviceBandwidthD + serviceBandwidthI;

            Service service = new DefaultService(srcNode, dstNode, serviceBandwidth, null, null);
            services.add(service);
        }

        return services;
    }
}
