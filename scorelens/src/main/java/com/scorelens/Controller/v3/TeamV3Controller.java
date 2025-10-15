package com.scorelens.Controller.v3;

import com.scorelens.DTOs.Request.PageableRequestDto;
import com.scorelens.DTOs.Request.TeamCreateRequest;
import com.scorelens.DTOs.Request.TeamUpdateRequest;
import com.scorelens.DTOs.Request.TeamV3Request;
import com.scorelens.DTOs.Response.TeamResponse;
import com.scorelens.Entity.ResponseObject;
import com.scorelens.Service.TeamService;
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
@Tag(name = "Team V3", description = "Unified Team API")
@RestController
@RequestMapping("v3/teams")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeamV3Controller {
    
    @Autowired
    TeamService teamService;

    @GetMapping
    public ResponseObject getTeams(
            @Parameter(description = "Query type: all, byId, byMatch",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"all", "byId", "byMatch"}
                    ))
            @RequestParam(defaultValue = "all") String queryType,
            
            @Parameter(description = "Team ID (required for queryType=byId)")
            @RequestParam(required = false) Integer teamId,
            
            @Parameter(description = "Match ID (required for queryType=byMatch)")
            @RequestParam(required = false) Integer matchId,

            @Parameter(description = "Page number (1-based)", required = true)
            @RequestParam(required = false, defaultValue = "1") Integer page,

            @Parameter(description = "Page size", required = true)
            @RequestParam(required = false, defaultValue = "10") Integer size,

            @Parameter(description = "Sort field",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"createAt", "teamID"}
                    ))
            @RequestParam(defaultValue = "createAt") String sortBy,

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
        if (teamId != null) filters.put("teamId", teamId);
        if (matchId != null) filters.put("matchId", matchId);

        return ResponseObject.builder()
                .status(1000)
                .message("Store List")
                .data(teamService.getAll(req, filters))
                .build();
    }

    @PostMapping
    public ResponseObject createTeam(@RequestBody TeamCreateRequest request) {
        return ResponseObject.builder()
                .status(1000)
                .message("Create new Team successfully")
                .data(teamService.addTeam(request))
                .build();
    }

    @PutMapping("/{id}")
    public ResponseObject updateTeam(@PathVariable Integer id, @RequestBody TeamUpdateRequest request) {
        return ResponseObject.builder()
                .status(1000)
                .message("Update Team information successfully")
                .data(teamService.updateTeam(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseObject deleteTeam(@PathVariable Integer id) {
        return ResponseObject.builder()
                .status(1000)
                .message("Team with ID " + id + " has been deleted")
                .data(teamService.delete(id))
                .build();
    }
}
