package com.rbkmoney.faultdetector.data;

import lombok.Data;

/**
 * ID сервиса и его настройки. Изначально задается с дефолтными настройками, но в дальнейшем можно
 * корректировать для достижения наиболее корректных показателей
 */
@Data
public class Service {

    private String serviceId;

    private long eventErrorTimeout;

    private int maxCountHoveringEvents;

}
