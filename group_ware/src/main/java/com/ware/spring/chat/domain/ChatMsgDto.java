package com.ware.spring.chat.domain;

import java.time.LocalDateTime;

import com.ware.spring.member.domain.Member;

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
@Builder
@ToString
public class ChatMsgDto {

	private Long message_no;
	private Long room_no;
	private String chat_content;
	private String is_from_sender;
	private String is_receiver_read;
	private LocalDateTime send_date;
	
	private ChatRoom chatRoom;
	
	private String chat_type;
	private String sender_id;
	private String receiver_id;
	private String me_flag;
	
	public ChatMsg toEntity() {
		
		return ChatMsg.builder()
				.messageNo(message_no)

				.chatRoom(chatRoom)
				.chatContent(chat_content)
				.isFromSender(is_from_sender)
				.isReceiverRead(is_receiver_read)
				.sendDate(send_date)
				.build();
	}
	
	public ChatMsgDto toDto(ChatMsg chatMsg) {
		
		return ChatMsgDto.builder()
				.message_no(chatMsg.getMessageNo())

				.chatRoom(chatMsg.getChatRoom())
				.chat_content(chatMsg.getChatContent())
				.is_from_sender(chatMsg.getIsFromSender())
				.is_receiver_read(chatMsg.getIsReceiverRead())
				.send_date(chatMsg.getSendDate())
				.build();
	}
}
