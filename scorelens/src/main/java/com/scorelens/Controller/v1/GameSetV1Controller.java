package com.scorelens.Controller.v1;

import com.scorelens.DTOs.Request.GameSetCreateRequest;
import com.scorelens.DTOs.Request.GameSetUpdateRequest;
import com.scorelens.DTOs.Response.GameSetResponse;
import com.scorelens.Entity.GameSet;
import com.scorelens.Entity.ResponseObject;
import com.scorelens.Service.BilliardMatchService;
import com.scorelens.Service.GameSetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Game Set", description = "Manage Game Set")
@RestController
@RequestMapping("v1/gamesets")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GameSetV1Controller {

    @Autowired
    private GameSetService gameSetService;

    BilliardMatchService billiardMatchService;

    @GetMapping("/{id}")
    public ResponseObject getById(@PathVariable Integer id) {
        return ResponseObject.builder()
                        .status(1000)
                        .message("Get GameSet information succesfully")
                        .data(gameSetService.getById(id))
                        .build();
    }

    @GetMapping("match/{id}")
    public ResponseObject getByMatch(@PathVariable Integer id) {
        return ResponseObject.builder()
                .status(1000)
                .message("Get Game Sets information succesfully")
                .data(gameSetService.getByMatchID(id))
                .build();
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

    @PutMapping("/cancel/{id}")
    public ResponseObject updateSet(@PathVariable Integer id) {
        return ResponseObject.builder()
                .status(1000)
                .message("Cancel GameSet sucessfully")
                .data(gameSetService.cancel(id))
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

