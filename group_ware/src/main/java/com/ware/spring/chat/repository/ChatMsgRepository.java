package com.ware.spring.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ware.spring.chat.domain.ChatMsg;
import com.ware.spring.chat.domain.ChatRoom;


public interface ChatMsgRepository extends JpaRepository<ChatMsg, Long>{
	
	List<ChatMsg> findAllBychatRoom(ChatRoom chatRoom);
	
	@Query(value="SELECT * FROM chat_msg "+
			"WHERE room_no = ?1 " +
			"ORDER BY send_date DESC LIMIT 1 ", nativeQuery = true)
	ChatMsg findByRoomNoOne(Long roomNo); //특정 멤버 채팅방 목록
	
	@Query(value = "SELECT * FROM chat_msg " +
            "WHERE room_no = ?1 " +
            "AND is_from_sender = 'N'", nativeQuery = true)
	ChatMsg findByReceiverId(String receiverId);
	
	 List<ChatMsg> findAllByChatRoom(ChatRoom chatRoom);
}
