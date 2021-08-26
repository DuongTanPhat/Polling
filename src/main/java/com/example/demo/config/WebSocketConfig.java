package com.example.demo.config;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.DefaultUserDestinationResolver;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.simp.user.UserDestinationResolver;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.DefaultSimpUserRegistry;

import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.security.UserPrincipal;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{
//	@Autowired
//	private DefaultSimpUserRegistry userRegistry = new DefaultSimpUserRegistry();
//	@Autowired  
//	private DefaultUserDestinationResolver resolver = new DefaultUserDestinationResolver(userRegistry);
	  private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

		@Autowired
	    private JwtTokenProvider tokenProvider;
		 @Autowired
		    private CustomUserDetailsService customUserDetailsService;
	    
//	  @Bean
//	  @Primary
//	  public SimpUserRegistry userRegistry() {
//	    return userRegistry;
//	  }
//
//	  @Bean
//	  @Primary
//	  public UserDestinationResolver userDestinationResolver() {
//	    return resolver;
//	  }
 
	@Override
	    public void registerStompEndpoints(StompEndpointRegistry registry) {
	        registry.addEndpoint("/api/ws").setAllowedOriginPatterns("*").withSockJS();
	    }

	    @Override
	    public void configureMessageBroker(MessageBrokerRegistry registry) {
	        registry.setApplicationDestinationPrefixes("/app");
	        registry.enableSimpleBroker("/topic","/queue");
	    }
	    @Override
	    public void configureClientInboundChannel(ChannelRegistration registration) {
	        registration.interceptors(new ChannelInterceptor() {
	            @Override
	            public Message<?> preSend(Message<?> message, MessageChannel channel) {
	                StompHeaderAccessor accessor =
	                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
	                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
	                    List<String> authorization = accessor.getNativeHeader("Authorization");
	                    logger.debug("X-Authorization: {}", authorization);
	                    String jwt="";
	                    if (StringUtils.hasText(authorization.get(0)) && authorization.get(0).startsWith("Bearer ")) {
	                    	jwt =  authorization.get(0).substring(7, authorization.get(0).length());
	                    }
	                    //String accessToken = authorization.get(0).split(" ")[1];
	                    if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
	                        Long userId = tokenProvider.getUserIdFromJWT(jwt);

	                        UserPrincipal userDetails = customUserDetailsService.loadUserById2(userId);
	                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, jwt, userDetails.getAuthorities());
	                        //authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	                        
//	                        if (accessor.getMessageType() == SimpMessageType.CONNECT) {
//	                            userRegistry.onApplicationEvent(SessionConnectedEvent(this, message, (Pri)authentication));
//	                          } else if (accessor.getMessageType() == SimpMessageType.SUBSCRIBE) {
//	                            userRegistry.onApplicationEvent(SessionSubscribeEvent(this, message, authentication));
//	                          } else if (accessor.getMessageType() == SimpMessageType.UNSUBSCRIBE) {
//	                            userRegistry.onApplicationEvent(SessionUnsubscribeEvent(this, message, authentication));
//	                          } else if (accessor.getMessageType() == SimpMessageType.DISCONNECT) {
//	                            userRegistry.onApplicationEvent(SessionDisconnectEvent(this, message, accessor.getSessionId(), CloseStatus.NORMAL));
//	                          }
	                        //UserPrincipal pri = (UserPrincipal) userDetails;
	                        
	                        //JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
	                        //Authentication authentication = converter.convert(jwt);
//	                        accessor.setUser((Principal) userDetails);
	                        accessor.setUser(authentication);
	                        accessor.setLeaveMutable(true);
	                    }
	                    
	                    //Jwt jwt = jwtDecoder.decode(accessToken);
	                    
	                }
	                
	                return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
	                //return message;
	            }
	        });
	    }
}
