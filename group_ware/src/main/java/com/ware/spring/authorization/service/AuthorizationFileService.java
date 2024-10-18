package com.ware.spring.authorization.service;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ware.spring.authorization.domain.Authorization;
import com.ware.spring.authorization.repository.AuthorizationRepository;

@Service
public class AuthorizationFileService {
 
    private String fileDir = "C:\\document\\upload\\";
    
    private final AuthorizationRepository authorizationRepository;
    
    @Autowired
    public AuthorizationFileService(AuthorizationRepository authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }

    // 파일 업로드 메서드
    public String upload(MultipartFile file) {
        String newFileName = null;

        try {
            // 파일 이름이 직렬화된 객체가 아닌지 확인
            String oriFileName = file.getOriginalFilename();  // 파일 이름 가져오기

            // 파일 이름이 직렬화된 객체인지 체크하고 예외 처리
            if (!(oriFileName instanceof String)) {
                throw new ClassCastException("파일 이름이 올바른 문자열 형식이 아닙니다.");
            }

            // 기존 로직 유지
            String fileExt = oriFileName.substring(oriFileName.lastIndexOf("."), oriFileName.length());

            UUID uuid = UUID.randomUUID();
            String uniqueName = uuid.toString().replaceAll("-", "");
            newFileName = uniqueName + fileExt;
            File saveFile = new File(fileDir + newFileName);

            if (!saveFile.exists()) {
                saveFile.mkdirs();
            }

            file.transferTo(saveFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newFileName;
    }

    // 파일 다운로드 메서드
    public ResponseEntity<Object> download(Long authorNo) {
        try {
            Authorization authorization = authorizationRepository.findByAuthorNo(authorNo);
            
            if (authorization == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            String newFileName = authorization.getAuthorNewThumbnail();
            String oriFileName = authorization.getAuthorOriThumbnail();

            // 파일 이름이 직렬화된 객체로 저장된 경우 처리
            if (!(oriFileName instanceof String)) {
                throw new ClassCastException("파일 이름이 올바른 문자열 형식이 아닙니다.");
            }

            oriFileName = URLEncoder.encode(oriFileName, "UTF-8");
            String downDir = fileDir + newFileName;

            Path filePath = Paths.get(downDir);

            if (!Files.exists(filePath)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Resource resource = new InputStreamResource(Files.newInputStream(filePath));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.builder("attachment").filename(oriFileName).build());

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

 // 직렬화된 데이터를 읽어오는 메서드
    public void readSerializedData() {
        try {
            // 직렬화된 파일에서 데이터를 읽어옴
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileDir + "data.dat"));
            Object obj = ois.readObject();

            // ObjectStreamClass 타입 처리
            if (obj instanceof ObjectStreamClass) {
                ObjectStreamClass osc = (ObjectStreamClass) obj;
                System.out.println("읽은 ObjectStreamClass 데이터: " + osc);
            }
            // String 타입 처리
            else if (obj instanceof String) {
                String data = (String) obj;
                System.out.println("읽은 데이터: " + data);
            }
            // 그 외 타입 처리
            else {
                System.out.println("알 수 없는 객체 타입: " + obj.getClass().getName());
            }

            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
