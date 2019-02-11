package com.rbkmoney.faultdetector.handlers;

public interface Handler<T> {

    void handle(T id) throws Exception;

}
