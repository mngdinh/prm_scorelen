package com.scorelens.Controller.v3;

import com.scorelens.DTOs.Request.*;
import com.scorelens.Entity.ResponseObject;
import com.scorelens.Service.PlayerService;
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
import java.util.Map;

@Slf4j
@Tag(name = "Player V3", description = "Unified Player API")
@RestController
@RequestMapping("v3/players")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerV3Controller {
    
    @Autowired
    PlayerService playerService;

    @GetMapping
    public ResponseObject getPlayers(
            @Parameter(description = "Query type: all, byId, byTeam, byCustomer",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"all", "byId", "byTeam", "byCustomer"}
                    ))
            @RequestParam(defaultValue = "all") String queryType,
            
            @Parameter(description = "Player ID (required for queryType=byId)")
            @RequestParam(required = false) Integer playerId,

            @Parameter(description = "Team ID (required for queryType=byTeam)")
            @RequestParam(required = false) Integer teamId,

            @Parameter(description = "Customer ID (required for queryType=byCustomer)")
            @RequestParam(required = false) String customerId,

            @Parameter(description = "Page number (1-based)", required = true)
            @RequestParam(required = false, defaultValue = "1") Integer page,

            @Parameter(description = "Page size", required = true)
            @RequestParam(required = false, defaultValue = "10") Integer size,

            @Parameter(description = "Sort field",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"createAt", "totalScore"}
                    ))
            @RequestParam(required = false, defaultValue = "createAt") String sortBy,

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
        if (teamId != null) filters.put("teamId", teamId);
        if (customerId != null && !customerId.isEmpty()) filters.put("customerId", customerId);

        return ResponseObject.builder()
                .status(1000)
                .message("Player List")
                .data(playerService.getAll(req, filters))
                .build();
    }

    @PostMapping
    public ResponseObject createPlayer(@RequestBody PlayerCreateRequest request) {
        return ResponseObject.builder()
                .status(1000)
                .message("Create new Player successfully")
                .data(playerService.createPlayer(request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseObject deletePlayer(@PathVariable Integer id) {
        return ResponseObject.builder()
                .status(1000)
                .message("Player with ID " + id + " has been deleted")
                .data(playerService.delete(id))
                .build();
    }

    @Operation(summary = "Update player with unified parameters",
               description = "Unified API that combines all PUT operations from v1 controller")
    @PutMapping
    public ResponseObject updatePlayer(@RequestBody PlayerV3UpdateRequest request) {
        try {
            String updateType = request.getUpdateType();
            if (updateType == null) {
                return ResponseObject.builder()
                        .status(400)
                        .message("updateType is required. Valid values: update, saveCustomer")
                        .build();
            }

            if (request.getId() == null) {
                return ResponseObject.builder()
                        .status(400)
                        .message("Player ID is required")
                        .build();
            }

            switch (updateType.toLowerCase()) {
                case "update":
                    return handleUpdatePlayer(request);
                case "savecustomer":
                    return handleSaveCustomer(request);
                default:
                    return ResponseObject.builder()
                            .status(400)
                            .message("Invalid updateType. Valid values: update, saveCustomer")
                            .build();
            }
        } catch (Exception e) {
            log.error("Error in updatePlayer: ", e);
            return ResponseObject.builder()
                    .status(500)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }

    private ResponseObject handleUpdatePlayer(PlayerV3UpdateRequest request) {
        PlayerUpdateRequest updateRequest = new PlayerUpdateRequest();
        updateRequest.setName(request.getName());
        updateRequest.setTotalScore(request.getTotalScore() != null ? request.getTotalScore() : 0);
        updateRequest.setStatus(request.getStatus());
        updateRequest.setCustomerID(request.getCustomerID());

        return ResponseObject.builder()
                .status(1000)
                .message("Update Player information successfully")
                .data(playerService.updatePlayer(request.getId(), updateRequest))
                .build();
    }

    private ResponseObject handleSaveCustomer(PlayerV3UpdateRequest request) {
        if (request.getInfo() == null || request.getInfo().trim().isEmpty()) {
            return ResponseObject.builder()
                    .status(400)
                    .message("Customer info (email or phone number) is required for saveCustomer operation")
                    .build();
        }

        CustomerSaveRequest saveRequest = new CustomerSaveRequest();
        saveRequest.setInfo(request.getInfo());

        return ResponseObject.builder()
                .status(1000)
                .message("Update Player information successfully")
                .data(playerService.updateCustomer(request.getId(), saveRequest))
                .build();
    }
}
