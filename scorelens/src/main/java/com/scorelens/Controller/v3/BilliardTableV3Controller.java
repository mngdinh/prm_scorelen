package com.scorelens.Controller.v3;

import com.scorelens.DTOs.Request.BilliardTableRequest;
import com.scorelens.DTOs.Request.BilliardTableV3UpdateRequest;
import com.scorelens.DTOs.Request.PageableRequestDto;
import com.scorelens.DTOs.Response.BilliardMatchResponse;
import com.scorelens.DTOs.Response.BilliardTableResponse;
import com.scorelens.DTOs.Response.PageableResponseDto;
import com.scorelens.DTOs.Response.StoreResponse;
import com.scorelens.Entity.ResponseObject;
import com.scorelens.Enums.TableStatus;
import com.scorelens.Service.BilliardMatchService;
import com.scorelens.Service.BilliardTableService;
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
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "Billiard Table V3", description = "Unified Billiard Table API")
@RestController
@RequestMapping("v3/tables")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BilliardTableV3Controller {

    @Autowired
    BilliardTableService billiardTableService;

    @Autowired
    StoreService storeService;

    @Autowired
    BilliardMatchService billiardMatchService;

    @GetMapping
    public ResponseObject getBilliardTables(
            @Parameter(description = "Query type: all, byId, byStore",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"all", "byId", "byStore"}
                    ))
            @RequestParam(required = true, defaultValue = "all") String queryType,

            @Parameter(description = "Table ID (required for queryType=byId)")
            @RequestParam(required = false) String tableId,

            @Parameter(description = "Store ID (required for queryType=byStore)")
            @RequestParam(required = false) String storeId,

            @Parameter(description = "Status type: available, inUse, underMaintainance",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"available", "inUse", "underMaintainance", "null"}
                    ))
            @RequestParam(defaultValue = "null") String status,

            @Parameter(description = "Page number (1-based)", required = true)
            @RequestParam(required = false, defaultValue = "1") Integer page,

            @Parameter(description = "Page size", required = true)
            @RequestParam(required = false, defaultValue = "10") Integer size,

            @Parameter(description = "Sort By",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"tableCode", "billardTableID"}
                    ))
            @RequestParam(defaultValue = "tableCode") String sortBy,

            @Parameter(description = "Sort Direction",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"desc", "asc"}
                    ))
            @RequestParam(required = true, defaultValue = "desc") String sortDirection

    ) {
        PageableRequestDto req = PageableRequestDto.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Map<String, Object> filters = new HashMap<>();
        filters.put("queryType", queryType);
        if (tableId != null && !"null".equals(tableId)) filters.put("tableId", tableId);
        if (storeId != null && !"null".equals(storeId)) filters.put("storeId", storeId);
        if (status != null && !"null".equals(status)) filters.put("status", status);


        PageableResponseDto<BilliardTableResponse> r = billiardTableService.getAll(req, filters);


        return ResponseObject.builder()
                .status(1000)
                .message("Get data successfully")
                .data(r)
                .build();

    }

    @Operation(summary = "Update billiard table with unified parameters",
            description = "Unified API that combines all PUT operations from v1 controller")
    @PutMapping
    public ResponseObject updateBilliardTable(@RequestBody BilliardTableV3UpdateRequest request) {
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
                        .message("Table ID is required")
                        .build();
            }

            switch (updateType.toLowerCase()) {
                case "update":
                    return handleUpdateTable(request);
                case "status":
                    return handleUpdateStatus(request);
                default:
                    return ResponseObject.builder()
                            .status(400)
                            .message("Invalid updateType. Valid values: update, status")
                            .build();
            }
        } catch (Exception e) {
            log.error("Error in updateBilliardTable: ", e);
            return ResponseObject.builder()
                    .status(500)
                    .message("Internal server error: " + e.getMessage())
                    .build();
        }
    }

    private ResponseObject handleUpdateTable(BilliardTableV3UpdateRequest request) {
        BilliardTableRequest tableRequest = new BilliardTableRequest();
        tableRequest.setTableType(request.getTableType());
        tableRequest.setName(request.getName());
        tableRequest.setDescription(request.getDescription());
        tableRequest.setStatus(request.getStatus());
        tableRequest.setCameraUrl(request.getCameraUrl());
        tableRequest.setActive(request.isActive());
        tableRequest.setStoreID(request.getStoreID());

        return ResponseObject.builder()
                .status(1000)
                .message("Table is updated")
                .data(billiardTableService.updateBilliardTable(request.getId(), tableRequest))
                .build();
    }

    private ResponseObject handleUpdateStatus(BilliardTableV3UpdateRequest request) {
        if (request.getStatusValue() == null) {
            return ResponseObject.builder()
                    .status(400)
                    .message("Status value is required for status update operation")
                    .build();
        }

        return ResponseObject.builder()
                .status(1000)
                .message("Table is updated")
                .data(billiardTableService.updateBilliardTable(request.getId(), request.getStatusValue()))
                .build();
    }

    @PostMapping
    public ResponseObject addTable(@RequestBody BilliardTableRequest request) {
        return ResponseObject.builder()
                .status(1000)
                .message("New table is created")
                .data(billiardTableService.createBilliardTable(request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseObject deleteTable(@PathVariable String id) {
        String tableCode = billiardTableService.findBilliardTableById(id).getTableCode();
        log.info("Table {} is deleted", tableCode);
        return ResponseObject.builder()
                .status(1000)
                .message(String.format("Table with code %s is deleted", tableCode))
                .data(billiardTableService.deleteBilliardTable(id))
                .build();
    }
}
