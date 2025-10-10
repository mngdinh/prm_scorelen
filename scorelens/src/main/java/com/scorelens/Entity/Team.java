package com.scorelens.Entity;

import com.scorelens.Enums.ResultStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teamID")
    private Integer teamID;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "totalMember")
    private Integer totalMember;

    @Column(name = "totalScore")
    private Integer totalScore;

    @Column(name = "createAt")
    private LocalDateTime createAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ResultStatus status; //-- win, lose, draw, pending

    @ManyToOne
    @JoinColumn(name = "billiardMatchID", nullable = false)
    private BilliardMatch billiardMatch;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players = new ArrayList<>();

    public void addPlayer(Player player) {
        players.add(player);
        player.setTeam(this);
    }

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamSet> tss = new ArrayList<>();

    public void addTeamSet(TeamSet teamSet) {
        tss.add(teamSet);
        teamSet.setTeam(this);
    }
}
