package com.magictickets.backend.infrastructure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.magictickets.backend.application.port.PurchaseNotifier;

@Component
public class ConsolePurchaseNotifier implements PurchaseNotifier {

    private static final Logger logger = LoggerFactory.getLogger(ConsolePurchaseNotifier.class);

    @Override
    public void notifyPurchase(String eventName, int quantity) {
        logger.info("Purchase confirmed: {} ticket(s) for event '{}'", quantity, eventName);
    }
}