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

    /**
     * 회원 등록을 처리합니다.
     * 설명: 회원 정보와 프로필 이미지를 받아 회원을 등록하고 이미지를 저장합니다.
     * 
     * @param croppedImageData Base64로 인코딩된 프로필 이미지 데이터
     * @param memberDto 등록할 회원의 정보가 담긴 DTO
     * @return 회원 등록 성공 또는 실패에 대한 메시지를 포함한 응답
     */
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
                String mimeType = croppedImageData.substring(croppedImageData.indexOf("/") + 1, croppedImageData.indexOf(";"));
                String base64Image = croppedImageData.split(",")[1];
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                BufferedImage originalImage = ImageIO.read(bis);
                if (originalImage == null) {
                    throw new IOException("BufferedImage 생성 실패");
                }
                int imageType = BufferedImage.TYPE_INT_ARGB;
                if (mimeType.equals("jpeg") || mimeType.equals("jpg")) {
                    imageType = BufferedImage.TYPE_INT_RGB;
                }
                BufferedImage formattedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), imageType);
                formattedImage.getGraphics().drawImage(originalImage, 0, 0, null);
                // 이미지 저장 경로 설정 (부서 이름을 폴더 이름으로 사용)
                String uploadDir = "src/main/resources/static/profile/" + distributorName;
                Files.createDirectories(Paths.get(uploadDir));
                // 파일 이름 설정 및 저장
                String fileName = distributorName + "_" + memberDto.getMem_name() + "_프로필." + mimeType;
                Path path = Paths.get(uploadDir, fileName);
                File outputfile = path.toFile();
                boolean writeSuccess = ImageIO.write(formattedImage, mimeType.equals("jpg") ? "jpeg" : mimeType, outputfile);

                if (!writeSuccess) {
                    throw new IOException("이미지 파일 저장 실패");
                }

                memberDto.setProfile_saved(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                response.put("success", false);
                response.put("res_msg", "크롭된 프로필 이미지 저장 중 오류 발생: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

        try {
            memberService.saveMember(memberDto);
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

    /**
     * 아이디 중복 여부를 확인합니다.
     * 설명: 클라이언트에서 전달한 아이디의 중복 여부를 확인합니다.
     * 
     * @param memId 중복 체크할 회원 아이디
     * @return 중복 여부를 나타내는 Boolean 값이 포함된 응답
     */
    @ResponseBody
    @GetMapping("/check-id")
    public ResponseEntity<Map<String, Boolean>> checkIdDuplication(@RequestParam("mem_id") String memId) {
        Map<String, Boolean> response = new HashMap<>();
        boolean exists = memberService.isIdDuplicated(memId);
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 회원 정보를 조회합니다.
     * 설명: 회원 번호로 특정 회원 정보를 조회하여 반환합니다.
     * 
     * @param memNo 조회할 회원 번호
     * @return 조회된 회원 정보가 포함된 DTO
     */
    @ResponseBody
    @GetMapping("/{memNo}")
    public ResponseEntity<MemberDto> getMemberById(@PathVariable("memNo") Long memNo) {
        MemberDto member = memberService.getMemberById(memNo);
        return ResponseEntity.ok(member);
    }

    /**
     * 회원 정보를 수정합니다.
     * 설명: 회원 정보를 수정하고 수정 후 로그아웃을 처리합니다.
     * 
     * @param memberDto 수정할 회원의 정보가 담긴 DTO
     * @param profileImage 선택적인 프로필 이미지
     * @return 수정 성공 여부에 대한 메시지를 포함한 응답
     */
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
            // 비밀번호 변경 여부 확인 로직
            boolean passwordChanged = memberService.isPasswordChanged(memberDto);
            
            // 회원 정보 업데이트 수행
            memberService.updateMember(memberDto, profileImage);

            // 비밀번호가 변경된 경우에만 로그아웃 처리
            if (passwordChanged) {
                new SecurityContextLogoutHandler().logout(request, response, authentication);
            }

            responseMap.put("success", true);
            responseMap.put("message", passwordChanged 
                ? "비밀번호가 변경되었습니다. 다시 로그인해주세요." 
                : "회원 정보가 성공적으로 수정되었습니다.");
            responseMap.put("passwordChanged", passwordChanged); 
            return ResponseEntity.ok().body(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        }
    }


    /**
     * 마이페이지 정보를 반환합니다.
     * 설명: 로그인한 사용자의 정보를 기반으로 마이페이지 데이터를 반환합니다.
     * 
     * @param model 뷰에 전달할 데이터
     * @param user 현재 로그인한 사용자
     * @return 마이페이지 뷰 이름
     */
    @ResponseBody
    @GetMapping("/mypage")
    public String myPage(Model model, @AuthenticationPrincipal SecurityUser user) {
        MemberDto memberDto = memberService.getMemberById(user.getMember().getMemNo());
        List<Distributor> distributors = memberService.getDistributors();
        List<Rank> ranks = memberService.getRank();

        model.addAttribute("member", memberDto);
        model.addAttribute("distributors", distributors);
        model.addAttribute("rank", ranks);

        return "mypage";
    }

    /**
     * 비밀번호를 검증합니다.
     * 설명: 클라이언트에서 제공한 비밀번호가 현재 로그인한 사용자의 비밀번호와 일치하는지 확인합니다.
     * 마이페이지 본인확인용도
     * @param requestBody 검증할 비밀번호가 포함된 요청 본문
     * @param securityUser 현재 로그인한 사용자
     * @return 비밀번호 일치 여부에 대한 메시지를 포함한 응답
     */
    @PostMapping("/verify-password")
    public ResponseEntity<Map<String, Object>> verifyPassword(
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal SecurityUser securityUser) {

        String inputPassword = requestBody.get("mem_pw");
        String currentUserPassword = securityUser.getPassword();

        Map<String, Object> response = new HashMap<>();
        if (passwordEncoder.matches(inputPassword, currentUserPassword)) {
            response.put("success", true);
            response.put("message", "비밀번호가 일치합니다.");
        } else {
            response.put("success", false);
            response.put("message", "비밀번호가 일치하지 않습니다.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 회원 정보를 수정합니다.
     * 설명: 회원 정보와 프로필 이미지를 받아 회원 정보를 수정합니다.
     * 
     * @param memberDto 수정할 회원의 정보가 담긴 DTO
     * @param profileImage 선택적인 프로필 이미지
     * @return 수정 성공 또는 실패에 대한 메시지를 포함한 응답
     */
    @PostMapping("/edit")
    public ResponseEntity<?> editMember(
        @RequestPart("memberData") MemberDto memberDto,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            memberService.editMember(memberDto, profileImage);
            return ResponseEntity.ok().body("회원 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("회원 정보 수정 중 오류가 발생했습니다. 오류: " + e.getMessage());
        }
    }
}
