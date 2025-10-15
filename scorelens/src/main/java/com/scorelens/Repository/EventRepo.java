package com.scorelens.Repository;

import com.scorelens.DTOs.Response.EventResponse;
import com.scorelens.Entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EventRepo extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {
    List<Event> findAllByGameSet_GameSetID(int gameSetID);
    List<Event> findAllByPlayer_PlayerID(int playerID);
    List<Event> findAllByGameSet_GameSetIDAndPlayer_PlayerID(int gameSetID, int playerID);
}
