package com.example.demo.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Post;
import com.example.demo.payload.PostNumberCount;
import com.example.demo.payload.PostResponse;
import com.example.demo.repository.ChoiceRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.GroupPostingRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.LikeRepository;
import com.example.demo.repository.ParticipantRepository;
import com.example.demo.repository.PollRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.PremiumRepository;
import com.example.demo.repository.RelationshipRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.TaggingRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VoteRepository;
import com.example.demo.security.UserPrincipal;

@Service
public class NotificationService {

	private final SimpMessagingTemplate template;
	private final SimpUserRegistry simpUserRegistry;

	// private Set<String> listeners = new HashSet<>();
//    private Map<Long,Long> mapChoice= new HashMap<Long,Long>();
	private Map<Long, Set<String>> mapPost = new HashMap<Long, Set<String>>();

//    private Map<String,String> mapUser = new HashMap<String,String>();
//    private Map<String,String> mapSession = new HashMap<String,String>();
	public NotificationService(SimpMessagingTemplate template, SimpUserRegistry simpUserRegistry) {
		this.template = template;
		this.simpUserRegistry = simpUserRegistry;
	}

	public void add(String username, Long id) {
//        listeners.add(sessionId);
//		System.out.println("aloaloaloalaoalao");
		if (mapPost.containsKey(id)) {
			mapPost.get(id).add(username);

		} else {
			Set<String> map = new HashSet<String>();
			map.add(username);
			mapPost.put(id, map);
		}
//        mapUser.put(sessionId, currentUser.getUsername());
//        mapSession.put(currentUser.getUsername(), sessionId);
	}
	public void remove(String username) {
		// listeners.remove(sessionId);
		// map.remove(sessionId);
		Iterator<Map.Entry<Long, Set<String>>> i = mapPost.entrySet().iterator();
		while (i.hasNext()) {

			Map.Entry<Long, Set<String>> entry = i.next();
			if (entry.getValue().contains(username)) {
				Set<String> newEntry = entry.getValue();
				newEntry.remove(username);
				entry.setValue(newEntry);
				if (entry.getValue().isEmpty()) {
					i.remove();
					mapPost.remove(entry.getKey());
				}
			}
		}

//    	for (Map.Entry<Long,Set<String>> entry : mapPost.entrySet()) {
//    		if(entry.getValue().contains(username)) {
//    			Set<String> newEntry = entry.getValue();
//    			newEntry.remove(username);
//    			entry.setValue(newEntry);
//    			if(entry.getValue().isEmpty()) {
//    				mapPost.remove(entry.getKey());
//    			}
//    		}
//    	}
//    	mapUser.remove(sessionId);
//    	mapSession.remove(username);
	}

	public void dispatch(String username, Object object, Long id) {
		if (mapPost.containsKey(id)) {
			for (String idU : mapPost.get(id)) {
				if (!idU.equals(username))
					template.convertAndSendToUser(idU, "/queue/item", object);
			}
		}
	}

	public boolean dispatch2(String username, Object object,String currentUsername) {
//		try {
//		template.convertAndSendToUser(username, "/queue/item", object);
//		System.out.println("ok");
//		return true;
//		}catch(MessagingException e) {
//			System.out.println(username + " not loggin");
//			return false;
//		}
		
		Iterator<SimpUser> i = simpUserRegistry.getUsers().stream().iterator();
		while (i.hasNext()) {

			SimpUser user = i.next();
			UserPrincipal users = (UserPrincipal) ((Authentication) user.getPrincipal()).getPrincipal();
			System.out.println(users.getUsername());
			if (users.getUsername().equals(username)&&!users.getUsername().equals(currentUsername)) {
				template.convertAndSendToUser(username, "/queue/item", object);
				return true;
			}
		}
		return false;
	}

	@EventListener
	public void sessionDisconnectionHandler(SessionDisconnectEvent event) {
//        String sessionId = event.getSessionId();
//        LOGGER.info("Disconnecting " + sessionId + "!");
//        String username = mapUser.get(sessionId);
		UserPrincipal user = (UserPrincipal) ((Authentication) event.getUser()).getPrincipal();
		System.out.println(user.getUsername());
		remove(user.getUsername());
	}
}
