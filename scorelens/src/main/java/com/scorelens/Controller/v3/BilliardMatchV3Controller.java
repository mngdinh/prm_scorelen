package com.scorelens.Controller.v3;

import com.scorelens.DTOs.Request.*;
import com.scorelens.DTOs.Response.*;
import com.scorelens.Entity.BilliardMatch;
import com.scorelens.Entity.ResponseObject;
import com.scorelens.Enums.*;
import com.scorelens.Service.*;
import com.scorelens.Service.KafkaService.KafkaProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "Billiard Match V3", description = "Unified Billiard Match API")
@RestController
@RequestMapping("v3/billiard-matches")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BilliardMatchV3Controller {

    @Autowired
    BilliardMatchService billiardMatchService;

    @Autowired
    BilliardTableService billiardTableService;

    @Autowired
    KafkaProducer producer;

    @Autowired
    GameSetV3Controller gameSetController;

    @Autowired
    EventProcessorService eventProcessorService;

    WebSocketService webSocketService;

    NotificationService notificationService;

    RealTimeNotification realTimeNotification;

    @GetMapping
    public ResponseObject getBilliardMatches(
            @Parameter(description = "Query type: byId, byCustomer, byStaff, byPlayer, filter, byTable",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"byTable", "byId", "byCustomer", "byStaff", "byPlayer",}
                    ))
            @RequestParam(defaultValue = "byTable") String queryType,

            @Parameter(description = "Table ID (required for queryType=byTable)")
            @RequestParam(required = false) String tableId,

            @Parameter(description = "Match ID (required for queryType=byId)")
            @RequestParam(required = false) Integer matchId,

            @Parameter(description = "Customer ID (required for queryType=byCustomer or byCustomer)")
            @RequestParam(required = false) String customerId,

            @Parameter(description = "Staff ID (required for queryType=byStaff or byStaff)")
            @RequestParam(required = false) String staffId,

            @Parameter(description = "Player ID (required for queryType=byPlayer)")
            @RequestParam(required = false) Integer playerId,

            @Parameter(description = "Filter by date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date,

            @Parameter(description = "Filter by status")
            @RequestParam(required = false) String status,

            @Parameter(description = "Filter by mode ID")
            @RequestParam(required = false) Integer modeID,

            @Parameter(description = "Page number (1-based)", required = true)
            @RequestParam(defaultValue = "1") Integer page,

            @Parameter(description = "Page size", required = true)
            @RequestParam(defaultValue = "10") Integer size,

            @Parameter(description = "Sort field",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"startTime", "endTime"}
                    ))
            @RequestParam(defaultValue = "startTime") String sortBy,

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
        if (tableId != null && !"null".equals(tableId)) filters.put("tableId", tableId);
        if (queryType != null && !"null".equals(queryType)) filters.put("queryType", queryType);
        if (matchId != null) filters.put("matchId", matchId);
        if (customerId != null && !"null".equals(customerId)) filters.put("customerId", customerId);
        if (staffId != null && !"null".equals(staffId)) filters.put("staffId", staffId);
        if (playerId != null) filters.put("playerId", playerId);
        if (date != null) filters.put("date", date);
        if (status != null) filters.put("status", status);
        if (modeID != null) filters.put("modeID", modeID);

        PageableResponseDto<BilliardMatchResponse> data = billiardMatchService.getAll(req, filters);

        return ResponseObject.builder()
                .status(1000)
                .message("Success")
                .data(data)
                .build();
    }

    @GetMapping("/byEmail/{id}")
    public ResponseObject getByCustomerID(@PathVariable String id) {
        return ResponseObject.builder()
                .status(1000)
                .message("Get Matchs information successfully")
                .data(billiardMatchService.getByCustomerID(id))
                .build();
    }

    @PostMapping
    public ResponseObject createMatch(@RequestBody BilliardMatchCreateRequest request) {
        BilliardMatchResponse response = billiardMatchService.createMatch(request);
        BilliardTableResponse tableRs = billiardTableService.findBilliardTableById(response.getBilliardTableID());
        String tableCode = tableRs.getTableCode();
        String tableID = response.getBilliardTableID();
        String storeID = tableRs.getStoreID();
        //cam ai check
        producer.sendHeartbeat(tableID);
        //gửi thông tin trận đấu cho py
        InformationRequest req = producer.mapInformation(response);
        producer.sendEvent(tableID, req);

        //set table status: inUse
        billiardTableService.setInUse(String.valueOf(response.getBilliardTableID()));

        //add info into notification
        NotificationResponse tmp = newNoti(
                response.getBilliardMatchID(),
                "Match " + response.getBilliardMatchID() + " is created on table " + tableCode,
                NotificationType.created
        );

        //send to fcm & websocket
        //send match information to mobile
        webSocketService.sendToWebSocket(
                WebSocketTopic.NOTI_NOTIFICATION.getValue() + tableID,
                new WebsocketReq(WSFCMCode.MATCH_START, response)
        );
        realTimeNotification.sendRealTimeNotification(
                tmp.getMessage(),
                WebSocketTopic.NOTI_NOTIFICATION,
                tableID,
                WSFCMCode.NOTIFICATION
        );
        //send create msg to admin dashboard => which table is in use
        webSocketService.sendToWebSocket(
                WebSocketTopic.DASHBOARD.getValue() + storeID,
                new WebsocketReq(WSFCMCode.MATCH_START, response)
        );

        return ResponseObject.builder()
                .status(1000)
                .message("Create new Match successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Update billiard match with unified parameters",
            description = "Unified API that combines all PUT operations from v1 controller")
    @PutMapping
    public ResponseObject updateBilliardMatch(@RequestBody BilliardMatchV3UpdateRequest request) {
        try {
            MatchUpdateType updateType = request.getUpdateType();
            if (updateType == null) {
                return ResponseObject.builder()
                        .status(400)
                        .message("updateType is required. Valid values: update, score, cancel, complete, manual")
                        .build();
            }

            switch (updateType) {
                case update:
                    return handleUpdateMatch(request);
                case score:
                    return handleUpdateScore(request);
                case cancel:
                    return handleCancel(request);
                case complete:
                    return handleComplete(request);
                case manual:
                    return handleManualUpdate(request);
                default:
                    return ResponseObject.builder()
                            .status(400)
                            .message("Invalid updateType. Valid values: update, score, cancel, complete, manual")
                            .build();
            }
        } catch (Exception e) {
            log.error("Error in updateBilliardMatch: ", e);
            return ResponseObject.builder()
                    .status(500)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }

    private ResponseObject handleUpdateMatch(BilliardMatchV3UpdateRequest request) {
        if (request.getMatchID() == null) {
            return ResponseObject.builder()
                    .status(400)
                    .message("Match ID is required for update operation")
                    .build();
        }

        BilliardMatchUpdateRequest updateRequest = new BilliardMatchUpdateRequest();
        updateRequest.setWinner(request.getWinner());
        updateRequest.setStatus(request.getStatus());

        return ResponseObject.builder()
                .status(1000)
                .message("Update Match information successfully")
                .data(billiardMatchService.updateMatch(request.getMatchID(), updateRequest))
                .build();
    }

    private ResponseObject handleUpdateScore(BilliardMatchV3UpdateRequest request) {
        if (request.getMatchID() == null || request.getTeamID() == null || request.getDelta() == null) {
            return ResponseObject.builder()
                    .status(400)
                    .message("matchID, teamID, and delta are required for score update")
                    .build();
        }

        ScoreRequest scoreRequest = new ScoreRequest();
        scoreRequest.setMatchID(request.getMatchID());
        scoreRequest.setTeamID(request.getTeamID());
        scoreRequest.setDelta(request.getDelta());

        BilliardMatchResponse rs = billiardMatchService.updateScore(scoreRequest);

        if (rs.getStatus().equals(MatchStatus.completed)) {

            //free matchID & gameSetID in queue
            eventProcessorService.resetMatchState(rs.getBilliardMatchID());
            List<Integer> gameSetIDList = rs.getSets()
                    .stream()
                    .map(GameSetResponse::getGameSetID)
                    .toList();
            eventProcessorService.resetGameSetState(gameSetIDList);

            //free table
            billiardTableService.setAvailable(String.valueOf(rs.getBilliardTableID()));

        }


        return ResponseObject.builder()
                .status(1000)
                .message("Update score successfully")
                .data(rs)
                .build();
    }

    private ResponseObject handleCancel(BilliardMatchV3UpdateRequest request) {
        if (request.getMatchID() == null || request.getForfeitTeamID() == null) {
            return ResponseObject.builder()
                    .status(400)
                    .message("Match ID and forfeitTeamID are required for forfeit operation")
                    .build();
        }

        BilliardMatchResponse response = billiardMatchService.cancelMatch(request.getMatchID(), request.getForfeitTeamID());
        //free table
        billiardTableService.setAvailable(String.valueOf(response.getBilliardTableID()));

        return ResponseObject.builder()
                .status(1000)
                .message("Team with ID " + request.getForfeitTeamID() + " has been forfeited")
                .data(response)
                .build();
    }

    private ResponseObject handleComplete(BilliardMatchV3UpdateRequest request) {
        if (request.getMatchID() == null) {
            return ResponseObject.builder()
                    .status(400)
                    .message("Match ID is required for complete operation")
                    .build();
        }

        BilliardMatch match = billiardMatchService.findMatchByID(request.getMatchID());
        //free table
        billiardTableService.setAvailable(String.valueOf(match.getBillardTable().getBillardTableID()));

        return ResponseObject.builder()
                .status(1000)
                .message("Match is currently completed")
                .data(billiardMatchService.completeMatch(request.getMatchID()))
                .build();
    }

    private ResponseObject handleManualUpdate(BilliardMatchV3UpdateRequest request) {
        if (request.getMatchID() == null) {
            return ResponseObject.builder()
                    .status(400)
                    .message("Match ID is required for manual update operation")
                    .build();
        }

        BilliardMatch match = billiardMatchService.findMatchByID(request.getMatchID());
        String manualMatch = billiardMatchService.startMatch(match.getBilliardMatchID());
        ResponseObject tmp = gameSetController.manualUpdateSet(match.getBilliardMatchID());

        return ResponseObject.builder()
                .status(1000)
                .message("Match's information updated manually successfully")
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseObject deleteMatch(@PathVariable Integer id) {
        return ResponseObject.builder()
                .status(1000)
                .message("Match with ID " + id + " has been deleted")
                .data(billiardMatchService.delete(id))
                .build();
    }

    @DeleteMapping()
    public void deleteAll() {
        billiardMatchService.deleteAll();
    }

    private NotificationResponse newNoti(int matchID, String msg, NotificationType type) {
        //add info into notification
        return notificationService.saveNotification(
                new NotificationRequest(
                        matchID,
                        msg,
                        type
                ));
    }


}
