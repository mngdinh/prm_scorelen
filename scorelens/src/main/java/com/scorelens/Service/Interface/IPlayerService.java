package com.scorelens.Service.Interface;

import com.scorelens.DTOs.Request.CustomerSaveRequest;
import com.scorelens.DTOs.Request.PlayerCreateRequest;
import com.scorelens.DTOs.Request.PlayerUpdateRequest;
import com.scorelens.DTOs.Response.PlayerResponse;
import com.scorelens.Entity.Player;

import java.util.List;

public interface IPlayerService {
    List<PlayerResponse> getAllPlayers();
    List<PlayerResponse> getByTeam(Integer id);
    PlayerResponse getPlayerById(int id);

    Player createPlayer(PlayerCreateRequest request);

    PlayerResponse updatePlayer(Integer id, PlayerUpdateRequest request);
    Integer delete(Integer id);
    void deletePlayer(int id);
    PlayerResponse updateCustomer(Integer id, CustomerSaveRequest request);

}
