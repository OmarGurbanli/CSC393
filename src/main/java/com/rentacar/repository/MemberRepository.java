package com.rentacar.repository;

import com.rentacar.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByDrivingLicenseNumber(String drivingLicenseNumber);
}