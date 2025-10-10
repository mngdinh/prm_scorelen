    package com.scorelens.Controller.v1;

    import com.scorelens.DTOs.Request.ShotEvent;
    import com.scorelens.Entity.ResponseObject;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import lombok.AccessLevel;
    import lombok.RequiredArgsConstructor;
    import lombok.experimental.FieldDefaults;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.messaging.handler.annotation.DestinationVariable;
    import org.springframework.messaging.handler.annotation.MessageMapping;
    import org.springframework.messaging.simp.SimpMessagingTemplate;
    import org.springframework.web.bind.annotation.*;


    @RequiredArgsConstructor
    @Slf4j
    @Tag(name = "Web Socket", description = "Manage Noti")
    @RestController
    @RequestMapping("/v1/noti")
    @CrossOrigin(origins = {"http://localhost:5173", "exp://192.168.90.68:8081", "https://scorelens.onrender.com", "http://localhost:8080"})
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public class WebSocketV1Controller {

        SimpMessagingTemplate messagingTemplate;

        // REST API forward to notification topic
        @PostMapping("/send-notification")
        public ResponseObject sendNotification(@RequestParam String message, @RequestParam String tableID) {
            log.info("Sending notification from table {}: {} ", tableID, message);
            messagingTemplate.convertAndSend("/topic/notification/" + tableID, message);
            return ResponseObject.builder()
                    .status(1000)
                    .message(message)
                    .data(message)
                    .build();
        }

        // REST API forward to logging_notification topic
        @PostMapping("/send-logging")
        public ResponseObject sendLogging(@RequestParam String message, @RequestParam String tableID) {
            log.info("Sending logging from table {}: {} ", tableID, message);
            messagingTemplate.convertAndSend("/topic/logging_notification/" + tableID, message);
            return ResponseObject.builder()
                    .status(1000)
                    .message(message)
                    .data(message)
                    .build();
        }

        // REST API forward to shot_event topic
        @PostMapping("/shot_event")
        public ResponseObject shotEvent(@RequestBody ShotEvent event, @RequestParam String tableID) {
            log.info("Sending shot_event from table {}: {} ", tableID, event);
            messagingTemplate.convertAndSend("/topic/shot_event/" + tableID, event);
            return ResponseObject.builder()
                    .status(1000)
                    .message("shot event")
                    .data(event)
                    .build();
        }


    }
