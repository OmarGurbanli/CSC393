package com.rentacar.service;

import com.rentacar.model.Member;
import com.rentacar.model.ReservationStatus;
import com.rentacar.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found with email: " + email));
    }

    public Member getMemberByDrivingLicense(String drivingLicenseNumber) {
        return memberRepository.findByDrivingLicenseNumber(drivingLicenseNumber)
                .orElseThrow(() -> new RuntimeException("Member not found with driving license: " + drivingLicenseNumber));
    }

    public Member createMember(Member member) {
        if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
            throw new RuntimeException("Member with email " + member.getEmail() + " already exists");
        }
        if (memberRepository.findByDrivingLicenseNumber(member.getDrivingLicenseNumber()).isPresent()) {
            throw new RuntimeException("Member with driving license " + member.getDrivingLicenseNumber() + " already exists");
        }

        return memberRepository.save(member);
    }

    public Member updateMember(Long id, Member memberDetails) {
        Member member = getMemberById(id);
        member.setName(memberDetails.getName());
        member.setAddress(memberDetails.getAddress());
        member.setPhone(memberDetails.getPhone());

        return memberRepository.save(member);
    }

    public boolean deleteMember(Long id) {
        Member member = getMemberById(id);

        boolean hasActiveReservations = member.getReservations().stream()
                .anyMatch(reservation -> reservation.getStatus() == ReservationStatus.ACTIVE);

        if (hasActiveReservations) {
            return false;
        }

        memberRepository.delete(member);
        return true;
    }
}