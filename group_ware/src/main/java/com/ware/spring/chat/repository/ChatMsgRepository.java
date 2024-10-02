package com.ware.spring.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ware.spring.chat.domain.ChatMsg;
import com.ware.spring.chat.domain.ChatMsgDto;
import com.ware.spring.chat.domain.ChatRoom;

import jakarta.transaction.Transactional;

public interface ChatMsgRepository extends JpaRepository<ChatMsg, Long>{
	
	List<ChatMsg> findAllBychatRoom(ChatRoom chatRoom);
	

	
}
