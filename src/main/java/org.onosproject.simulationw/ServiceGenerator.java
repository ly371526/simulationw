package org.onosproject.simulationw;

import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServiceGenerator implements ServiceGenerateService {

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    public List<Service> serviceGenerator(Integer servicesTotal) {

        List<Service> services = new ArrayList<>();
        List<Device> devices = new ArrayList<>();
        deviceService.getDevices().forEach(devices::add);
        Random random = new Random();

        for (int i = 0; i < servicesTotal; i++) {
            Integer srcIndex = random.nextInt(devices.size());
            DeviceId srcNode = devices.get(srcIndex).id();
            List<Device> dstDevices = new ArrayList<>(devices);
            dstDevices.remove(devices.get(srcIndex));
            Integer dstIndex = random.nextInt(dstDevices.size());
            DeviceId dstNode = dstDevices.get(dstIndex).id();
            dstDevices.clear();

            Double serviceBandwidthD = random.nextDouble();
            Integer serviceBandwidthI = random.nextInt(5);
            Double serviceBandwidth = serviceBandwidthD + serviceBandwidthI;

            Service service = new DefaultService(srcNode, dstNode, serviceBandwidth, null);
            services.add(service);
        }

        return services;
    }
}
