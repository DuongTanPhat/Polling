package com.example.demo.util;

import com.example.demo.model.Poll;
import com.example.demo.model.User;
import com.example.demo.model.Vote;
import com.example.demo.payload.ChoiceResponse;
import com.example.demo.payload.PollResponse;
import com.example.demo.payload.UserSummary;
import com.example.demo.repository.PollRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VoteRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

public class ModelMapper {

//    public static PollResponse mapPollToPollResponse(Poll poll) {
//        PollResponse pollResponse = new PollResponse();
//        pollResponse.setId(poll.getId());
//        pollResponse.setQuestion(poll.getQuestion());
//        
//        pollResponse.setPhoto(poll.getPhoto());
//        pollResponse.setCreationDateTime(poll.getCreatedAt());
//        pollResponse.setExpirationDateTime(poll.getExpirationDateTime());
//        Instant now = Instant.now();
//        pollResponse.setExpired(poll.getExpirationDateTime().isBefore(now));
//        
//        pollResponse.setUnseenUserForVote(poll.isUnseenUserForVote());
//        pollResponse.setUnseenUserForAddChoice(poll.isUnseenUserForAddChoice());
//        pollResponse.setUnseenOwner(poll.isUnseenOwner());
//        pollResponse.setAddChoice(poll.isAddChoice());
//        pollResponse.setCanFix(poll.isCanFix());
//        
//        pollResponse.setShowResultCase(poll.getShowResultCase());
//        pollResponse.setMaxVotePerTimeLoad(poll.getMaxVotePerTimeLoad());
//        pollResponse.setMaxVotePerChoice(poll.getMaxVotePerChoice());
//        pollResponse.setMaxVoteOfPoll(poll.getMaxVoteOfPoll());
//        pollResponse.setTimeLoad(poll.getTimeLoad());
//
//        
//        List<ChoiceResponse> choiceResponses = poll.getChoices().stream().map(choice -> {
//            ChoiceResponse choiceResponse = new ChoiceResponse();
//            choiceResponse.setId(choice.getId());
//            choiceResponse.setText(choice.getText());
//            choiceResponse.setCreationDateTime(choice.getCreatedAt());
//            
////            if(userVote.containsKey(choice.getId())) {
////                choiceResponse.setVoteCount(choiceVotesMap.get(choice.getId()));
////            } else {
////                choiceResponse.setVoteCount(0);
////            }
////            if(choiceVotesMap.containsKey(choice.getId())) {
////                choiceResponse.setVoteCount(choiceVotesMap.get(choice.getId()));
////            } else {
////                choiceResponse.setVoteCount(0);
////            }
//            return choiceResponse;
//        }).collect(Collectors.toList());
//
//        pollResponse.setChoices(choiceResponses);
//        
//        long totalVotes = pollResponse.getChoices().stream().mapToLong(ChoiceResponse::getVoteCount).sum();
//        pollResponse.setTotalVotes(totalVotes);
//        
////        UserSummary creatorSummary = new UserSummary(creator.getId(), creator.getUsername(), creator.getName(),creator.getPhoto());
////        pollResponse.setCreatedBy(creatorSummary);
////
////        if(userVote != null) {
////        	List<Long> sle = userVote.stream().map(idc ->{
////        		return idc.getChoice().getId();
////        	}).collect(Collectors.toList());
////            pollResponse.setSelectedChoice(sle);
////        }
//
//        
//
//        return pollResponse;
//    }
}
