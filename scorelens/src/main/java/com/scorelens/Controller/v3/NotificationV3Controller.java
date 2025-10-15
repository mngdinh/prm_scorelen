package com.scorelens.Controller.v3;


import com.scorelens.DTOs.Request.NotificationRequest;
import com.scorelens.DTOs.Request.PageableRequestDto;
import com.scorelens.Entity.ResponseObject;
import com.scorelens.Service.NotificationService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "Notification", description = "Handling Notifications")
@RestController
@RequestMapping("v3/notifications")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationV3Controller {

    NotificationService notificationService;

    @PostMapping
    public ResponseObject newNotification(@RequestBody NotificationRequest request){
        return ResponseObject.builder()
                .status(1000)
                .message("New Notification")
                .data(notificationService.saveNotification(request))
                .build();
    }

    @GetMapping()
    public ResponseObject getNotificationListByBilliardMatchID(
            @Parameter(description = "Query type: all, byId",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"all", "byId", "byMatch"}
                    ))
            @RequestParam(defaultValue = "all") String queryType,

            @Parameter(description = "Notification ID (required for queryType=byId)")
            @RequestParam(required = false) Integer notificationId,

            @Parameter(description = "Match ID (required for queryType=byMatch)")
            @RequestParam(required = false) Integer matchId,

            @Parameter(description = "Page number (1-based)", required = true)
            @RequestParam(required = false, defaultValue = "1") Integer page,

            @Parameter(description = "Page size", required = true)
            @RequestParam(required = false, defaultValue = "10") Integer size,

            @Parameter(description = "Sort field",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"notificationID", "createAt"}
                    ))
            @RequestParam(required = false, defaultValue = "notificationID") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"desc", "asc"}
                    ))
            @RequestParam(defaultValue = "desc") String sortDirection
    ){
        PageableRequestDto req = PageableRequestDto.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Map<String, Object> filters = new HashMap<>();
        filters.put("queryType", queryType);
        if (notificationId != null) filters.put("notificationId", notificationId);
        if (matchId != null) filters.put("matchId", matchId);

        return ResponseObject.builder()
                .status(1000)
                .message("Notification List")
                .data(notificationService.getAll(req, filters))
                .build();
    }
}
