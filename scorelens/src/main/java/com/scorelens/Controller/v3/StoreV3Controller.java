package com.scorelens.Controller.v3;

import com.scorelens.DTOs.Request.PageableRequestDto;
import com.scorelens.DTOs.Request.StoreRequest;
import com.scorelens.DTOs.Request.StoreV3UpdateRequest;
import com.scorelens.Entity.ResponseObject;
import com.scorelens.Service.StoreService;
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
@Tag(name = "Store V3", description = "Unified Store API")
@RestController
@RequestMapping("v3/stores")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StoreV3Controller {
    
    @Autowired
    StoreService storeService;

    @GetMapping
    public ResponseObject getStores(
            @Parameter(description = "Query type: all, byId",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"all", "byId"}
                    ))
            @RequestParam(defaultValue = "all") String queryType,
            
            @Parameter(description = "Store ID (required for queryType=byId)")
            @RequestParam(required = false) String storeId,
            
            @Parameter(description = "Page number (1-based)", required = true)
            @RequestParam(required = false, defaultValue = "1") Integer page,
            
            @Parameter(description = "Page size", required = true)
            @RequestParam(required = false, defaultValue = "10") Integer size,

            @Parameter(description = "Sort field",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"name", "storeID"}
                    ))
            @RequestParam(defaultValue = "name") String sortBy,

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
        if (storeId != null && !storeId.isEmpty()) filters.put("storeId", storeId);

        return ResponseObject.builder()
                .status(1000)
                .message("Store List")
                .data(storeService.getAll(req, filters))
                .build();
    }

    @GetMapping("data/{id}")
    public ResponseObject getStoreData(@PathVariable String id) {
        return ResponseObject.builder()
                .status(1000)
                .message("Store found")
                .data(storeService.getStoreData(id))
                .build();
    }

    @PostMapping
    public ResponseObject addStore(@RequestBody StoreRequest storeRequest) {
        return ResponseObject.builder()
                .status(1000)
                .message("New Store is created")
                .data(storeService.createStore(storeRequest))
                .build();
    }
    
    @Operation(summary = "Update store with unified parameters", 
               description = "Unified API that combines all PUT operations from v1 controller")
    @PutMapping
    public ResponseObject updateStore(@RequestBody StoreV3UpdateRequest request) {
        try {
            String updateType = request.getUpdateType();
            if (updateType == null) {
                return ResponseObject.builder()
                        .status(400)
                        .message("updateType is required. Valid values: update, status")
                        .build();
            }
            
            if (request.getId() == null || request.getId().trim().isEmpty()) {
                return ResponseObject.builder()
                        .status(400)
                        .message("Store ID is required")
                        .build();
            }
            
            switch (updateType.toLowerCase()) {
                case "update":
                    return handleUpdateStore(request);
                case "status":
                    return handleUpdateStatus(request);
                default:
                    return ResponseObject.builder()
                            .status(400)
                            .message("Invalid updateType. Valid values: update, status")
                            .build();
            }
        } catch (Exception e) {
            log.error("Error in updateStore: ", e);
            return ResponseObject.builder()
                    .status(500)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }

    private ResponseObject handleUpdateStore(StoreV3UpdateRequest request) {
        StoreRequest storeRequest = new StoreRequest();
        storeRequest.setName(request.getName());
        storeRequest.setAddress(request.getAddress());
        storeRequest.setStatus(request.getStatus());
        storeRequest.setDescription(request.getDescription());

        return ResponseObject.builder()
                .status(1000)
                .message("Store is updated")
                .data(storeService.updateStore(request.getId(), storeRequest))
                .build();
    }

    private ResponseObject handleUpdateStatus(StoreV3UpdateRequest request) {
        if (request.getStatusValue() == null || request.getStatusValue().trim().isEmpty()) {
            return ResponseObject.builder()
                    .status(400)
                    .message("Status value is required for status update operation")
                    .build();
        }

        return ResponseObject.builder()
                .status(1000)
                .message("Store's status is updated")
                .data(storeService.updateStore(request.getId(), request.getStatusValue()))
                .build();
    }
}
