package com.ware.spring.security.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.ware.spring.security.service.SecurityService;

@Configuration
public class WebSecurityConfig {

    private final DataSource dataSource;
    private final UserDetailsService userDetailsService;

    @Autowired
    public WebSecurityConfig(DataSource dataSource, SecurityService userDetailsService) {
        this.dataSource = dataSource;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Spring Security의 기본 설정을 구성하는 SecurityFilterChain 빈을 정의합니다.
     * 설명: HTTP 요청에 대한 보안 규칙과 로그인, 로그아웃, CSRF, Remember Me 설정 등을 구성합니다.
     * 
     * @param http HttpSecurity 객체를 통한 설정
     * @return 구성된 SecurityFilterChain
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(requests -> 
                requests
                    .requestMatchers("/login", "/css/**", "/image/**").permitAll() 
                    .requestMatchers("/member/register").hasAnyAuthority( "ROLE_지점대표", "ROLE_대표") 
                    .requestMatchers("/authorization/**", "/approval/**", "/notice/**","/board/**","/chat/**","/api/**","/commute/**","/vehicle/**","/clearNoticeNotification/**").authenticated()
                    .anyRequest().authenticated()  // 그 외 모든 요청은 인증 필요
            )
            .formLogin(login -> 
                login.loginPage("/login")  // 사용자 정의 로그인 페이지 설정
                    .loginProcessingUrl("/login")  // 로그인 폼 처리 URL
                    .usernameParameter("mem_id")  // 사용자명 파라미터 설정
                    .passwordParameter("mem_pw")  // 비밀번호 파라미터 설정
                    .permitAll()  // 로그인 페이지는 모두에게 허용
                    .successHandler(new MyLoginSuccessHandler())  // 로그인 성공 시 처리할 핸들러
                    .failureHandler(new MyLoginFailureHandler()))  // 로그인 실패 시 처리할 핸들러
            .logout(logout -> 
                logout.logoutUrl("/logout")  // 로그아웃 URL 설정
                      .logoutSuccessUrl("/login?logout")  // 로그아웃 성공 후 리다이렉트 URL
                      .invalidateHttpSession(true)  // 로그아웃 시 세션 무효화
                      .deleteCookies("JSESSIONID")  // 로그아웃 시 JSESSIONID 쿠키 삭제
                      .permitAll())  // 로그아웃은 모두에게 허용
            .rememberMe(rememberMe -> 
                rememberMe.rememberMeParameter("remember-me")  // 'Remember Me' 기능에 사용할 파라미터 이름 설정
                        .tokenValiditySeconds(86400 * 7)  // Remember Me 토큰 유효 기간을 7일로 설정
                        .alwaysRemember(false)  // 'Remember Me' 체크박스 미선택 시 Remember Me 기능 비활성화
                        .tokenRepository(tokenRepository())  // Remember Me 토큰 저장소 설정
                        .userDetailsService(userDetailsService))  // 사용자 정보를 조회할 서비스 설정
            .csrf(csrf -> 
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())  // CSRF 토큰을 HTTP-only가 아닌 쿠키로 설정하여 클라이언트 측에서 읽기 가능
                .ignoringRequestMatchers("/api/member/verify-password")  // 특정 경로는 CSRF 보호에서 제외
                .ignoringRequestMatchers(
                    "/calendar/schedule/createScheduleWithJson",
                    "/calendar/schedule/getScheduleListForLoggedInUser",
                    "/calendar/schedule/update/{id}",
                    "/calendar/schedule/delete/{id}",
                    "/folder/create",
                    "/folder/uploadFile",
                    "/folder/updateDelYn",
                    "/folder/apiList",
                    "/folder/downloadFile",
                    "/personal-drive/apiList",
                    "/personal-drive/uploadFile",
                    "/personal-drive/create",
                    "/api/member/**",
                    "/api/commute/**",
                    "/chat/**",
                    "/chatting/**",
                    "/clearNoticeNotification/**",
                    "/notice/**",
                    "/board/**",
                    "/api/vehicle/**"
                )
            )
            .httpBasic(Customizer.withDefaults());  // HTTP Basic 인증 사용
        return http.build();
    }

    /**
     * Remember Me 기능에서 사용할 PersistentTokenRepository 빈을 정의합니다.
     * 설명: Remember Me 토큰을 데이터베이스에 저장하여 사용자가 로그인 상태를 유지할 수 있도록 합니다.
     * 
     * @return JdbcTokenRepositoryImpl 데이터베이스를 사용하는 PersistentTokenRepository
     */
    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    /**
     * 비밀번호 인코딩을 위한 PasswordEncoder 빈을 정의합니다.
     * 설명: 비밀번호를 BCrypt 알고리즘을 사용하여 암호화하는 PasswordEncoder를 제공합니다.
     * 
     * @return BCryptPasswordEncoder 비밀번호 암호화에 사용할 PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
