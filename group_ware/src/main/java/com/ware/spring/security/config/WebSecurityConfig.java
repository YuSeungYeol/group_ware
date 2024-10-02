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

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(requests -> 
                requests
                    .requestMatchers("/login", "/css/**", "/image/**").permitAll()  // 모두에게 허용
                    .requestMatchers("/api/member/register").hasAnyAuthority("ROLE_차장", "ROLE_지점대표", "ROLE_대표")  // 인증된 사용자만 접근 가능
                    .requestMatchers("/member/register").hasAnyAuthority("ROLE_차장", "ROLE_지점대표", "ROLE_대표")  // 인증된 사용자만 접근 가능
                    .requestMatchers("/authorization/**", "/approval/**", "/notice/**","/board/**").authenticated()
                    .anyRequest().authenticated()
            )
            .formLogin(login -> 
                login.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .usernameParameter("mem_id")
                    .passwordParameter("mem_pw")
                    .permitAll()
                    .successHandler(new MyLoginSuccessHandler())
                    .failureHandler(new MyLoginFailureHandler()))
            .logout(logout -> 
                logout.logoutUrl("/logout")  // 로그아웃 URL 설정
                      .logoutSuccessUrl("/login?logout")  // 로그아웃 성공 후 리다이렉트
                      .invalidateHttpSession(true)  // 세션 무효화
                      .deleteCookies("JSESSIONID")  // 쿠키 삭제
                      .permitAll())  // 로그아웃은 모두에게 허용

            .rememberMe(rememberMe -> 
                rememberMe.rememberMeParameter("remember-me")
                        .tokenValiditySeconds(86400 * 7)  // 토큰 유효 기간 7일
                        .alwaysRemember(false)
                        .tokenRepository(tokenRepository())
                        .userDetailsService(userDetailsService))
            .csrf(csrf -> 
            csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())  // CSRF 토큰을 쿠키로 설정
            .ignoringRequestMatchers("/api/member/verify-password")  // 이 경로는 CSRF 필터 무시
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
				"/chatting/**"
                
            )
        )
            .httpBasic(Customizer.withDefaults());  // HTTP Basic 인증 사용
        return http.build();
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
