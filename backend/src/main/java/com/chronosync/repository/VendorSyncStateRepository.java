package com.chronosync.repository;

import com.chronosync.model.VendorSyncState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorSyncStateRepository extends JpaRepository<VendorSyncState, Long> {
}