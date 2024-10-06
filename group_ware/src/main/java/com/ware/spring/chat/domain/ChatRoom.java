package com.ware.spring.chat.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_room")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Builder
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="room_no")
	private Long roomNo;
	
	@Column(name="room_name")
	private String roomName;
	 
	@Column(name="from_id")
	private String fromId;
	
	@Column(name="to_id")
	private String toId;
	
	@Column(name="is_group")
	private String isGroup = "N";
	
	@Column(name = "room_status")
	private String roomStatus = "A"; // status A = Active , P = Pending, C = Closed
	
	@Column(name = "room_reg_date")
	@CreationTimestamp
	private LocalDateTime roomRegDate;
	
	@Column(name = "last_msg")
	private String lastMsg;
	
	@Column(name = "last_date")
	private LocalDateTime lastDate;
	
	@OneToMany(mappedBy = "chatRoom")
	private List<ChatMsg> chatMsg;
}
