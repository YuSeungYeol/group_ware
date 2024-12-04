package com.ware.spring.member.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import com.ware.spring.member.domain.Distributor;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.domain.MemberDto;
import com.ware.spring.member.domain.Rank;
import com.ware.spring.member.repository.DistributorRepository;
import com.ware.spring.member.repository.MemberRepository;
import com.ware.spring.member.repository.RankRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final DistributorRepository distributorRepository;
    private final RankRepository rankRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public MemberService(MemberRepository memberRepository, DistributorRepository distributorRepository, RankRepository rankRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.distributorRepository = distributorRepository;
        this.rankRepository = rankRepository;
        this.passwordEncoder = passwordEncoder;

    }
    
    public boolean isPasswordChanged(MemberDto memberDto) {
        String currentPassword = memberRepository.findPasswordById(memberDto.getMem_no());

        if (memberDto.getMem_pw() == null || memberDto.getMem_pw().isEmpty()) {
            return false;
        }
        return !passwordEncoder.matches(memberDto.getMem_pw(), currentPassword);
    }
    /**
     * 사원번호 자동생성 로직
     * 기술: 문자열 조작, Random
     * 설명: 사용자의 소속된 지점 번호, 등록일, 무작위 2자리 숫자를 조합하여 사원번호를 생성합니다.
     */
    public String generateEmpNo(MemberDto memberDto) {
        String distributorNo = String.format("%02d", memberDto.getDistributor_no()); 
        String memRegDate = memberDto.getMem_reg_date().format(DateTimeFormatter.ofPattern("yyMM")); 
        String randomTwoDigits = String.format("%02d", new Random().nextInt(100)); 

        String empNo = distributorNo + memRegDate + randomTwoDigits;
        return empNo;
    }

    /**
     * 부서 이름 조회
     * 기술: Spring Data JPA
     * 설명: 지점 번호를 통해 해당 지점의 이름을 조회합니다.
     */
    public String getDistributorNameByNo(Long distributorNo) {
        return distributorRepository.findDistributorNameByDistributorNo(distributorNo);
    }
    /**
     * 아이디 중복 확인 메서드
     * 기술: Spring Data JPA
     * 설명: 특정 아이디가 이미 존재하는지 확인합니다.
     */
    public boolean isIdDuplicated(String memId) {
        return memberRepository.existsByMemId(memId);
    }
    // 회원 등록 메서드
    /**
     * 회원 등록 메서드
     * 기술: Spring Data JPA, PasswordEncoder, DTO 패턴
     * 설명: 사원번호 생성, 비밀번호 인코딩한 후, DTO를 엔티티로 변환하여 데이터베이스에 저장합니다.
     */

    public Member saveMember(MemberDto memberDto) {
        // 사원번호 생성
        String empNo = generateEmpNo(memberDto);
        System.out.println("Generated empNo in Service: " + empNo);

        // 직급과 소속 찾기
        Rank rank = rankRepository.findById(memberDto.getRank_no())
                .orElseThrow(() -> new IllegalArgumentException("직급 정보를 찾을 수 없습니다."));
        Distributor distributor = distributorRepository.findById(memberDto.getDistributor_no())
                .orElseThrow(() -> new IllegalArgumentException("소속 정보를 찾을 수 없습니다."));

        // 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(memberDto.getMem_pw());
        memberDto.setMem_pw(encodedPassword);  // DTO에 저장

        // DTO에서 엔티티로 변환하면서 사원번호 설정
        Member member = memberDto.toEntity(rank, distributor);
        member.setEmpNo(empNo); // 사원번호 설정

        // 회원 정보 저장
        return memberRepository.save(member);
    }
    /**
     * 프로필 이미지 저장 (saveProfilePicture)
     * 기술: 파일 입출력, java.nio.file.Files, StandardCopyOption
     * 설명: 프로필 이미지를 지정된 디렉토리에 저장하며, 기존 파일이 있는 경우 덮어씁니다.
     */
    private String saveProfilePicture(MultipartFile profilePicture, String distributorName, String memberName) throws IOException {
        String uploadDir = "src/main/resources/static/profile/" + distributorName;
        Files.createDirectories(Paths.get(uploadDir));

        String fileName = distributorName + "_" + memberName + "_프로필." + getExtension(profilePicture.getOriginalFilename());
        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(profilePicture.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }
    /**
     * 회원 정보 수정 (updateMember)
     * 기술: Spring Data JPA, 파일 입출력, MultipartFile, DTO 패턴
     * 설명: 기존 회원 정보를 가져와 필요한 필드를 수정하고, 프로필 사진이 업로드된 경우 파일을 저장하며 변경 사항을 데이터베이스에 반영합니다.
     */
    public void updateMember(MemberDto memberDto, MultipartFile profilePicture) throws IOException {
        Member existingMember = memberRepository.findById(memberDto.getMem_no())
            .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다: " + memberDto.getMem_no()));

        memberDto.setMem_id(existingMember.getMemId());  
        memberDto.setEmp_no(existingMember.getEmpNo());  
        if (memberDto.getMem_leave() == null || memberDto.getMem_leave().isEmpty()) {
            memberDto.setMem_leave(existingMember.getMemLeave() != null ? existingMember.getMemLeave() : "N");
        }
        if (memberDto.getMem_pw() != null && !memberDto.getMem_pw().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(memberDto.getMem_pw());
            memberDto.setMem_pw(encodedPassword);
        } else {
            memberDto.setMem_pw(existingMember.getMemPw());  
        }

        // 프로필 사진 처리: 새로운 파일이 있는 경우에만 저장
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String savedFileName = saveProfilePicture(profilePicture, existingMember.getDistributor().getDistributorName(), existingMember.getMemName());
            memberDto.setProfile_saved(savedFileName);
        } else {
            memberDto.setProfile_saved(existingMember.getProfileSaved()); 
        }

        // 등록일 유지, 수정일 갱신
        memberDto.setMem_reg_date(existingMember.getMemRegDate());
        memberDto.setMem_mod_date(LocalDate.now());

        Member member = memberDto.toEntity(existingMember.getRank(), existingMember.getDistributor());
        memberRepository.save(member); 
    }

    /**
     * 파일 확장자 추출 (getExtension)
     * 기술: 문자열 조작
     * 설명: 파일명에서 확장자를 추출하여 반환합니다.
     */
    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 직급 정보를 불러오는 메서드 (getRank)
     * 기술: Spring Data JPA
     * 설명: 데이터베이스에서 모든 직급 정보를 조회해 반환합니다.
     */    public List<Rank> getRank() {
        return rankRepository.findAll();
    }
    /**
     * 소속 지점을 불러오는 메서드 (getDistributors)
     * 기술: Spring Data JPA
     * 설명: 데이터베이스에서 모든 소속 정보를 조회해 반환합니다.
     */
    // 소속 지점을 불러오는 메서드
    public List<Distributor> getDistributors() {
        return distributorRepository.findAll();
    }

    public Member findMemberById(String memId) {
        Optional<Member> member = memberRepository.findByMemId(memId);
        return member.orElseThrow(() -> new IllegalArgumentException("해당 아이디의 회원을 찾을 수 없습니다: " + memId));
    }
    /**
     * 특정 회원 조회 (getMemberById)
     * 기술: Spring Data JPA, DTO 패턴
     * 설명: 특정 ID 또는 회원 번호로 회원을 조회한 뒤, DTO로 변환하여 반환합니다.
     */
    public MemberDto getMemberById(Long memNo) {
        // 멤버 조회
        Member member = memberRepository.findById(memNo)
            .orElseThrow(() -> new IllegalArgumentException("해당 멤버를 찾을 수 없습니다: " + memNo));

        // DTO로 변환하면서 rank_name과 distributor_name을 추가 설정
        MemberDto memberDto = MemberDto.toDto(member);
        memberDto.setRank_name(member.getRank().getRankName());  // Rank의 rank_name 설정
        memberDto.setDistributor_name(member.getDistributor().getDistributorName());  // Distributor의 distributor_name 설정

        return memberDto;
    }

    /**
     * 퇴사 여부에 따라 사번 오름차순으로 정렬된 회원 리스트를 반환합니다.
     * 기술: Spring Data JPA, 페이징 (Pageable)
     * 설명: 특정 퇴사 여부(memLeave)에 따라 회원 리스트를 정렬된 형태로 반환합니다.
     * 
     * @param memLeave 퇴사 여부 ('Y' 또는 'N')
     * @param pageable 페이징 정보
     * @return 페이징된 회원 리스트
     */
    public Page<Member> findAllByMemLeaveOrderByEmpNoAsc(String memLeave, Pageable pageable) {
        return memberRepository.findAllByMemLeaveOrderByEmpNoAsc(memLeave, pageable);
    }

    /**
     * 모든 회원을 사번 오름차순으로 정렬된 형태로 반환합니다.
     * 기술: Spring Data JPA, 페이징 (Pageable)
     * 설명: 퇴사 여부와 상관없이 모든 회원을 정렬된 형태로 반환합니다.
     * 
     * @param pageable 페이징 정보
     * @return 페이징된 회원 리스트
     */
    public Page<Member> findAllOrderByEmpNoAsc(Pageable pageable) {
        return memberRepository.findAllByOrderByEmpNoAsc(pageable);
    }

    /**
     * 특정 지점에 속한 회원을 반환합니다.
     * 기술: Spring Data JPA, 페이징 (Pageable)
     * 설명: 주어진 distributorNo에 해당하는 배급사에 속한 회원들을 반환합니다.
     * 
     * @param distributorNo 배급사 번호
     * @param pageable 페이징 정보
     * @return 페이징된 회원 리스트
     */
    public Page<Member> findMembersByDistributor(Long distributorNo, Pageable pageable) {
        return memberRepository.findByDistributor_DistributorNo(distributorNo, pageable);
    }

    /**
     * 현재 지점에 속한 회원을 반환합니다.
     * 기술: Spring Data JPA, 페이징 (Pageable)
     * 설명: 주어진 distributorNo에 해당하는 현재 배급사에 속한 회원들을 반환합니다.
     * 
     * @param distributorNo 배급사 번호
     * @param pageable 페이징 정보
     * @return 페이징된 회원 리스트
     */
    public Page<Member> findMembersByCurrentDistributor(Long distributorNo, Pageable pageable) {
        return memberRepository.findByDistributor_DistributorNo(distributorNo, pageable);
    }

    /**
     * 검색 조건에 맞는 회원 리스트를 반환합니다.
     * 기술: Spring Data JPA, 페이징 (Pageable)
     * 설명: 이름이나 이메일을 기준으로 검색어에 맞는 회원 리스트를 페이징된 형태로 반환합니다.
     * 
     * @param searchText 검색어
     * @param searchType 검색 유형 ('name' 또는 'email')
     * @param pageable 페이징 정보
     * @return 페이징된 회원 리스트
     */
    public Page<Member> findMembersBySearchCriteria(String searchText, String searchType, Pageable pageable) {
        if (searchType != null && searchText != null) {
            switch (searchType) {
                case "name":
                    return memberRepository.findByMemNameContaining(searchText, pageable);
                case "email":
                    return memberRepository.findByMemEmailContaining(searchText, pageable);
                default:
                    return memberRepository.findAll(pageable);
            }
        }
        return memberRepository.findAll(pageable);
    }

    /**
     * 검색 조건 및 필터에 따라 회원 리스트를 반환합니다.
     * 기술: Spring Data JPA, 페이징 (Pageable)
     * 설명: 회원의 이름, 직급, 입사일, 지점 등을 기준으로 검색어에 맞는 회원 리스트를 반환하거나,
     * 퇴사 여부와 지점 필터에 따라 회원 리스트를 필터링합니다.
     * 
     * @param searchType 검색 유형 ('name', 'rank', 'hireDate', 'branch', 'empNo')
     * @param searchText 검색어
     * @param statusFilter 상태 필터 ('resigned', 'all', 'mybranch' 등)
     * @param distributorNo 배급사 번호 (필터에 'mybranch'가 있을 경우 사용)
     * @param pageable 페이징 정보
     * @return 페이징된 회원 리스트
     */
    public Page<Member> searchMembersByCriteria(String searchType, String searchText, String statusFilter, Long distributorNo, Pageable pageable) {
        String memLeave = "N";  // 기본값은 재직 중인 직원
        if ("resigned".equals(statusFilter)) {
            memLeave = "Y";  // 퇴사한 직원
        } else if ("all".equals(statusFilter)) {
            memLeave = null;  // 재직/퇴사 여부 상관없이 모든 직원
        }

        if (searchText != null && !searchText.isEmpty()) {
            // 검색 조건이 있을 때
            if ("mybranch".equals(statusFilter)) {
                return memberRepository.findByDistributor_DistributorNoAndMemLeaveAndSearchText(distributorNo, "N", searchText, pageable);
            } else if (memLeave != null) {
                switch (searchType) {
                    case "name":
                        return memberRepository.findByMemNameContainingAndMemLeave(searchText, memLeave, pageable);
                    case "rank":
                        return memberRepository.findByRankRankNameContainingAndMemLeave(searchText, memLeave, pageable);
                    case "hireDate":
                        return memberRepository.findByMemRegDateAndMemLeave(LocalDate.parse(searchText), memLeave, pageable);
                    case "branch":
                        return memberRepository.findByDistributorDistributorNameContainingAndMemLeave(searchText, memLeave, pageable);
                    case "empNo":
                        return memberRepository.findByEmpNoContainingAndMemLeave(searchText, memLeave, pageable);
                    default:
                        return memberRepository.findAll(pageable);
                }
            } else {
                switch (searchType) {
                    case "name":
                        return memberRepository.findByMemNameContaining(searchText, pageable);
                    case "rank":
                        return memberRepository.findByRankRankNameContaining(searchText, pageable);
                    case "hireDate":
                        return memberRepository.findByMemRegDate(LocalDate.parse(searchText), pageable);
                    case "branch":
                        return memberRepository.findByDistributorDistributorNameContaining(searchText, pageable);
                    case "empNo":
                        return memberRepository.findByEmpNoContaining(searchText, pageable);
                    default:
                        return memberRepository.findAll(pageable);
                }
            }
        } else {
            // 검색 조건이 없을 때 상태 필터만 처리
            if ("mybranch".equals(statusFilter)) {
                return memberRepository.findByDistributor_DistributorNoAndMemLeave(distributorNo, "N", pageable);
            } else if (memLeave != null) {
                return memberRepository.findAllByMemLeaveOrderByEmpNoAsc(memLeave, pageable);
            } else {
                return memberRepository.findAllByOrderByEmpNoAsc(pageable);
            }
        }
    }


    /**
     * 회원 번호로 회원을 조회합니다.
     * 기술: Spring Data JPA
     * 설명: 주어진 회원 번호(memNo)에 해당하는 회원을 조회합니다.
     * 회원이 존재하지 않을 경우 예외를 발생시킵니다.
     * 
     * @param memNo 회원 번호
     * @return 조회된 회원 객체
     */
    public Member findMemberByNo(Long memNo) {
        return memberRepository.findById(memNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원 번호의 회원을 찾을 수 없습니다: " + memNo));
    }

    /**
     * 회원 정보를 수정하는 메서드.
     * 기술: Spring Data JPA
     * 설명: 주어진 회원 객체의 정보로 기존 회원 데이터를 수정합니다.
     * 직급 및 지점 정보를 포함해 필드 값을 갱신하고 저장합니다.
     * 
     * @param member 수정할 회원 객체
     */
    public void editMember(Member member) {
        Member existingMember = memberRepository.findById(member.getMemNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        // 수정할 정보 설정
        existingMember.setMemName(member.getMemName());
        existingMember.setMemPhone(member.getMemPhone());
        existingMember.setMemEmail(member.getMemEmail());

        // 직급 수정 (직급 ID를 받아서 Rank 설정)
        Rank rank = rankRepository.findById(member.getRank().getRankNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 직급을 찾을 수 없습니다."));
        existingMember.setRank(rank);

        // 지점 수정 (지점 ID를 받아서 Distributor 설정)
        Distributor distributor = distributorRepository.findById(member.getDistributor().getDistributorNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 지점을 찾을 수 없습니다."));
        existingMember.setDistributor(distributor);

        // 프로필 사진, 연차 등 정보 설정
        existingMember.setProfileSaved(member.getProfileSaved());
        existingMember.setMemOff(member.getMemOff());

        memberRepository.save(existingMember);  // 수정된 회원 정보 저장
    }

    /**
     * 채팅용 회원 리스트를 조회합니다.
     * 기술: Spring Data JPA
     * 설명: 특정 회원(memId)을 제외한 회원 리스트를 조회하고, 해당 리스트를 DTO로 변환하여 반환합니다.
     * 
     * @param memId 제외할 회원 ID
     * @return 회원 DTO 리스트
     */
    public List<MemberDto> findAllForChat(String memId) {
        List<Member> memberList = memberRepository.findAllForChat(memId);
        List<MemberDto> memberDtoList = new ArrayList<>();
        for (Member m : memberList) {
            MemberDto dto = MemberDto.toDto(m);
            memberDtoList.add(dto);
        }
        return memberDtoList;
    }

    /**
     * 회원 정보를 수정하고 프로필 사진을 처리합니다.
     * 기술: Spring Data JPA, 파일 업로드 (MultipartFile)
     * 설명: 회원 정보를 수정하고, 새로운 프로필 사진이 업로드된 경우 이를 저장합니다.
     * 프로필 사진이 없으면 기존 사진을 유지합니다.
     * 
     * @param memberDto 수정할 회원 정보를 담은 DTO
     * @param profilePicture 수정할 프로필 사진 (선택사항)
     * @throws IOException 프로필 사진 저장 실패 시 발생
     */
    public void editMember(MemberDto memberDto, MultipartFile profilePicture) throws IOException {
        // 회원 번호로 회원 찾기
        Member existingMember = memberRepository.findByMemNo(memberDto.getMem_no())
            .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다: " + memberDto.getMem_no()));

        // 공통 로직: 필수 필드 유지, 회원 정보 수정
        existingMember.setMemName(memberDto.getMem_name());
        existingMember.setMemPhone(memberDto.getMem_phone());
        existingMember.setMemEmail(memberDto.getMem_email());

        // 직급과 지점 수정
        Rank rank = rankRepository.findById(memberDto.getRank_no())
            .orElseThrow(() -> new IllegalArgumentException("해당 직급을 찾을 수 없습니다."));
        existingMember.setRank(rank);
        Distributor distributor = distributorRepository.findById(memberDto.getDistributor_no())
            .orElseThrow(() -> new IllegalArgumentException("해당 지점을 찾을 수 없습니다."));
        existingMember.setDistributor(distributor);

        // 프로필 사진 처리
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String savedFileName = saveProfilePicture(profilePicture, existingMember.getDistributor().getDistributorName(), existingMember.getMemName());
            existingMember.setProfileSaved(savedFileName);
        } else {
            existingMember.setProfileSaved(existingMember.getProfileSaved());  // 기존 사진 유지
        }

        // 기타 정보 수정 및 저장
        existingMember.setMemOff(memberDto.getMem_off());
        memberRepository.save(existingMember);
    }
}
