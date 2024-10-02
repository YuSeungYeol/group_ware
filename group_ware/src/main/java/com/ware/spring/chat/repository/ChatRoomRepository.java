package com.ware.spring.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ware.spring.chat.domain.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>{
	
	@Query(value="SELECT cr FROM ChatRoom cr "+
			"WHERE cr.fromId = ?1 " +
			"OR cr.toId = ?1 ",
		countQuery="SELECT COUNT(cr) FROM ChatRoom cr " +
				"WHERE cr.fromId = ?1 " +
				"OR cr.toId = ?1 ")
	Page<ChatRoom> findAllByfromIdAndtoId(String memId, Pageable pageable); //특정 멤버 채팅방 목록
	
	ChatRoom findByroomNo(Long roomNo);
}
