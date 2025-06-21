package com.homosapiens.diagnocareservice.core.kafka.eventEnums;

public enum KafkaEvent {
    AVAILABILITY_CREATED,
    AVAILABILITY_UPDATED,
    AVAILABILITY_DELETED;

    @Override
    public String toString() {
        return name();
    }
}
