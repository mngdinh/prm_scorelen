package com.scorelens.Service.ConcreteHandler;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.scorelens.DTOs.Request.ProducerRequest;
import com.scorelens.Enums.KafkaCode;
import com.scorelens.Service.BilliardMatchService;
import com.scorelens.Service.BilliardTableService;
import com.scorelens.Service.Interface.KafkaCodeHandler;
import com.scorelens.Service.Interface.customAnnotation.KafkaCodeMapping;
import com.scorelens.Service.RealTimeNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@KafkaCodeMapping(KafkaCode.PAYMENT)
@Slf4j
@RequiredArgsConstructor
public class PaymentHandler implements KafkaCodeHandler {

    private final BilliardTableService tableService;

    private final RealTimeNotification realTimeNotification;

    private final BilliardMatchService matchService;

    @Override
    public void handle(ProducerRequest request) throws FirebaseMessagingException {
        //set paid status in match to true
        Integer matchID = (Integer) request.getData();
        boolean isPaid = matchService.setPaidStatus(matchID);
        //free table
        tableService.setAvailable(request.getTableID());

    }
}
