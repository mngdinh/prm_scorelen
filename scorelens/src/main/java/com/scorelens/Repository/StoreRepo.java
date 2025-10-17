package com.scorelens.Repository;

import com.scorelens.Entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreRepo extends JpaRepository<Store, String>, JpaSpecificationExecutor<Store> {
    boolean existsByname(String name);

    @Query("SELECT p.customerID, COUNT(p) AS cnt " +
            "FROM Player p " +
            "WHERE p.customerID IS NOT NULL AND p.team.billiardMatch.billardTable.store.storeID = :storeId " +
            "GROUP BY p.customerID " +
            "ORDER BY cnt DESC")
    List<Object[]> countMatchesByCustomerInStore(@Param("storeId") String storeId);

}
