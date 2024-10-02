	package com.ware.spring.security.service;
	
	import java.util.ArrayList;
	import java.util.List;
	
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.security.core.GrantedAuthority;
	import org.springframework.security.core.authority.SimpleGrantedAuthority;
	import org.springframework.security.core.userdetails.UserDetails;
	import org.springframework.security.core.userdetails.UserDetailsService;
	import org.springframework.security.core.userdetails.UsernameNotFoundException;
	import org.springframework.stereotype.Service;
	
	import com.ware.spring.member.domain.Member;
	import com.ware.spring.member.repository.MemberRepository;
	import com.ware.spring.security.vo.SecurityUser;
	
	@Service("SecurityService")
	public class SecurityService implements UserDetailsService {
	
	    private final MemberRepository memberRepository;
	
	    @Autowired
	    public SecurityService(MemberRepository memberRepository) {
	        this.memberRepository = memberRepository;
	    }
	
	    @Override
	    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	        Member member = memberRepository.findByMemId(username)
	                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
	
	        List<GrantedAuthority> authorities = new ArrayList<>();
	        String rankAuthority = getAuthorityByRank(member.getRank().getRankNo());
	        authorities.add(new SimpleGrantedAuthority(rankAuthority));
	
	        return new SecurityUser(member, authorities);
	    }
	
	    private String getAuthorityByRank(Long rankNo) {
	        switch (rankNo.intValue()) {
	            case 1:
	                return "ROLE_사원";
	            case 2:
	                return "ROLE_대리";
	            case 3:
	                return "ROLE_과장";
	            case 4:
	                return "ROLE_부장";
	            case 5:
	                return "ROLE_차장";
	            case 6:
	                return "ROLE_지점대표";
	            case 7:
	                return "ROLE_대표";
	            default:
	                return "ROLE_알수없음";
	        }
	    }

	}
