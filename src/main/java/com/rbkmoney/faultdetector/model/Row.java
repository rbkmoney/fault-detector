package com.rbkmoney.faultdetector.model;

import com.rbkmoney.faultdetector.data.ServiceEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Row {

    private String bucketName;
    private String key;
    private ServiceEvent value;

}
