package com.rentacar;

import com.rentacar.model.Member;
import com.rentacar.repository.MemberRepository;
import com.rentacar.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(MemberService.class)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void createMember_SavesAndReturnsMember() {
        Member member = new Member();
        member.setName("Alihan Kerimov");
        member.setAddress("Istanbul, Turkey");
        member.setEmail("ali@example.com");
        member.setPhone("+905551234567");
        member.setDrivingLicenseNumber("DL1234567");

        Member saved = memberService.createMember(member);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Alihan Kerimov");
        assertThat(saved.getEmail()).isEqualTo("ali@example.com");
        assertThat(saved.getDrivingLicenseNumber()).isEqualTo("DL1234567");
    }

    @Test
    void createMember_ThrowsException_WhenEmailExists() {
        Member existing = new Member();
        existing.setName("Existing User");
        existing.setEmail("existing@example.com");
        existing.setDrivingLicenseNumber("DL111111");
        memberRepository.save(existing);

        Member duplicate = new Member();
        duplicate.setName("New User");
        duplicate.setEmail("existing@example.com"); // Same email
        duplicate.setDrivingLicenseNumber("DL222222");

        assertThatThrownBy(() -> memberService.createMember(duplicate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("email");
    }

    @Test
    void createMember_ThrowsException_WhenDrivingLicenseExists() {
        Member existing = new Member();
        existing.setName("Existing User");
        existing.setEmail("user1@example.com");
        existing.setDrivingLicenseNumber("DL111111");
        memberRepository.save(existing);

        Member duplicate = new Member();
        duplicate.setName("New User");
        duplicate.setEmail("user2@example.com");
        duplicate.setDrivingLicenseNumber("DL111111"); // Same driving license

        assertThatThrownBy(() -> memberService.createMember(duplicate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("driving license");
    }

    @Test
    void getMemberById_ReturnsExistingMember() {
        Member member = new Member();
        member.setName("Test User");
        member.setEmail("test@example.com");
        member.setDrivingLicenseNumber("DL999999");
        Member saved = memberRepository.save(member);

        Member found = memberService.getMemberById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("Test User");
    }

    @Test
    void getMemberById_ThrowsException_WhenNotFound() {
        assertThatThrownBy(() -> memberService.getMemberById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Member not found");
    }

    @Test
    void getMemberByEmail_ReturnsCorrectMember() {
        Member member = new Member();
        member.setName("Email Test");
        member.setEmail("emailtest@example.com");
        member.setDrivingLicenseNumber("DL888888");
        memberRepository.save(member);

        Member found = memberService.getMemberByEmail("emailtest@example.com");

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("emailtest@example.com");
        assertThat(found.getName()).isEqualTo("Email Test");
    }

    @Test
    void getAllMembers_ReturnsAllMembers() {
        Member member1 = new Member();
        member1.setName("User One");
        member1.setEmail("one@example.com");
        member1.setDrivingLicenseNumber("DL111111");

        Member member2 = new Member();
        member2.setName("User Two");
        member2.setEmail("two@example.com");
        member2.setDrivingLicenseNumber("DL222222");

        memberRepository.saveAll(List.of(member1, member2));

        List<Member> members = memberService.getAllMembers();

        assertThat(members).hasSize(2);
        assertThat(members).extracting(Member::getName)
                .contains("User One", "User Two");
    }

    @Test
    void updateMember_UpdatesCorrectly() {
        Member member = new Member();
        member.setName("Old Name");
        member.setEmail("old@example.com");
        member.setDrivingLicenseNumber("DL123456");
        member.setAddress("Old Address");
        member.setPhone("Old Phone");
        Member saved = memberRepository.save(member);

        Member updatedDetails = new Member();
        updatedDetails.setName("New Name");
        updatedDetails.setAddress("New Address");
        updatedDetails.setPhone("New Phone");

        Member updated = memberService.updateMember(saved.getId(), updatedDetails);

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getAddress()).isEqualTo("New Address");
        assertThat(updated.getPhone()).isEqualTo("New Phone");
        // Email and driving license should remain unchanged
        assertThat(updated.getEmail()).isEqualTo("old@example.com");
        assertThat(updated.getDrivingLicenseNumber()).isEqualTo("DL123456");
    }

    @Test
    void deleteMember_ReturnsTrue_WhenNoActiveReservations() {
        Member member = new Member();
        member.setName("To Delete");
        member.setEmail("delete@example.com");
        member.setDrivingLicenseNumber("DL555555");
        Member saved = memberRepository.save(member);

        boolean result = memberService.deleteMember(saved.getId());

        assertThat(result).isTrue();
        assertThat(memberRepository.findById(saved.getId())).isEmpty();
    }
}