package com.ware.spring.member.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.member.domain.Distributor;
import com.ware.spring.member.domain.Member;
import com.ware.spring.member.domain.MemberDto;
import com.ware.spring.member.domain.Rank;
import com.ware.spring.member.service.MemberService;
import com.ware.spring.security.vo.SecurityUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/member")
public class MemberApiController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberApiController(MemberService memberService, PasswordEncoder passwordEncoder) {
        this.memberService = memberService;
        this.passwordEncoder = passwordEncoder;
    }
    @ResponseBody
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerMember(@RequestParam("croppedImage") String croppedImageData, 
                                                              @ModelAttribute MemberDto memberDto) {
        Map<String, Object> response = new HashMap<>();
        memberDto.setMem_leave("N");

        // 부서 이름 가져오기
        String distributorName = memberService.getDistributorNameByNo(memberDto.getDistributor_no());

        // 크롭된 이미지 처리
        if (croppedImageData != null && !croppedImageData.isEmpty()) {
            try {
                // Base64 디코딩 및 이미지 변환
                String mimeType = croppedImageData.substring(croppedImageData.indexOf("/") + 1, croppedImageData.indexOf(";"));
                String base64Image = croppedImageData.split(",")[1];
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                BufferedImage originalImage = ImageIO.read(bis);

                if (originalImage == null) {
                    throw new IOException("BufferedImage 생성 실패");
                }

                // 이미지 타입 변환
                int imageType = BufferedImage.TYPE_INT_ARGB;
                if (mimeType.equals("jpeg") || mimeType.equals("jpg")) {
                    imageType = BufferedImage.TYPE_INT_RGB;
                }
                BufferedImage formattedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), imageType);
                formattedImage.getGraphics().drawImage(originalImage, 0, 0, null);

                // 이미지 저장 경로 설정 (부서 이름을 폴더 이름으로 사용)
                String uploadDir = "src/main/resources/static/profile/" + distributorName;
                Files.createDirectories(Paths.get(uploadDir));  // 폴더가 없으면 생성

                // 부서 이름과 함께 파일명 설정
                String fileName = distributorName + "_" + memberDto.getMem_name() + "_프로필." + mimeType;
                Path path = Paths.get(uploadDir, fileName);

                // 디렉토리 생성 및 파일 저장
                File outputfile = path.toFile();
                boolean writeSuccess = ImageIO.write(formattedImage, mimeType.equals("jpg") ? "jpeg" : mimeType, outputfile);

                if (!writeSuccess) {
                    throw new IOException("이미지 파일 저장 실패");
                }

                memberDto.setProfile_saved(fileName);  // 저장된 파일 이름 설정
            } catch (IOException e) {
                e.printStackTrace();
                response.put("success", false);
                response.put("res_msg", "크롭된 프로필 이미지 저장 중 오류 발생: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

        try {
            // 회원 등록 처리 및 사원번호 생성
            memberService.saveMember(memberDto);

            // 사원번호 확인 로그
            System.out.println("Member registered with empNo: " + memberDto.getEmp_no());

            response.put("success", true);
            response.put("res_msg", "회원 등록이 성공적으로 완료되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("res_msg", "회원 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // 아이디 중복 체크
    @ResponseBody
    @GetMapping("/check-id")
    public ResponseEntity<Map<String, Boolean>> checkIdDuplication(@RequestParam("mem_id") String memId) {
        Map<String, Boolean> response = new HashMap<>();
        boolean exists = memberService.isIdDuplicated(memId);
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    // 조직도
    @ResponseBody
    @GetMapping("/{memNo}")
    public ResponseEntity<MemberDto> getMemberById(@PathVariable("memNo") Long memNo) {
        MemberDto member = memberService.getMemberById(memNo);
        return ResponseEntity.ok(member);
    }
    @ResponseBody
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateMember(
        @ModelAttribute MemberDto memberDto,
        @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // 회원 정보 수정 처리
            memberService.updateMember(memberDto, profileImage);

            // 로그아웃 처리
            new SecurityContextLogoutHandler().logout(request, response, authentication);

            // 성공 메시지를 응답으로 전송 (JSON 형식)
            responseMap.put("success", true);
            responseMap.put("message", "회원 정보가 성공적으로 수정되었습니다. 다시 로그인해주세요.");
            return ResponseEntity.ok().body(responseMap);  // 응답은 JSON 형식으로 처리
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        }
    }
    @ResponseBody
    @GetMapping("/mypage")
    public String myPage(Model model, @AuthenticationPrincipal SecurityUser user) {
        // 로그인한 사용자의 정보를 가져옴
        MemberDto memberDto = memberService.getMemberById(user.getMember().getMemNo());

        // 부서와 직급 정보 추가
        List<Distributor> distributors = memberService.getDistributors();
        List<Rank> ranks = memberService.getRank();

        model.addAttribute("member", memberDto); // 회원 정보 추가
        model.addAttribute("distributors", distributors); // 부서 정보 추가
        model.addAttribute("rank", ranks); // 직급 정보 추가

        return "mypage"; // 마이페이지 뷰로 이동
    }
    @PostMapping("/verify-password")
    public ResponseEntity<Map<String, Object>> verifyPassword(
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal SecurityUser securityUser) {

        // 클라이언트에서 전송된 비밀번호를 받음
        String inputPassword = requestBody.get("mem_pw");  // 클라이언트에서 name="mem_pw"로 전송됨
        String currentUserPassword = securityUser.getPassword();  // 로그인한 사용자의 암호화된 비밀번호

        Map<String, Object> response = new HashMap<>();

        // 입력된 비밀번호와 로그인한 사용자의 비밀번호를 비교
        if (passwordEncoder.matches(inputPassword, currentUserPassword)) {
            response.put("success", true);
            response.put("message", "비밀번호가 일치합니다.");
        } else {
            response.put("success", false);
            response.put("message", "비밀번호가 일치하지 않습니다.");
        }

        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/edit")
    public ResponseEntity<?> editMember(
        @RequestPart("memberData") MemberDto memberDto,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            // 회원 정보 업데이트 로직
            memberService.editMember(memberDto, profileImage);
            return ResponseEntity.ok().body("회원 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            // 예외 발생 시 구체적인 메시지 로그에 출력
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("회원 정보 수정 중 오류가 발생했습니다. 오류: " + e.getMessage());
        }
    }
}


