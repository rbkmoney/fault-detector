package com.rbkmoney.faultdetector.converter;

import com.rbkmoney.faultdetector.data.ServiceEvent;
import com.rbkmoney.faultdetector.model.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ServiceEventToRowConverter implements Converter<ServiceEvent, Row> {

    @Override
    public Row convert(ServiceEvent serviceEvent) {
        Row row = new Row();
        row.setBucketName(serviceEvent.getServiceId());
        row.setKey(serviceEvent.getRequestId());
        row.setValue(serviceEvent);
        return row;
    }
}
