package com.community.repository;

import com.community.dto.MessageResponse;
import com.community.entity.Conversation;
import com.community.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
	
	@Query("""
		    select new com.community.dto.MessageResponse(
		        m.id,
		        m.content,
		        m.sentAt,
		        s.userId,
		        s.fullName,
		        s.email,
		        o.email,
		        r.title
		    )
		    from Message m
		    join m.sender s
		    join m.conversation c
		    join c.owner o
		    join c.booking b
		    join b.resource r
		    where c.id = :conversationId
		    order by m.sentAt asc
		""")
		List<MessageResponse> findMessagesByConversationId(@Param("conversationId") Long conversationId);

	List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);

}
