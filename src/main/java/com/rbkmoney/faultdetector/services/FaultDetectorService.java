package com.rbkmoney.faultdetector.services;

import com.rbkmoney.damsel.fault_detector.AvailabilityResponse;
import com.rbkmoney.damsel.fault_detector.FaultDetectorSrv;
import com.rbkmoney.damsel.fault_detector.Operation;
import com.rbkmoney.damsel.fault_detector.SetRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FaultDetectorService implements FaultDetectorSrv.Iface {

    @Override
    public void registerOperation(String serviceId, String requestId, Operation operation) throws TException {

    }

    @Override
    public List<AvailabilityResponse> checkAvailability(List<String> services) throws TException {
        return null;
    }

    @Override
    public void setServiceStatistics(String s, SetRequest setRequest) throws TException {

    }
}
