package com.scorelens.Repository;

import com.scorelens.Entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PlayerRepo extends JpaRepository<Player, Integer>, JpaSpecificationExecutor<Player> {
    List<Player> findByTeam_TeamID(int id);
}
