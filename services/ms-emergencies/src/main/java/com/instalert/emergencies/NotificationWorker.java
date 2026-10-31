package com.instalert.emergencies;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.*;
@Component
@EnableScheduling
@ConditionalOnProperty(name="notifications.worker.enabled",havingValue="true",matchIfMissing=true)
public class NotificationWorker {
 private final NotificationDelivery delivery;
 NotificationWorker(NotificationDelivery delivery){this.delivery=delivery;}
 @Scheduled(fixedDelayString="${notifications.poll-ms:1000}") public void poll(){delivery.deliver();}
}
