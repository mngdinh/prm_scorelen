package com.scorelens.Controller.v3;

import com.scorelens.DTOs.Request.GameSetCreateRequest;
import com.scorelens.DTOs.Request.GameSetUpdateRequest;
import com.scorelens.DTOs.Request.GameSetV3Request;
import com.scorelens.DTOs.Request.PageableRequestDto;
import com.scorelens.DTOs.Response.GameSetResponse;
import com.scorelens.Entity.GameSet;
import com.scorelens.Entity.ResponseObject;
import com.scorelens.Service.GameSetService;
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
@Tag(name = "Game Set V3", description = "Unified Game Set API")
@RestController
@RequestMapping("v3/gamesets")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GameSetV3Controller {
    
    @Autowired
    GameSetService gameSetService;

    @GetMapping
    public ResponseObject getGameSets(
            @Parameter(description = "Query type: byId, byMatch",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"byMatch", "byId"}
                    ))
            @RequestParam(defaultValue = "all") String queryType,
            
            @Parameter(description = "Game Set ID (required for queryType=byId)")
            @RequestParam(required = false) Integer gameSetId,
            
            @Parameter(description = "Match ID (required for queryType=byMatch)")
            @RequestParam(required = false) Integer matchId,
            
            @Parameter(description = "Page number (1-based)", required = true)
            @RequestParam(required = false, defaultValue = "1") Integer page,
            
            @Parameter(description = "Page size", required = true)
            @RequestParam(required = false, defaultValue = "10") Integer size,
            
            @Parameter(description = "Sort field",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"startTime", "endTime"}
                    ))
            @RequestParam(required = false, defaultValue = "startTime") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"desc", "asc"}
                    ))
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        try {
            PageableRequestDto req = PageableRequestDto.builder()
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();

            Map<String, Object> filters = new HashMap<>();
            filters.put("queryType", queryType);
            if (gameSetId != null) filters.put("gameSetId", gameSetId);
            if (matchId != null) filters.put("matchId", matchId);

            return ResponseObject.builder()
                    .status(1000)
                    .message("Success")
                    .data(gameSetService.getAll(req, filters))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error in getGameSets: ", e);
            return ResponseObject.builder()
                    .status(500)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping
    public ResponseObject createSet(@RequestBody GameSetCreateRequest request) {
        return ResponseObject.builder()
                .status(1000)
                .message("Create GameSet sucessfully")
                .data(gameSetService.createSet(request))
                .build();
    }

    @PutMapping("/{id}")
    public ResponseObject updateSet(@PathVariable Integer id, @RequestBody GameSetUpdateRequest request) {
        return ResponseObject.builder()
                .status(1000)
                .message("Update GameSet sucessfully")
                .data(gameSetService.updateSet(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseObject deleteSet(@PathVariable Integer id) {
        return ResponseObject.builder()
                .status(1000)
                .message("GameSet with ID " + id + " has been deleted")
                .data(gameSetService.delete(id))
                .build();
    }


    public ResponseObject manualUpdateSet(Integer billiardMatchID) {
        List<GameSet> list = gameSetService.getByMatch(billiardMatchID);
        for (GameSet gameSet : list) {
            if (gameSet.getGameSetNo() == 1){
                GameSet tmp = gameSetService.startSet(gameSet.getGameSetID());
            }
        }
        return ResponseObject.builder()
                .status(1000)
                .message("Manual Update GameSet successfully")
                .data(true)
                .build();
    }

}
