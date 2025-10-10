package com.scorelens.Entity;

import com.scorelens.Enums.TableStatus;
import com.scorelens.Enums.TableType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BilliardTable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "billardTableID", nullable = false, length = 50)
    private String billardTableID;

    @Column(name = "tableCode", length = 10)
    private String tableCode; //ex: bàn pool mrsung số 1, PMR01

    @Enumerated(EnumType.STRING)
    @Column(name = "tableType", length = 10)
    private TableType tableType;

    @Column(name = "name", length = 50)
    private String name;

    @Lob
    @Column(name = "description", length = 50)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private TableStatus status;

    @Column(name = "qrCode", length = 255)
    private String qrCode;

    @Column(name="cameraUrl", length = 255)
    private String cameraUrl;

    @Column(name = "isActive")
    private boolean isActive; //0: available, 1: unavailable

    @ManyToOne
    @JoinColumn(name = "storeID", nullable = false)
    private Store store;

    @OneToMany(mappedBy = "billardTable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BilliardMatch> matches = new ArrayList<>();

    @OneToMany(mappedBy = "billardTable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FCMToken> tokens = new ArrayList<>();


}
