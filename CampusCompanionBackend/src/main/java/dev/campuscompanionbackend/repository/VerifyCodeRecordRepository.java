package dev.campuscompanionbackend.repository;

import dev.campuscompanionbackend.entity.User;
import dev.campuscompanionbackend.entity.VerifyCodeRecord;
import dev.campuscompanionbackend.enums.VerifyCodeRecordStatus;
import dev.campuscompanionbackend.enums.VerifyCodeRecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface VerifyCodeRecordRepository extends JpaRepository<VerifyCodeRecord, Long> {

    Optional<VerifyCodeRecord> findFirstByEmailAndTypeAndStatusOrderByCreatedAtDesc(
            String email,
            VerifyCodeRecordType type,
            VerifyCodeRecordStatus status
    );
}
