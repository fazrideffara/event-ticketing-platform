package com.fintech.eticket.consumer;

import com.fintech.eticket.common.constant.KafkaConstant;
import com.fintech.eticket.event.OrderPaidEvent;
import com.fintech.eticket.service.ETicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidKafkaConsumer {

    private final ETicketService eTicketService;

    @KafkaListener(
            topics = KafkaConstant.TOPIC_ORDER_PAID,
            groupId = KafkaConstant.GROUP_ID_NOTIFICATION,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderPaidEvent(OrderPaidEvent event) {
        log.info("Received OrderPaidEvent from Kafka: orderNumber={}, event={}, totalAmount={}",
                event.getOrderNumber(), event.getEventName(), event.getTotalAmount());
        try {
            eTicketService.issueTicketFromEvent(event);
        } catch (Exception e) {
            log.error("Error processing OrderPaidEvent for orderNumber: {}", event.getOrderNumber(), e);
        }
    }
}
