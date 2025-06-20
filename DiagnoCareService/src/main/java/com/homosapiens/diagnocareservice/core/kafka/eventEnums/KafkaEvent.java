package com.homosapiens.diagnocareservice.core.kafka.eventEnums;

public enum KafkaEvent {
    AVAILABILITY_CREATED,
    AVAILABILITIES_GENERATED,
    AVAILABILITY_DELETED;

    @Override
    public String toString() {
        return name();
    }
}
