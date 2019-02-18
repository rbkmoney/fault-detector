package com.rbkmoney.faultdetector.converter;

import com.rbkmoney.faultdetector.data.ServiceEvent;
import com.rbkmoney.faultdetector.model.ServiceEventRow;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ServiceEventToRowConverter implements Converter<ServiceEvent, ServiceEventRow> {

    @Override
    public ServiceEventRow convert(ServiceEvent serviceEvent) {
        ServiceEventRow serviceEventRow = new ServiceEventRow();
        serviceEventRow.setBucketName(serviceEvent.getServiceId());
        serviceEventRow.setKey(serviceEvent.getOperationId());
        serviceEventRow.setValue(serviceEvent);
        return serviceEventRow;
    }
}
