package com.cobre.notificationservice.application.port.out;

import java.time.Instant;

public interface ClockPort {

    Instant now();
}

