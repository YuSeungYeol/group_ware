package com.ware.spring.chat.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ChatRoomDto {
	
	      private Long room_no;
	      private String room_name;
	      private String from_id;
	      private String to_id;
	      private String is_group;
	      private String room_status;
	      private LocalDateTime room_reg_date;
	      private String last_msg;
	      private LocalDateTime last_date;
	      
	      private String not_me_name;
	      private String not_me_id;
	      
	      private String is_receiver_read;
	      
	      public ChatRoom toEntity() {
	    	  
	    	  return ChatRoom.builder()
	    			  .roomNo(room_no)
	    			  .roomName(room_name)
	    			  .fromId(from_id)
	    			  .toId(to_id)
	    			  .isGroup(is_group != null ? is_group : "N")
	    			  .roomStatus(room_status != null ? room_status : "A")
	    			  .roomRegDate(room_reg_date)
	    			  .lastMsg(last_msg)
	    			  .lastDate(last_date)
	    			  .build();
	      }
	      
	      public ChatRoomDto toDto(ChatRoom chatRoom) {
	    	  
	    	  return ChatRoomDto.builder()
	    			  .room_no(chatRoom.getRoomNo())
	    			  .room_name(chatRoom.getRoomName())
	    			  .from_id(chatRoom.getFromId())
	    			  .to_id(chatRoom.getToId())
	    			  .is_group(chatRoom.getIsGroup())
	    			  .room_status(chatRoom.getRoomStatus())
	    			  .room_reg_date(chatRoom.getRoomRegDate())
	    			  .last_date(chatRoom.getLastDate())
	    			  .last_msg(chatRoom.getLastMsg())
	    			  .build();
	      }
}
