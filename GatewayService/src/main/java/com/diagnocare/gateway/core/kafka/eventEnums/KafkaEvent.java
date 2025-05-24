package com.diagnocare.gateway.core.kafka.eventEnums;

public enum KafkaEvent {
    USER_REGISTERED,
    USER_UPDATE,
    USER_DELETED;

    @Override
    public String toString() {
        return name();
    }
}
