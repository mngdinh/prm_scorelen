package com.scorelens.Controller.v1;


import com.scorelens.DTOs.Request.InformationRequest;
import com.scorelens.DTOs.Request.ProducerRequest;
import com.scorelens.Entity.ResponseObject;
import com.scorelens.Enums.KafkaCode;
import com.scorelens.Service.KafkaService.KafkaProducer;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Kafka Producer", description = "Kafka Producer")
@RestController
@RequestMapping("v1/kafka/producers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaProducerController {

    KafkaProducer kafkaProducer;

    @PostMapping("/send")
    public ResponseObject send(@RequestBody ProducerRequest request) {
        kafkaProducer.sendEvent(request);
        return ResponseObject.builder()
                .status(1000)
                .message("Successfully sent message")
                .build();
    }

    @PostMapping("/send/information")
    public ResponseObject sendInformation(@RequestBody InformationRequest request) {
        kafkaProducer.sendEvent(request);
        return ResponseObject.builder()
                .status(1000)
                .message("Successfully sent message")
                .build();
    }

    @PostMapping("/send/information/{id}")
    public ResponseObject sendInformation(@RequestBody InformationRequest request, @PathVariable String id) {
        kafkaProducer.sendEvent(id, request);
        return ResponseObject.builder()
                .status(1000)
                .message("Successfully sent message")
                .build();
    }


    @PostMapping("send/stop/{id}")
    public ResponseObject sendStop(@RequestParam String id) {
        kafkaProducer.sendEvent(id ,new ProducerRequest(KafkaCode.STOP_STREAM,id, "Stop stream"));
        return ResponseObject.builder()
                .status(1000)
                .message("Successfully sent message")
                .build();
    }


}
