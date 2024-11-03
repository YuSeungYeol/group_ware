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

import com.ware.spring.commute.domain.Commute;
import com.ware.spring.commute.repository.CommuteRepository;
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
    /**
     * 사원번호 생성 로직
     * 기술: 문자열 조작, Random
     * 설명: 사용자의 소속된 지점 번호, 등록일, 무작위 2자리 숫자를 조합하여 사원번호를 생성합니다.
     */
    public String generateEmpNo(MemberDto memberDto) {
        String distributorNo = String.format("%02d", memberDto.getDistributor_no()); // 두 자리로 맞춤
        String memRegDate = memberDto.getMem_reg_date().format(DateTimeFormatter.ofPattern("yyMM")); // YYMM 추출
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
     * 회원 정보 수정 (updateMember)
     * 기술: Spring Data JPA, 파일 입출력, MultipartFile, DTO 패턴
     * 설명: 기존 회원 정보를 가져와 필요한 필드를 수정하고, 프로필 사진이 업로드된 경우 파일을 저장하며 변경 사항을 데이터베이스에 반영합니다.
     */
    public void updateMember(MemberDto memberDto, MultipartFile profilePicture) throws IOException {
        // 기존 회원 정보 가져오기
        Member existingMember = memberRepository.findById(memberDto.getMem_no())
            .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다: " + memberDto.getMem_no()));

        // 필수 필드 유지
        memberDto.setMem_id(existingMember.getMemId());  // 아이디 유지
        memberDto.setEmp_no(existingMember.getEmpNo());  // 사원번호 유지
        if (memberDto.getMem_leave() == null || memberDto.getMem_leave().isEmpty()) {
            memberDto.setMem_leave(existingMember.getMemLeave() != null ? existingMember.getMemLeave() : "N");
        }
        // 비밀번호가 비어있지 않으면 암호화하고, 비어있으면 기존 비밀번호 유지
        if (memberDto.getMem_pw() != null && !memberDto.getMem_pw().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(memberDto.getMem_pw());
            memberDto.setMem_pw(encodedPassword);
        } else {
            memberDto.setMem_pw(existingMember.getMemPw());  // 기존 비밀번호 유지
        }

        // 프로필 사진 처리: 새로운 파일이 있는 경우에만 저장
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String savedFileName = saveProfilePicture(profilePicture, existingMember.getDistributor().getDistributorName(), existingMember.getMemName());
            memberDto.setProfile_saved(savedFileName);
        } else {
            memberDto.setProfile_saved(existingMember.getProfileSaved());  // 기존 사진 유지
        }

        // 등록일 유지, 수정일 갱신
        memberDto.setMem_reg_date(existingMember.getMemRegDate());
        memberDto.setMem_mod_date(LocalDate.now());

        // DTO를 엔티티로 변환하여 저장
        Member member = memberDto.toEntity(existingMember.getRank(), existingMember.getDistributor());
        memberRepository.save(member);  // 업데이트 처리
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
     * 비밀번호 업데이트 (updatePassword)
     * 기술: PasswordEncoder, Spring Data JPA
     * 설명: 비밀번호를 암호화한 후 데이터베이스에 업데이트합니다.
     */
    public void updatePassword(int memNo, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        memberRepository.updatePassword(memNo, encodedPassword);
    }

    public void updateMemberInfo(int memNo, String memName, String memPhone, String memEmail) {
        memberRepository.updateMemberInfo(memNo, memName, memPhone, memEmail);
    }

    /**
     * 회원 리스트 조회 (findAllByMemLeaveOrderByEmpNoAsc, findAllOrderByEmpNoAsc)
     * 기술: Spring Data JPA, 페이징 (Pageable)
     * 설명: 퇴사 여부에 따라 정렬된 회원 리스트를 반환하거나, 모든 회원을 정렬된 형태로 반환합니다.
     */
    public Page<Member> findAllByMemLeaveOrderByEmpNoAsc(String memLeave, Pageable pageable) {
        return memberRepository.findAllByMemLeaveOrderByEmpNoAsc(memLeave, pageable);
    }
    public Page<Member> findAllOrderByEmpNoAsc(Pageable pageable) {
        return memberRepository.findAllByOrderByEmpNoAsc(pageable);
    }
    public Page<Member> findMembersByDistributor(Long distributorNo, Pageable pageable) {
        return memberRepository.findByDistributor_DistributorNo(distributorNo, pageable);
    }
    public Page<Member> findMembersByCurrentDistributor(Long distributorNo, Pageable pageable) {
        return memberRepository.findByDistributor_DistributorNo(distributorNo, pageable);
    }
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
    public Page<Member> searchMembersByCriteria(String searchType, String searchText, String statusFilter, Long distributorNo, Pageable pageable) {
        String memLeave = "N";  // 기본값은 재직 중인 직원
        if ("resigned".equals(statusFilter)) {
            memLeave = "Y";  // 퇴사한 직원
        } else if ("all".equals(statusFilter)) {
            memLeave = null;  // 재직/퇴사 여부 상관없이 모든 직원
        }

        if (searchText != null && !searchText.isEmpty()) {
            // 검색어가 있을 때
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
            if ("mybranch".equals(statusFilter)) {
                return memberRepository.findByDistributor_DistributorNoAndMemLeave(distributorNo, "N", pageable);
            } else if (memLeave != null) {
                return memberRepository.findAllByMemLeaveOrderByEmpNoAsc(memLeave, pageable);
            } else {
                return memberRepository.findAllByOrderByEmpNoAsc(pageable);
            }
        }
    }



    public Member findMemberByNo(Long memNo) {
        return memberRepository.findById(memNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원 번호의 회원을 찾을 수 없습니다: " + memNo));
    }
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

    public List<MemberDto> findAllForChat(String memId){
    	List<Member> memberList = memberRepository.findAllForChat(memId);
    	List<MemberDto> memberDtoList = new ArrayList<MemberDto>();
    	for(Member m : memberList) {
    		new MemberDto();
			MemberDto dto = MemberDto.toDto(m);
    		memberDtoList.add(dto);
    	}
    	return memberDtoList;
    }
    
    public void editMember(MemberDto memberDto, MultipartFile profilePicture) throws IOException {
        // 사원번호가 아니라 회원 번호(mem_no)로 회원 찾기
        Member existingMember = memberRepository.findByMemNo(memberDto.getMem_no())
            .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다: " + memberDto.getMem_no()));
        System.out.println("Received member data: " + memberDto);
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
