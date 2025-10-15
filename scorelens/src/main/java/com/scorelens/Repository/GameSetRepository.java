package com.scorelens.Repository;

import com.scorelens.Entity.GameSet;
import com.scorelens.Entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameSetRepository extends JpaRepository<GameSet, Integer>, JpaSpecificationExecutor<GameSet> {
    List<GameSet> findByBilliardMatch_BilliardMatchID(Integer id);
}

