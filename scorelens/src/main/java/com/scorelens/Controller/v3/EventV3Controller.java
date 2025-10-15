package com.scorelens.Controller.v3;

import com.scorelens.DTOs.Request.EventRequest;
import com.scorelens.DTOs.Request.EventV3Request;
import com.scorelens.DTOs.Request.PageableRequestDto;
import com.scorelens.DTOs.Response.EventResponse;
import com.scorelens.DTOs.Response.PageableResponseDto;
import com.scorelens.Entity.ResponseObject;
import com.scorelens.Service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "Event V3", description = "Unified Event API")
@RestController
@RequestMapping("v3/events")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventV3Controller {
    
    @Autowired
    EventService eventService;

    @GetMapping
    public ResponseObject getEvents(
            @Parameter(description = "Query type: byPlayer, byGameSet, byPlayerAndGameSet",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"byPlayer", "byGameSet", "byPlayerAndGameSet"}
                    ))
            @RequestParam(defaultValue = "byGameSet") String queryType,
            
            @Parameter(description = "Player ID (required for queryType=byPlayer or byPlayerAndGameSet)")
            @RequestParam(required = false) Integer playerId,
            
            @Parameter(description = "Game Set ID (required for queryType=byGameSet, byPlayerAndGameSet, or countByGameSet)")
            @RequestParam(required = false) Integer gameSetId,
            
            @Parameter(description = "Page number (1-based)", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            
            @Parameter(description = "Page size", required = true)
            @RequestParam(defaultValue = "10") Integer size,
            
            @Parameter(description = "Sort field",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"timeStamp", "player", "gameSet"}
                    ))
            @RequestParam(defaultValue = "timeStamp") String sortBy,
            
            @Parameter(description = "Sort direction (asc/desc)",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"desc", "asc"}
                    ))
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PageableRequestDto req = PageableRequestDto.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Map<String, Object> filters = new HashMap<>();
        filters.put("queryType", queryType);
        if (playerId != null) filters.put("playerId", playerId);
        if (gameSetId != null) filters.put("gameSetId", gameSetId);

        PageableResponseDto<EventResponse> res = eventService.getAll(req, filters);

        return ResponseObject.builder()
                .status(1000)
                .message("Success")
                .data(res)
                .build();
    }
    
    @PostMapping
    public ResponseObject addEvent(@RequestBody EventRequest event) {
        return ResponseObject.builder()
                .status(1000)
                .message("Event added")
                .data(eventService.addEvent(event))
                .build();
    }
    
    @DeleteMapping("/player/{id}")
    public ResponseObject deleteEventByPlayerID(@PathVariable int id) {
        return ResponseObject.builder()
                .status(1000)
                .message("Events deleted")
                .data(eventService.deleteEventByPlayerID(id))
                .build();
    }
    
    @DeleteMapping("/game_set/{id}")
    public ResponseObject deleteEventByRoundID(@PathVariable int id) {
        return ResponseObject.builder()
                .status(1000)
                .message("Events deleted")
                .data(eventService.deleteEventByGameSetID(id))
                .build();
    }
}
