package com.scorelens.Repository;

import com.scorelens.Entity.BilliardTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BilliardTableRepo extends JpaRepository<BilliardTable, String>, JpaSpecificationExecutor<BilliardTable> {
    List<BilliardTable> findAllByStore_StoreID(String storeID);
}
