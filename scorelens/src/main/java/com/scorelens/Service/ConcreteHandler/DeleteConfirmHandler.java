package com.scorelens.Service.ConcreteHandler;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.scorelens.DTOs.Request.ProducerRequest;
import com.scorelens.DTOs.Request.WebsocketReq;
import com.scorelens.Enums.KafkaCode;
import com.scorelens.Enums.WSFCMCode;
import com.scorelens.Enums.WebSocketTopic;
import com.scorelens.Service.FCMService;
import com.scorelens.Service.Interface.KafkaCodeHandler;
import com.scorelens.Service.Interface.customAnnotation.KafkaCodeMapping;
import com.scorelens.Service.RealTimeNotification;
import com.scorelens.Service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@KafkaCodeMapping(KafkaCode.DELETE_CONFIRM)
@RequiredArgsConstructor
public class DeleteConfirmHandler implements KafkaCodeHandler {

    final RealTimeNotification realTimeNotification;

    @Override
    public void handle(ProducerRequest request){
        String tableID = request.getTableID();
        int deleteCount = (Integer) request.getData();
        realTimeNotification.sendRealTimeNotification(
                "Delete Event count: " + deleteCount,
                WebSocketTopic.NOTI_NOTIFICATION,
                tableID,
                WSFCMCode.WARNING
        );

    }
}
