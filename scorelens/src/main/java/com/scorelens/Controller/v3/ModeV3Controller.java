package com.scorelens.Controller.v3;

import com.scorelens.DTOs.Request.ModeRequest;
import com.scorelens.DTOs.Request.ModeV3Request;
import com.scorelens.DTOs.Request.PageableRequestDto;
import com.scorelens.DTOs.Response.ModeResponse;
import com.scorelens.Entity.ResponseObject;
import com.scorelens.Service.ModeService;
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
@Tag(name = "Mode V3", description = "Unified Mode API")
@RestController
@RequestMapping("v3/modes")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModeV3Controller {
    
    @Autowired
    ModeService modeService;

    @GetMapping
    public ResponseObject getModes(
            @Parameter(description = "Query type: all, byId",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"all", "byId"}
                    ))
            @RequestParam(defaultValue = "all") String queryType,
            
            @Parameter(description = "Mode ID (required for queryType=byId)")
            @RequestParam(required = false) Integer modeId,

            @Parameter(description = "Page number (1-based)", required = true)
            @RequestParam(required = false, defaultValue = "1") Integer page,

            @Parameter(description = "Page size", required = true)
            @RequestParam(required = false, defaultValue = "10") Integer size,

            @Parameter(description = "Sort field",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"modeID", "name"}
                    ))
            @RequestParam(required = false, defaultValue = "modeID") String sortBy,

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
        if (modeId != null) filters.put("modeId", modeId);

        return ResponseObject.builder()
                .status(1000)
                .message("Get modes")
                .data(modeService.getAll(req, filters))
                .build();
    }

    @PostMapping
    public ResponseObject createMode(@RequestBody ModeRequest request) {
        return ResponseObject.builder()
                .status(1000)
                .message("Create new Mode successfully")
                .data(modeService.createMode(request))
                .build();
    }

    @PutMapping("/{id}")
    public ResponseObject updateMode(@PathVariable Integer id, @RequestBody ModeRequest request) {
        return ResponseObject.builder()
                .status(1000)
                .message("Update Mode information successfully")
                .data(modeService.updateMode(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseObject delete(@PathVariable Integer id) {
        return ResponseObject.builder()
                .status(1000)
                .message("Mode with ID " + id + " has been deleted")
                .data(modeService.delete(id))
                .build();
    }
}
