package com.scorelens.DTOs.Response;

import com.scorelens.Enums.ResultStatus;
import lombok.*;

import java.time.LocalDateTime;

///**
// * DTO for {@link com.scorelens.Entity.Player}
// */
@Getter
@Setter
public class PlayerResponse {
    private Integer teamID;
    private int playerID;
    private String name;
    private int totalScore;
    private String customerID;
    private ResultStatus status; //win, lose, draw, pending
    private LocalDateTime createAt;
}