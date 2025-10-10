package com.scorelens.Service.Interface;

import com.scorelens.DTOs.Request.BilliardTableRequest;
import com.scorelens.DTOs.Response.BilliardTableResponse;
import com.scorelens.Entity.BilliardTable;
import com.scorelens.Enums.TableStatus;

import java.util.List;

public interface IBilliardTableService {
    BilliardTableResponse createBilliardTable(BilliardTableRequest request);
    List<BilliardTableResponse> getAllBilliardTables();
    BilliardTableResponse findBilliardTableById(String billardTableID);
    BilliardTableResponse updateBilliardTable(String billiardTableID, BilliardTableRequest request);
    BilliardTableResponse updateBilliardTable(String billiardTableID, TableStatus status);
    void setInUse(String billardTableID);
    void setAvailable(String billardTableID);
    void setUnderMaintenance(String billardTableID);
    boolean deleteBilliardTable(String billiardTableID);
    List<BilliardTableResponse> getTablesByStore(String storeID);

}
