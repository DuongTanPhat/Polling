package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.payload.ChoicePrimeResponse;
import com.example.demo.payload.ChoiceRequest;
import com.example.demo.payload.ChoiceResponse;
import com.example.demo.payload.ChoiceVoteCount;
import com.example.demo.payload.CommentResponse;
import com.example.demo.payload.ExPost;
import com.example.demo.payload.GroupRequest;
import com.example.demo.payload.GroupResponse;
import com.example.demo.payload.NotificationResponse;
import com.example.demo.payload.PostNumberCount;
import com.example.demo.payload.PostRequest;
import com.example.demo.payload.PagedResponse;
import com.example.demo.payload.PollRequest;
import com.example.demo.payload.PollResponse;
import com.example.demo.payload.PostResponse;
import com.example.demo.payload.ReplyResponse;
import com.example.demo.payload.SocketChoiceResponse;
import com.example.demo.payload.SocketDeleteChoice;
import com.example.demo.payload.SocketEndPoll;
import com.example.demo.payload.SocketLikeComment;
import com.example.demo.payload.SocketNotification;
import com.example.demo.payload.UserCountVoted;
import com.example.demo.payload.UserIdCount;
import com.example.demo.payload.UserProfile;
import com.example.demo.payload.UserSummary;
import com.example.demo.payload.VoteRequest;
import com.example.demo.repository.ChoiceRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.GroupPostingRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.LikeRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.ParticipantRepository;
import com.example.demo.repository.PollRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.PremiumRepository;
import com.example.demo.repository.RelationshipRepository;
import com.example.demo.repository.ReplyRepository;
import com.example.demo.repository.StoragePostRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.TaggingRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VoteRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.util.AppConstants;
import com.example.demo.util.ModelMapper;

import com.example.demo.exception.FileStorageException;
import com.example.demo.property.FileStorageProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class PostService {

	@Autowired
	private PollRepository pollRepository;

	@Autowired
	private GroupPostingRepository groupPostingRepository;

	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private StoragePostRepository storagePostRepository;

	@Autowired
	private RelationshipRepository relationshipRepository;

	@Autowired
	private PremiumRepository premiumRepository;

	@Autowired
	private TaggingRepository taggingRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private VoteRepository voteRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private ParticipantRepository participantRepository;

	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	private LikeRepository likeRepository;
	@Autowired
	private ReplyRepository replyRepository;
	@Autowired
	private ChoiceRepository choiceRepository;
	@Autowired
	private NotificationRepository notificationRepository;
	@Autowired
	private NotificationService notificationService;
	private static final Logger logger = LoggerFactory.getLogger(PostService.class);

	private final Path fileStorageLocation;

	@Autowired
	public PostService(FileStorageProperties fileStorageProperties) {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			throw new FileStorageException("Could not create the directory where the uploaded files will be stored.",
					ex);
		}
	}

	public List<User> getAllPollsRandom() {
		// Retrieve Polls
		List<User> polls = pollRepository.findByNum(Long.valueOf(1), PageRequest.of(0, 3));
		// Map Polls to PollResponses containing vote counts and poll creator details
		return polls;
	}

	public PagedResponse<PostResponse> getAllPolls(UserPrincipal currentUser, int page, int size) {
		validatePageNumberAndSize(page, size);

		// Retrieve Polls
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Post> posts = postRepository.findAllOfUser(currentUser.getId(), pageable);

		if (posts.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), posts.getNumber(), posts.getSize(),
					posts.getTotalElements(), posts.getTotalPages(), posts.isLast());
		}
		// Map Polls to PollResponses containing vote counts and poll creator details
		List<Long> postIds = posts.map(Post::getId).getContent();
		postIds.forEach((postId) -> {
			notificationService.add(currentUser.getUsername(), postId);
		});
		List<PostNumberCount> commentCount = commentRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> commentCountMap = commentCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostNumberCount> likeCount = likeRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> likeCountMap = likeCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostResponse> postResponses = posts.map(post -> {
			return mapPostToPostResponse(currentUser.getId(), post, commentCountMap.get(post.getId()),
					likeCountMap.get(post.getId()), userRepository, voteRepository, likeRepository);
		}).getContent();

		return new PagedResponse<>(postResponses, posts.getNumber(), posts.getSize(), posts.getTotalElements(),
				posts.getTotalPages(), posts.isLast());

//		Map<Long,List<PollResponse>> pollResponseMap = getPollResponseMap(posts.getContent());
//		
//		
//		
//		Map<Long, Long> choiceCommentCountMap = getChoiceVoteCountMap(pollIds);
//		Map<Long, Long> choiceLikeCountMap = getChoiceVoteCountMap(pollIds);
//		Map<Long, List<Vote>> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
//		Map<Long, User> creatorMap = getPostCreatorMap(posts.getContent());
//
//		List<PollResponse> pollResponses = polls.map(poll -> {
//			return ModelMapper.mapPollToPollResponse(poll, choiceVoteCountMap, creatorMap.get(poll.getCreatedBy()),
//					pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
//		}).getContent();
//
//		return new PagedResponse<>(pollResponses, polls.getNumber(), polls.getSize(), polls.getTotalElements(),
//				polls.getTotalPages(), polls.isLast());
		// Map Polls to PollResponses containing vote counts and poll creator details
//		List<Long> pollIds = polls.map(Poll::getId).getContent();
//		Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
//		Map<Long, List<Vote>> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
//		Map<Long, User> creatorMap = getPollCreatorMap(polls.getContent());
//
//		List<PollResponse> pollResponses = polls.map(poll -> {
//			return ModelMapper.mapPollToPollResponse(poll, choiceVoteCountMap, creatorMap.get(poll.getCreatedBy()),
//					pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
//		}).getContent();
//
//		return new PagedResponse<>(pollResponses, polls.getNumber(), polls.getSize(), polls.getTotalElements(),
//				polls.getTotalPages(), polls.isLast());
	}

	public PagedResponse<PostResponse> getAllPollsSearch(UserPrincipal currentUser, String search, int page, int size) {
		validatePageNumberAndSize(page, size);

		// Retrieve Polls
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Post> posts = postRepository.findAllOfUserSearch(currentUser.getId(), search, pageable);

		if (posts.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), posts.getNumber(), posts.getSize(),
					posts.getTotalElements(), posts.getTotalPages(), posts.isLast());
		}
		// Map Polls to PollResponses containing vote counts and poll creator details
		List<Long> postIds = posts.map(Post::getId).getContent();
		postIds.forEach((postId) -> {
			notificationService.add(currentUser.getUsername(), postId);
		});
		List<PostNumberCount> commentCount = commentRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> commentCountMap = commentCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostNumberCount> likeCount = likeRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> likeCountMap = likeCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostResponse> postResponses = posts.map(post -> {
			return mapPostToPostResponse(currentUser.getId(), post, commentCountMap.get(post.getId()),
					likeCountMap.get(post.getId()), userRepository, voteRepository, likeRepository);
		}).getContent();

		return new PagedResponse<>(postResponses, posts.getNumber(), posts.getSize(), posts.getTotalElements(),
				posts.getTotalPages(), posts.isLast());
	}

	public PagedResponse<PostResponse> getAllPollsSave(UserPrincipal currentUser, int page, int size) {
		validatePageNumberAndSize(page, size);

		// Retrieve Polls
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Post> posts = storagePostRepository.findAllOfSave(currentUser.getId(), pageable);

		if (posts.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), posts.getNumber(), posts.getSize(),
					posts.getTotalElements(), posts.getTotalPages(), posts.isLast());
		}
		// Map Polls to PollResponses containing vote counts and poll creator details
		List<Long> postIds = posts.map(Post::getId).getContent();
		postIds.forEach((postId) -> {
			notificationService.add(currentUser.getUsername(), postId);
		});
		List<PostNumberCount> commentCount = commentRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> commentCountMap = commentCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostNumberCount> likeCount = likeRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> likeCountMap = likeCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostResponse> postResponses = posts.map(post -> {
			return mapPostToPostResponse(currentUser.getId(), post, commentCountMap.get(post.getId()),
					likeCountMap.get(post.getId()), userRepository, voteRepository, likeRepository);
		}).getContent();

		return new PagedResponse<>(postResponses, posts.getNumber(), posts.getSize(), posts.getTotalElements(),
				posts.getTotalPages(), posts.isLast());
	}

	public PagedResponse<PostResponse> getOnePolls(UserPrincipal currentUser, Long postId) {

		// Retrieve Polls
		Pageable pageable = PageRequest.of(0, 1, Sort.Direction.DESC, "createdAt");
		Page<Post> posts = postRepository.findByIdOfUser(currentUser.getId(), postId, pageable);

		if (posts.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), posts.getNumber(), posts.getSize(),
					posts.getTotalElements(), posts.getTotalPages(), posts.isLast());
		}
		// Map Polls to PollResponses containing vote counts and poll creator details
		List<Long> postIds = posts.map(Post::getId).getContent();
		postIds.forEach((id) -> {
			notificationService.add(currentUser.getUsername(), id);
		});
		List<PostNumberCount> commentCount = commentRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> commentCountMap = commentCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostNumberCount> likeCount = likeRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> likeCountMap = likeCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostResponse> postResponses = posts.map(post -> {
			return mapPostToPostResponse(currentUser.getId(), post, commentCountMap.get(post.getId()),
					likeCountMap.get(post.getId()), userRepository, voteRepository, likeRepository);
		}).getContent();

		return new PagedResponse<>(postResponses, posts.getNumber(), posts.getSize(), posts.getTotalElements(),
				posts.getTotalPages(), posts.isLast());
	}

	public PagedResponse<PostResponse> getOnePollsNoUser(Long postId) {

		// Retrieve Polls
		Pageable pageable = PageRequest.of(0, 1, Sort.Direction.DESC, "createdAt");
		Page<Post> posts = postRepository.findByIdNoUser(postId, pageable);

		if (posts.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), posts.getNumber(), posts.getSize(),
					posts.getTotalElements(), posts.getTotalPages(), posts.isLast());
		}
		// Map Polls to PollResponses containing vote counts and poll creator details
		List<Long> postIds = posts.map(Post::getId).getContent();
		List<PostNumberCount> commentCount = commentRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> commentCountMap = commentCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostNumberCount> likeCount = likeRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> likeCountMap = likeCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostResponse> postResponses = posts.map(post -> {
			return mapPostToPostResponse(null, post, commentCountMap.get(post.getId()), likeCountMap.get(post.getId()),
					userRepository, voteRepository, likeRepository);
		}).getContent();

		return new PagedResponse<>(postResponses, posts.getNumber(), posts.getSize(), posts.getTotalElements(),
				posts.getTotalPages(), posts.isLast());
	}

	public PagedResponse<PostResponse> getAllPollsOfGroup(UserPrincipal currentUser, String code, int page, int size) {
		validatePageNumberAndSize(page, size);

		// Retrieve Polls
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Post> posts = groupPostingRepository.findAllOfGroup(code, pageable);

		if (posts.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), posts.getNumber(), posts.getSize(),
					posts.getTotalElements(), posts.getTotalPages(), posts.isLast());
		}
		// Map Polls to PollResponses containing vote counts and poll creator details
		List<Long> postIds = posts.map(Post::getId).getContent();
		postIds.forEach((postId) -> {
			notificationService.add(currentUser.getUsername(), postId);
		});
		List<PostNumberCount> commentCount = commentRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> commentCountMap = commentCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostNumberCount> likeCount = likeRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> likeCountMap = likeCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostResponse> postResponses = posts.map(post -> {
			return mapPostToPostResponse(currentUser.getId(), post, commentCountMap.get(post.getId()),
					likeCountMap.get(post.getId()), userRepository, voteRepository, likeRepository);
		}).getContent();

		return new PagedResponse<>(postResponses, posts.getNumber(), posts.getSize(), posts.getTotalElements(),
				posts.getTotalPages(), posts.isLast());

//		Map<Long,List<PollResponse>> pollResponseMap = getPollResponseMap(posts.getContent());
//		
//		
//		
//		Map<Long, Long> choiceCommentCountMap = getChoiceVoteCountMap(pollIds);
//		Map<Long, Long> choiceLikeCountMap = getChoiceVoteCountMap(pollIds);
//		Map<Long, List<Vote>> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
//		Map<Long, User> creatorMap = getPostCreatorMap(posts.getContent());
//
//		List<PollResponse> pollResponses = polls.map(poll -> {
//			return ModelMapper.mapPollToPollResponse(poll, choiceVoteCountMap, creatorMap.get(poll.getCreatedBy()),
//					pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
//		}).getContent();
//
//		return new PagedResponse<>(pollResponses, polls.getNumber(), polls.getSize(), polls.getTotalElements(),
//				polls.getTotalPages(), polls.isLast());
		// Map Polls to PollResponses containing vote counts and poll creator details
//		List<Long> pollIds = polls.map(Poll::getId).getContent();
//		Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
//		Map<Long, List<Vote>> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
//		Map<Long, User> creatorMap = getPollCreatorMap(polls.getContent());
//
//		List<PollResponse> pollResponses = polls.map(poll -> {
//			return ModelMapper.mapPollToPollResponse(poll, choiceVoteCountMap, creatorMap.get(poll.getCreatedBy()),
//					pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
//		}).getContent();
//
//		return new PagedResponse<>(pollResponses, polls.getNumber(), polls.getSize(), polls.getTotalElements(),
//				polls.getTotalPages(), polls.isLast());
	}

	public PagedResponse<PostResponse> getAllPolls(int page, int size) {
		validatePageNumberAndSize(page, size);

		// Retrieve Polls
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Post> posts = postRepository.findAll(pageable);

		if (posts.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), posts.getNumber(), posts.getSize(),
					posts.getTotalElements(), posts.getTotalPages(), posts.isLast());
		}
		// Map Polls to PollResponses containing vote counts and poll creator details
		List<Long> postIds = posts.map(Post::getId).getContent();
		List<PostNumberCount> commentCount = commentRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> commentCountMap = commentCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostNumberCount> likeCount = likeRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> likeCountMap = likeCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostResponse> postResponses = posts.map(post -> {
			return mapPostToPostResponse(null, post, commentCountMap.get(post.getId()), likeCountMap.get(post.getId()),
					userRepository, voteRepository, likeRepository);
		}).getContent();

		return new PagedResponse<>(postResponses, posts.getNumber(), posts.getSize(), posts.getTotalElements(),
				posts.getTotalPages(), posts.isLast());
	}

	public PagedResponse<PostResponse> getAllPollsSearch(String search, int page, int size) {
		validatePageNumberAndSize(page, size);

		// Retrieve Polls
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Post> posts = postRepository.findAllSearch(search, pageable);

		if (posts.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), posts.getNumber(), posts.getSize(),
					posts.getTotalElements(), posts.getTotalPages(), posts.isLast());
		}
		// Map Polls to PollResponses containing vote counts and poll creator details
		List<Long> postIds = posts.map(Post::getId).getContent();
		List<PostNumberCount> commentCount = commentRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> commentCountMap = commentCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostNumberCount> likeCount = likeRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> likeCountMap = likeCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostResponse> postResponses = posts.map(post -> {
			return mapPostToPostResponse(null, post, commentCountMap.get(post.getId()), likeCountMap.get(post.getId()),
					userRepository, voteRepository, likeRepository);
		}).getContent();

		return new PagedResponse<>(postResponses, posts.getNumber(), posts.getSize(), posts.getTotalElements(),
				posts.getTotalPages(), posts.isLast());
	}

	@Transactional
	public Post createPost(UserPrincipal currentUser, PostRequest postRequest) {
		System.out.println(postRequest.getPublicDate());
		Post post = new Post();
		post.setQuestion(postRequest.getQuestion());
		if (postRequest.getPublicDate() != null) {
			Instant publicDate = LocalDateTime.parse( // Parse as an indeterminate `LocalDate`, devoid of time zone or
														// offset-from-UTC. NOT a moment, NOT a point on the timeline.
					postRequest.getPublicDate(), // This input uses a poor choice of format. Whenever possible, use
													// standard ISO 8601 formats when exchanging date-time values as
													// text. Conveniently, the java.time classes use the standard
													// formats by default when parsing/generating strings.
					DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.US) // Use single-character `M` & `d` when
																					// the number lacks a leading padded
																					// zero for single-digit values.
			) // Returns a `LocalDateTime` object.
					.atZone( // Apply a zone to that unzoned `LocalDateTime`, giving it meaning, determining
								// a point on the timeline.
							ZoneId.of("Asia/Ho_Chi_Minh") // Always specify a proper time zone with `Contintent/Region`
															// format, never a 3-4 letter pseudo-zone such as `PST`,
															// `CST`, or `IST`.
					) // Returns a `ZonedDateTime`. `toString` →
						// 2018-05-12T16:30-04:00[America/Toronto].
					.toInstant(); // Extract a `Instant` object, always in UTC by definition.
			post.setPublicDate(publicDate);
		}
		postRequest.getPolls().forEach(pollRequest -> {
			Poll poll = new Poll();
			poll.setQuestion(pollRequest.getQuestion());
			pollRequest.getChoices().forEach(choiceRequest -> {
				Choice choice = new Choice();
				choice.setText(choiceRequest.getText());
//				Premium p = premiumRepository.findByUserIdPremium(currentUser.getId());
//				System.out.println(p);
//				if (p != null) {
				if (choiceRequest.getPhoto() != null || choiceRequest.getReview() != null) {
					ChoicePrime choicePrime = new ChoicePrime();
					choicePrime.setPhoto(choiceRequest.getPhoto());
					choicePrime.setReview(choiceRequest.getReview());
					choicePrime.setChoice(choice);
					choice.setPrime(choicePrime);
				}
//				}

				poll.addChoice(choice);
			});
			poll.setPhoto(pollRequest.getPhoto());

			Instant now = Instant.now();
			if (post.getPublicDate() != null && post.getPublicDate().isAfter(now)) {
				if (pollRequest.getPollLength() != null) {
					Instant expirationDateTime = post.getPublicDate()
							.plus(Duration.ofDays(pollRequest.getPollLength().getDays()))
							.plus(Duration.ofHours(pollRequest.getPollLength().getHours()));
					poll.setExpirationDateTime(expirationDateTime);
				}

			} else {
				if (pollRequest.getPollLength() != null) {
					Instant expirationDateTime = now.plus(Duration.ofDays(pollRequest.getPollLength().getDays()))
							.plus(Duration.ofHours(pollRequest.getPollLength().getHours()));
					poll.setExpirationDateTime(expirationDateTime);
				}
			}

			poll.setUnseenUserForVote(pollRequest.getIsUnseenUserForVote());
			poll.setUnseenUserForAddChoice(pollRequest.getIsUnseenUserForAddChoice());
			poll.setAddChoice(pollRequest.getIsAddChoice());
			poll.setCanFix(pollRequest.getIsCanFix());
			poll.setShowResultCase(pollRequest.getShowResultCase());
			poll.setMaxVotePerTimeLoad(pollRequest.getMaxVotePerTimeLoad());
			poll.setMaxVotePerChoice(pollRequest.getMaxVotePerChoice());
			poll.setMaxVoteOfPoll(pollRequest.getMaxVoteOfPoll());
			poll.setTimeLoad(pollRequest.getTimeLoad().getDays() * 24 + pollRequest.getTimeLoad().getHours());
			post.addPoll(poll);
		});
		post.setShowCase(postRequest.getShowCase());
		post.setUnseenOwner(postRequest.getIsUnseenOwner());
//	Instant now = Instant.now();
//	Instant publicDate = now.plus(Duration.ofDays(postRequest.getPublicDate().getDays()))
//			.plus(Duration.ofHours(postRequest.getPublicDate().getHours()));

		postRepository.save(post);
		if (post.getQuestion().contains("#")) {
			String q = post.getQuestion();
			while (q.contains("#")) {
				int posTag = q.indexOf("#");
				int endTag = q.length();
				if (q.indexOf(" ", posTag) != -1) {
					endTag = q.indexOf(" ", posTag);
				}
				String tag = q.substring(posTag, endTag);
				System.out.println(tag);
				q = q.replaceAll(tag, "");
				Tag newTag = tagRepository.findByName(tag);
				if (newTag == null) {
					newTag = new Tag();
					newTag.setName(tag);
					Tag curTag = tagRepository.save(newTag);
					Tagging tagging = new Tagging();
					tagging.setPost(post);
					tagging.setTag(curTag);
					taggingRepository.save(tagging);
				} else {
					Tagging tagging = new Tagging();
					tagging.setPost(post);
					tagging.setTag(newTag);
					taggingRepository.save(tagging);

				}
			}
		}
		if (post.getShowCase() == 5) {
			if (postRequest.getGroups() != null && !postRequest.getGroups().isEmpty()) {
				postRequest.getGroups().forEach(groupCode -> {
					GroupPosting groupPosting = new GroupPosting();
					Group gr = groupRepository.findByGroupCode(groupCode)
							.orElseThrow(() -> new ResourceNotFoundException("Group", "Code", groupCode));
					groupPosting.setGroup(gr);
					groupPosting.setPost(post);
					List<Participant> p = gr.getParticipants();
					boolean check = true;
					Iterator<Participant> i = p.iterator();
					while (check && i.hasNext()) {
						Participant participant = i.next();
						if (participant.getUser().getId() == currentUser.getId()) {
							check = false;
						}
					}
					if (!check) {
						try {
							groupPostingRepository.save(groupPosting);
							sendNotiForGroup(groupPosting,currentUser,gr.getGroupCode()+" has new Post!");
						} catch (Exception e) {
							System.out.println(groupCode + " group can't add:" + e);
						}
					}
				});
			}
		}
		if (post.getShowCase() == 4) {
			if (postRequest.getUsersEmail() != null || postRequest.getUsersUsername() != null) {
//			if(postRequest.getUsersId()!=null) {
//				postRequest.getUsersId().forEach(userId->{
//					Relationship rl = new Relationship();
//					try {
//					User u = new User();
//					u.setId(userId);
//					rl.setUser(u);
//					rl.setPost(post);
//						relationshipRepository.save(rl);
//					}
//					catch (Exception e) {
//						System.out.println(userId + " user can't add: "+e);
//						throw new ResourceNotFoundException("User", "id", userId);
//						
//					}
//				});
//				
//			}
				if (postRequest.getUsersEmail() != null) {
					postRequest.getUsersEmail().forEach(userEmail -> {
						Relationship rl = new Relationship();
						try {
							User u = userRepository.findByEmail(userEmail)
									.orElseThrow(() -> new ResourceNotFoundException("User", "Email", userEmail));
							rl.setUser(u);
							rl.setPost(post);
							relationshipRepository.save(rl);
								SocketNotification socket = new SocketNotification();
								socket.setType(10);
								socket.setPostId(post.getId());
								socket.setName("You have a new liked Post from "+currentUser.getUsername()+" !");
									boolean check = notificationService.dispatch2(u.getUsername(), socket, currentUser.getUsername());
									Notification noti = new Notification();
									noti.setType(1);
									noti.setSourceId(post.getId());
									noti.setUser(u);
									noti.setContent(socket.getName());
									if (check)
										noti.setRead(true);
									else
										noti.setRead(false);
									notificationRepository.save(noti);
							
						} catch (Exception e) {
							System.out.println(userEmail + " user can't add:" + e);
						}
					});
				}
				if (postRequest.getUsersUsername() != null) {
					postRequest.getUsersUsername().forEach(username -> {
						Relationship rl = new Relationship();
						try {
							User u = userRepository.findByUsername(username)
									.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
							rl.setUser(u);
							rl.setPost(post);
							relationshipRepository.save(rl);
							SocketNotification socket = new SocketNotification();
							socket.setType(10);
							socket.setPostId(post.getId());
							socket.setName("You have a new liked Post from "+currentUser.getUsername()+" !");
								boolean check = notificationService.dispatch2(u.getUsername(), socket, currentUser.getUsername());
								Notification noti = new Notification();
								noti.setType(1);
								noti.setSourceId(post.getId());
								noti.setUser(u);
								noti.setContent(socket.getName());
								if (check)
									noti.setRead(true);
								else
									noti.setRead(false);
								notificationRepository.save(noti);
						} catch (Exception e) {
							System.out.println(username + " user can't add:" + e);
						}

					});
				}
				Relationship rl = new Relationship();
				User u = new User();
				u.setId(currentUser.getId());
				rl.setUser(u);
				rl.setPost(post);
				try {
					relationshipRepository.save(rl);
				} catch (Exception e) {
					System.out.println(currentUser.getId() + " user can't add:" + e);
				}
			}
		}
		return post;
	}

	public PagedResponse<UserCountVoted> getListUserVotedChoice(Long choiceId, UserPrincipal currentUser, int page,
			int size) {

		Choice choice = choiceRepository.findById(choiceId)
				.orElseThrow(() -> new ResourceNotFoundException("Choice", "id", choiceId));
		if (choice.getPoll().isUnseenUserForVote()
				&& currentUser.getId() != choice.getPoll().getPost().getCreatedBy()) {
			throw new BadRequestException("This choice cant see list user voted");
		}

		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "count");

		Page<UserCountVoted> pageCount = voteRepository.findUserVoteByChoiceId(choiceId, PageRequest.of(page, size));
		return new PagedResponse<>(pageCount.getContent(), pageCount.getNumber(), pageCount.getSize(),
				pageCount.getTotalElements(), pageCount.getTotalPages(), pageCount.isLast());
	}

	public List<UserSummary> getListUserFromEmail(String email, Long currentUser) {
		return userRepository.findByEmailStartsWith(email, currentUser, PageRequest.of(0, 5));
	}

	public List<UserSummary> getListUserFromUsername(String username, Long currentUser) {
		System.out.println(username);
		return userRepository.findByUsernameStartsWith(username, currentUser, PageRequest.of(0, 5));
	}

	public List<UserSummary> getListUserForAddGroupFromUsername(String username, String groupCode, Long currentUser) {
		System.out.println(username);
		return participantRepository.findByUsernameNotInStartsWith(username, groupCode, currentUser,
				PageRequest.of(0, 5));
	}

	public List<UserSummary> getListUserFromName(String name, Long currentUser) {
		return userRepository.findByNameContains(name, currentUser, PageRequest.of(0, 5));
	}

	@Transactional
	public Group createGroup(UserPrincipal currentUser, GroupRequest groupRequest) {
		User user = new User();
		user.setId(currentUser.getId());
		Group group = new Group(groupRequest.getName(), user, groupRequest.getCode());

		groupRepository.save(group);
		Participant participant = new Participant();
		participant.setGroup(group);

		participant.setUser(user);
		participantRepository.save(participant);
		if (groupRequest.getUsersUsername() != null) {
			groupRequest.getUsersUsername().forEach(username -> {
				Participant participant2 = new Participant();
				participant2.setGroup(group);
				try {
					User u = userRepository.findByUsername(username)
							.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
					participant2.setUser(u);

					participantRepository.save(participant2);
				} catch (Exception e) {
					System.out.println(username + " user can't add:" + e);
				}

			});
		}
		return group;
	}

	@Transactional
	public Group editGroup(UserPrincipal currentUser, GroupRequest groupRequest) {
		User user = new User();
		user.setId(currentUser.getId());
		Group group = groupRepository.findByGroupCode(groupRequest.getCode())
				.orElseThrow(() -> new ResourceNotFoundException("Group", "group_code", groupRequest.getCode()));

		if (currentUser.getId() != group.getGroupAdmin().getId()) {
			throw new BadRequestException("You aren't admin of this group");
		}
		group.setName(groupRequest.getName());
		if (groupRequest.getUsersUsername() != null) {
			groupRequest.getUsersUsername().forEach((username) -> {
				User u = userRepository.findByUsername(username)
						.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
				Participant participant = new Participant();
				participant.setGroup(group);
				participant.setUser(u);
				group.addParticipant(participant);
			});
		}
		try {
			groupRepository.save(group);
			// notificationService.dispatch(getSocketChoiceResponse(poll),
			// poll.getPost().getId());
		} catch (DataIntegrityViolationException ex) {

			throw new BadRequestException("Error to update this Group");

		}
//		
//		if (groupRequest.getUsersUsername() != null) {
//			groupRequest.getUsersUsername().forEach(username -> {
//				Participant participant2 = new Participant();
//				participant2.setGroup(group);
//				try {
//					User u = userRepository.findByUsername(username)
//							.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
//					participant2.setUser(u);
//					
//					participantRepository.save(participant2);
//				} catch (Exception e) {
//					System.out.println(username + " user can't add:" + e);
//				}
//
//			});
//		}
		return group;
	}

	public List<GroupResponse> getListGroupFromName(String name, Long currentUser) {
		return participantRepository.findGroupbyName(name, currentUser, PageRequest.of(0, 5));
	}

	public GroupResponse getGroupFromCode(String code, Long currentUser) {
		Group gr = groupRepository.findByGroupCode(code)
				.orElseThrow(() -> new ResourceNotFoundException("Group", "group_code", code));
		GroupResponse groupResponse = new GroupResponse(gr.getId(), gr.getName(), code, gr.getCreatedAt(),
				gr.getGroupAdmin().getUsername());
		gr.getParticipants().forEach((participant) -> {
			UserSummary member = new UserSummary(participant.getUser().getId(), participant.getUser().getUsername(),
					participant.getUser().getName(), participant.getUser().getPhoto());
			groupResponse.addMember(member);
		});
		return groupResponse;
	}

	public PagedResponse<GroupResponse> getListGroup(Long currentUser, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<GroupResponse> groupResponse = participantRepository.findGroupByUser(currentUser, pageable);
		return new PagedResponse<>(groupResponse.getContent(), groupResponse.getNumber(), groupResponse.getSize(),
				groupResponse.getTotalElements(), groupResponse.getTotalPages(), groupResponse.isLast());
	}

	public PagedResponse<PostResponse> getPostsCreatedBy(String username, UserPrincipal currentUser, int page,
			int size) {
		validatePageNumberAndSize(page, size);
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
		// Retrieve Polls
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Post> posts;
		if (currentUser != null && !currentUser.getUsername().equals(username)) {
			posts = postRepository.findPollsCreatedByUser(currentUser.getId(), user.getId(), pageable);
		} else if (currentUser != null && currentUser.getUsername().equals(username))
			posts = postRepository.findPollsCreatedByMe(currentUser.getId(), pageable);
		else
			posts = postRepository.findPollsCreatedByUser(Long.valueOf(-1), user.getId(), pageable);
		if (posts.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), posts.getNumber(), posts.getSize(),
					posts.getTotalElements(), posts.getTotalPages(), posts.isLast());
		}
		// Map Polls to PollResponses containing vote counts and poll creator details
		List<Long> postIds = posts.map(Post::getId).getContent();
		List<PostNumberCount> commentCount = commentRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> commentCountMap = commentCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostNumberCount> likeCount = likeRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> likeCountMap = likeCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));
		List<PostResponse> postResponses;
		if (currentUser != null) {
			postResponses = posts.map(post -> {
				return mapPostToPostResponse(currentUser.getId(), post, commentCountMap.get(post.getId()),
						likeCountMap.get(post.getId()), userRepository, voteRepository, likeRepository);
			}).getContent();
		}

		else {
			postResponses = posts.map(post -> {
				return mapPostToPostResponse(null, post, commentCountMap.get(post.getId()),
						likeCountMap.get(post.getId()), userRepository, voteRepository, likeRepository);
			}).getContent();
		}

		return new PagedResponse<>(postResponses, posts.getNumber(), posts.getSize(), posts.getTotalElements(),
				posts.getTotalPages(), posts.isLast());
	}

	public PagedResponse<PostResponse> getPostsVotedBy(String username, UserPrincipal currentUser, int page, int size) {
		validatePageNumberAndSize(page, size);
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
		// Retrieve Polls
		Pageable pageable = PageRequest.of(page, size);
		Page<ExPost> posts;
		if (currentUser != null && !currentUser.getUsername().equals(username)) {
			// posts = null;
			posts = voteRepository.findVotedPostByUserId(currentUser.getId(), user.getId(), pageable);
		} else if (currentUser != null && currentUser.getUsername().equals(username))
			posts = voteRepository.findVotedPostByMe(currentUser.getId(), pageable);
		else
			posts = voteRepository.findVotedPostByUserId(Long.valueOf(-1), user.getId(), pageable);
		if (posts.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), posts.getNumber(), posts.getSize(),
					posts.getTotalElements(), posts.getTotalPages(), posts.isLast());
		}
		// Map Polls to PollResponses containing vote counts and poll creator details
		// List<Long> postIds = posts.map(Post::getId).getContent();
		List<Long> postIds = posts.map(ExPost::getId).getContent();
		List<PostNumberCount> commentCount = commentRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> commentCountMap = commentCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostNumberCount> likeCount = likeRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> likeCountMap = likeCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));
		List<PostResponse> postResponses;
		if (currentUser != null)
			postResponses = posts.map(post -> {
				return mapPostToPostResponse(currentUser.getId(), post.getPost(), commentCountMap.get(post.getId()),
						likeCountMap.get(post.getId()), userRepository, voteRepository, likeRepository);
			}).getContent();
		else
			postResponses = posts.map(post -> {
				return mapPostToPostResponse(null, post.getPost(), commentCountMap.get(post.getId()),
						likeCountMap.get(post.getId()), userRepository, voteRepository, likeRepository);
			}).getContent();
		return new PagedResponse<>(postResponses, posts.getNumber(), posts.getSize(), posts.getTotalElements(),
				posts.getTotalPages(), posts.isLast());
	}

//
//	public PagedResponse<PollResponse> getPollsCreatedBy(String username, UserPrincipal currentUser, int page,
//			int size) {
//		validatePageNumberAndSize(page, size);
//
//		User user = userRepository.findByUsername(username)
//				.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
//
//		// Retrieve all polls created by the given username
//		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
//		Page<Poll> polls = pollRepository.findByCreatedBy(user.getId(), pageable);
//
//		if (polls.getNumberOfElements() == 0) {
//			return new PagedResponse<>(Collections.emptyList(), polls.getNumber(), polls.getSize(),
//					polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
//		}
//
//		// Map Polls to PollResponses containing vote counts and poll creator details
//		List<Long> pollIds = polls.map(Poll::getId).getContent();
//		Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
//		Map<Long, List<Vote>> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
//
//		List<PollResponse> pollResponses = polls.map(poll -> {
//			return ModelMapper.mapPollToPollResponse(poll, choiceVoteCountMap, user,
//					pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
//		}).getContent();
//
//		return new PagedResponse<>(pollResponses, polls.getNumber(), polls.getSize(), polls.getTotalElements(),
//				polls.getTotalPages(), polls.isLast());
//	}
//
//	public PagedResponse<PollResponse> getPollsVotedBy(String username, UserPrincipal currentUser, int page, int size) {
//		validatePageNumberAndSize(page, size);
//
//		User user = userRepository.findByUsername(username)
//				.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
//
//		// Retrieve all pollIds in which the given username has voted
//		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
//		Page<Long> userVotedPollIds = voteRepository.findVotedPollIdsByUserId(user.getId(), pageable);
//		if (userVotedPollIds.getNumberOfElements() == 0) {
//			return new PagedResponse<>(Collections.emptyList(), userVotedPollIds.getNumber(),
//					userVotedPollIds.getSize(), userVotedPollIds.getTotalElements(), userVotedPollIds.getTotalPages(),
//					userVotedPollIds.isLast());
//		}
//		// Retrieve all poll details from the voted pollIds.
//		List<Long> pollIds = userVotedPollIds.getContent();
//		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
//		List<Poll> polls = pollRepository.findByIdIn(pollIds, sort);
//		// Map Polls to PollResponses containing vote counts and poll creator details
//		Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
//		Map<Long, List<Vote>> pollUserVoteMap = getPollUserVoteMap2(currentUser, pollIds);
//		Map<Long, User> creatorMap = getPollCreatorMap(polls);
//		List<PollResponse> pollResponses = polls.stream().map(poll -> {
//			return ModelMapper.mapPollToPollResponse(poll, choiceVoteCountMap, creatorMap.get(poll.getCreatedBy()),
//					pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
//		}).collect(Collectors.toList());
//		return new PagedResponse<>(pollResponses, userVotedPollIds.getNumber(), userVotedPollIds.getSize(),
//				userVotedPollIds.getTotalElements(), userVotedPollIds.getTotalPages(), userVotedPollIds.isLast());
//	}
//	
//	public Poll createPoll(PollRequest pollRequest,MultipartFile photo) {
//		Poll poll = new Poll();
//		poll.setQuestion(pollRequest.getQuestion());
//
//		pollRequest.getChoices().forEach(choiceRequest -> {
//			poll.addChoice(new Choice(choiceRequest.getText()));
//		});
//
//		Instant now = Instant.now();
//		Instant expirationDateTime = now.plus(Duration.ofDays(pollRequest.getPollLength().getDays()))
//				.plus(Duration.ofHours(pollRequest.getPollLength().getHours()));
//
//		poll.setExpirationDateTime(expirationDateTime);
//		poll.setAnonymousUser(pollRequest.getIsAnonymousUser());
//		poll.setAddChoice(pollRequest.getIsAddChoice());
//		poll.setCanFix(pollRequest.getIsCanFix());
//		poll.setMaxVote(pollRequest.getMaxVote());
//		if(pollRequest.getMaxVote()<pollRequest.getMaxVotePerChoice()&&pollRequest.getTimeLoad()==0)poll.setMaxVotePerChoice(pollRequest.getMaxVote());
//		else
//		poll.setMaxVotePerChoice(pollRequest.getMaxVotePerChoice());
//		poll.setTimeLoad(pollRequest.getTimeLoad());
//		
//		
//		String fileName = StringUtils.cleanPath(photo.getOriginalFilename());
//		String newFileName = "";
//		try {
//			if(fileName.contains("..")) {
//				throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
//				
//			}
//			String fileExtension = "";
//			try {
//				fileExtension = fileName.substring(fileName.lastIndexOf("."));
//			}catch(Exception e) {
//				fileExtension ="";
//			}
//			
//			UUID uuid = UUID.randomUUID();
//			newFileName = uuid.toString()+fileExtension;
//			String name = "photo" + File.separatorChar + newFileName;
//			Path targetLocation = this.fileStorageLocation.resolve(name);
//			while(new File(targetLocation.toString()).isFile()) {
//				uuid = UUID.randomUUID();
//				newFileName = uuid.toString()+fileExtension;
//				name = "photo" + File.separatorChar + newFileName;
//				targetLocation = this.fileStorageLocation.resolve(name);
//			}
//			
////			newFileName = poll.getId()+fileExtension;
////			Path targetLocation = this.fileStorageLocation.resolve(newFileName);
//			System.out.println(targetLocation.toString());
//			Files.copy(photo.getInputStream(), targetLocation,StandardCopyOption.REPLACE_EXISTING);
//			poll.setPhoto(newFileName);
//			return pollRepository.save(poll);
//		}catch(IOException ex) {
//			throw new FileStorageException("Could not store file " + newFileName + ". Please try again!", ex);
//		}
//	}
////	public Poll createPoll(PollRequest pollRequest) {
////		Poll poll = new Poll();
////		poll.setQuestion(pollRequest.getQuestion());
////
////		pollRequest.getChoices().forEach(choiceRequest -> {
////			poll.addChoice(new Choice(choiceRequest.getText()));
////		});
////
////		Instant now = Instant.now();
////		Instant expirationDateTime = now.plus(Duration.ofDays(pollRequest.getPollLength().getDays()))
////				.plus(Duration.ofHours(pollRequest.getPollLength().getHours()));
////
////		poll.setExpirationDateTime(expirationDateTime);
////		poll.setAnonymousUser(pollRequest.getIsAnonymousUser());
////		poll.setAddChoice(pollRequest.getIsAddChoice());
////		poll.setCanFix(pollRequest.getIsCanFix());
////		poll.setMaxVote(pollRequest.getMaxVote());
////		if(pollRequest.getMaxVote()<pollRequest.getMaxVotePerChoice()&&pollRequest.getTimeLoad()==0)poll.setMaxVotePerChoice(pollRequest.getMaxVote());
////		else
////		poll.setMaxVotePerChoice(pollRequest.getMaxVotePerChoice());
////		poll.setTimeLoad(pollRequest.getTimeLoad());
////		return pollRepository.save(poll);
////	}
	public PostResponse addChoice(Long pollId, ChoiceRequest choiceRequest, UserPrincipal currentUser) {
		Poll poll = pollRepository.findById(pollId)
				.orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));
		if (poll.getExpirationDateTime() != null && poll.getExpirationDateTime().isBefore(Instant.now())) {
			throw new BadRequestException("Sorry! This Poll has already expired");
		}
//		User creator = userRepository.findById(poll.getPost().getCreatedBy())
//				.orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getPost().getCreatedBy()));
//		User user = userRepository.getOne(currentUser.getId());
		if (poll.getPost().getCreatedBy() != currentUser.getId() && !poll.isAddChoice())
			throw new BadRequestException("Sorry! You can't add this choice!");

		Choice choice = new Choice();
		choice.setPoll(poll);
		choice.setText(choiceRequest.getText());
		if (choiceRequest.getPhoto() != null && choiceRequest.getReview() != null) {

			ChoicePrime choicePrime = new ChoicePrime();
			choicePrime.setPhoto(choiceRequest.getPhoto());
			choicePrime.setReview(choiceRequest.getReview());
			choicePrime.setChoice(choice);

			choice.setPrime(choicePrime);
		}
		try {
			choiceRepository.save(choice);
			poll.addChoice(choice);
			SocketChoiceResponse socketChoiceResponse = new SocketChoiceResponse();
			socketChoiceResponse.setType(3);
			socketChoiceResponse.setPollId(poll.getId());
			socketChoiceResponse.setPostId(poll.getPost().getId());
			ChoiceResponse choiceResponse = mapChoiceToChoiceResponse(choice, null, null, userRepository,
					voteRepository);
			socketChoiceResponse.addChoiceResponse(choiceResponse);
			notificationService.dispatch(currentUser.getUsername(), socketChoiceResponse, poll.getPost().getId());
		} catch (DataIntegrityViolationException ex) {
			logger.info("User {} has add choice error in Poll {}", currentUser.getId(), pollId);
			throw new BadRequestException("Sorry! Something went wrong!");
		}

		// Retrieve poll creator details
		PostNumberCount commentCount = commentRepository.countByPostId(poll.getPost().getId());
		PostNumberCount likeCount = likeRepository.countByPostId(poll.getPost().getId());
		Long commentCountValue = Long.valueOf(0);
		Long likeCountValue = Long.valueOf(0);
		if (commentCount != null)
			commentCountValue = commentCount.getCount();
		if (likeCount != null)
			likeCountValue = likeCount.getCount();
		return mapPostToPostResponse(currentUser.getId(), poll.getPost(), commentCountValue, likeCountValue,
				userRepository, voteRepository, likeRepository);
	}

	public PostResponse deleteChoice(Long pollId, VoteRequest voteRequest, UserPrincipal currentUser) {
		Poll poll = pollRepository.findById(pollId)
				.orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

		if (poll.getExpirationDateTime() != null && poll.getExpirationDateTime().isBefore(Instant.now())) {
			throw new BadRequestException("Sorry! This Poll has already expired");
		}
//		User creator = userRepository.findById(poll.getPost().getCreatedBy())
//				.orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getPost().getCreatedBy()));
//		User user = userRepository.getOne(currentUser.getId());
		if (poll.getPost().getCreatedBy() != currentUser.getId())
			throw new BadRequestException("Sorry! You can't delete this choice!");
		Choice selectedChoice = poll.getChoices().stream()
				.filter(choice -> choice.getId().equals(voteRequest.getChoiceId())).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Choice", "id", voteRequest.getChoiceId()));
		List<Vote> vote = voteRepository.findByChoiceId(selectedChoice.getId());
		for (Vote vote2 : vote) {
			try {
				voteRepository.delete(vote2);

			} catch (DataIntegrityViolationException ex) {
				logger.info("User {} has delete choice voted error in Poll {}", currentUser.getId(), pollId);
				throw new BadRequestException("Sorry! Something went wrong!");
			}
		}
		try {
			poll.removeChoice(selectedChoice);
			choiceRepository.delete(selectedChoice);
			SocketDeleteChoice deleteChoice = new SocketDeleteChoice();
			deleteChoice.setType(4);
			deleteChoice.setChoiceId(voteRequest.getChoiceId());
			deleteChoice.setPollId(pollId);
			Long postId = poll.getPost().getId();
			deleteChoice.setPostId(postId);
			notificationService.dispatch(currentUser.getUsername(), deleteChoice, postId);
		} catch (DataIntegrityViolationException ex) {
			logger.info("User {} has delete choice error in Poll {}", currentUser.getId(), pollId);
			throw new BadRequestException("Sorry! Something went wrong!");
		}

		// -- Vote Saved, Return the updated Poll Response now --

		// Retrieve Vote Counts of every choice belonging to the current poll
		PostNumberCount commentCount = commentRepository.countByPostId(poll.getPost().getId());
		PostNumberCount likeCount = likeRepository.countByPostId(poll.getPost().getId());
		Long commentCountValue = Long.valueOf(0);
		Long likeCountValue = Long.valueOf(0);
		if (commentCount != null)
			commentCountValue = commentCount.getCount();
		if (likeCount != null)
			likeCountValue = likeCount.getCount();
		return mapPostToPostResponse(currentUser.getId(), poll.getPost(), commentCountValue, likeCountValue,
				userRepository, voteRepository, likeRepository);
//		List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);
//
//		Map<Long, Long> choiceVotesMap = votes.stream()
//				.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));
//
//		// Retrieve poll creator details
//		List<Vote> voteSeleted = voteRepository.findByUserIdAndPollId(currentUser.getId(),pollId);
//		return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator, voteSeleted);
	}

	@Transactional
	public boolean deletePost(Long postId, UserPrincipal currentUser) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
		if (post.getCreatedBy() == currentUser.getId()) {
			voteRepository.deleteVoteByPostId(postId);
			storagePostRepository.deleteStorageByPostId(postId);
			postRepository.deleteById(postId);
			return true;
		} else
			return false;
	}

	@Transactional
	public boolean storagePost(Long postId, UserPrincipal currentUser) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

		if (post.getShowCase() == 1 && post.getCreatedBy() != currentUser.getId()) {
			throw new BadRequestException("Sorry! You can't storage for this post");
		} else if (post.getShowCase() == 4) {
			boolean check = true;
			Iterator<Relationship> i = post.getRelationships().iterator();
			while (check && i.hasNext()) {
				Relationship relationship = i.next();
				if (relationship.getUser().getId() == currentUser.getId()) {
					check = false;
				}
			}
			if (check) {
				throw new BadRequestException("Sorry! You can't storage for this post");
			}
		} else if (post.getShowCase() == 5) {
			boolean check = true;
			Iterator<GroupPosting> i = post.getGroupPostings().iterator();
			while (check && i.hasNext()) {
				GroupPosting groupPosting = i.next();

				Iterator<Participant> i2 = groupPosting.getGroup().getParticipants().iterator();
				boolean check2 = true;
				while (check2 && i2.hasNext()) {
					Participant participant = i2.next();
					if (participant.getUser().getId() == currentUser.getId()) {
						check = false;
						check2 = false;
					}
				}
			}
			if (check) {
				throw new BadRequestException("Sorry! You can't storage for this post");
			}
		}

		StoragePost storagePost = new StoragePost();
		storagePost.setPost(post);
		User user = new User();
		user.setId(currentUser.getId());
		storagePost.setUser(user);
		try {
			storagePostRepository.save(storagePost);
			return true;
		} catch (DataIntegrityViolationException ex) {

			logger.info("User {} has error storage in Post {}", currentUser.getId(), postId);
			throw new BadRequestException("Sorry! Something went wrong!");

		}
	}

	@Transactional
	public boolean deleteStoragePost(Long postId, UserPrincipal currentUser) {

		StoragePost storagePost = storagePostRepository.findByUserIdAndPostId(postId, currentUser.getId());
		if (storagePost == null)
			throw new BadRequestException("You haven't saved this post yet!");
		try {
			storagePostRepository.delete(storagePost);
			return true;
		} catch (DataIntegrityViolationException ex) {

			logger.info("User {} has error delete storage in Post {}", currentUser.getId(), postId);
			throw new BadRequestException("Sorry! Something went wrong!");

		}
	}

	@Transactional
	public boolean deleteComment(Long postId, Long commentId, UserPrincipal currentUser) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
		boolean check = true;
		Iterator<Comment> i = post.getComments().iterator();
		while (check && i.hasNext()) {
			Comment cmt = i.next();
			if (cmt.getId().equals(commentId)) {
				if (cmt.getUser().getId() == currentUser.getId() || post.getCreatedBy() == currentUser.getId()) {

					try {
						post.removeComment(cmt);
						commentRepository.delete(cmt);
						// notificationService.dispatch(currentUser.getUsername(),getSocketChoiceResponse(poll),
						// poll.getPost().getId());
					} catch (DataIntegrityViolationException ex) {

						logger.info("User {} has delete comment error in Post {}", currentUser.getId(), postId);
						throw new BadRequestException("Sorry! Something went wrong!");

					}

				} else
					throw new BadRequestException("Sorry! You can't delete this!");
				check = false;
			}
		}
		return !check;
	}

	@Transactional
	public boolean deleteReply(Long postId, Long commentId, Long replyId, UserPrincipal currentUser) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
		boolean check = true;
		boolean check2 = true;
		Iterator<Comment> i = post.getComments().iterator();
		while (check && check2 && i.hasNext()) {
			Comment cmt = i.next();
			if (cmt.getId().equals(commentId)) {
				Iterator<Reply> i2 = cmt.getReplys().iterator();
				while (check2 && i2.hasNext()) {
					Reply reply = i2.next();
					if (reply.getId().equals(replyId)) {
						if (reply.getUser().getId() == currentUser.getId()
								|| post.getCreatedBy() == currentUser.getId())
							try {
								cmt.removeReply(reply);
								replyRepository.delete(reply);
								// notificationService.dispatch(currentUser.getUsername(),getSocketChoiceResponse(poll),
								// poll.getPost().getId());
							} catch (DataIntegrityViolationException ex) {
								logger.info("User {} has delete reply error in Post {}", currentUser.getId(), postId);
								throw new BadRequestException("Sorry! Something went wrong!");
							}
						else
							throw new BadRequestException("Sorry! You can't delete this!");
						check2 = false;
					}
				}
				check = false;
			}
		}
		return !check2;
	}

	@Transactional
	public PostResponse deleteVoted(Long pollId, VoteRequest voteRequest, UserPrincipal currentUser) {
		Poll poll = pollRepository.findById(pollId)
				.orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

		if (poll.getExpirationDateTime() != null && poll.getExpirationDateTime().isBefore(Instant.now())) {
			throw new BadRequestException("Sorry! This Poll has already expired");
		}

//		User user = userRepository.getOne(currentUser.getId());

		Choice selectedChoice = poll.getChoices().stream()
				.filter(choice -> choice.getId().equals(voteRequest.getChoiceId())).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Choice", "id", voteRequest.getChoiceId()));
		List<Vote> vote = voteRepository.findByUserIdAndChoiceId(currentUser.getId(), selectedChoice.getId());
		if (vote.isEmpty()) {
			logger.info("User {} has delete vote error in Poll {}", currentUser.getId(), pollId);
			throw new BadRequestException("Sorry! Something went wrong!");
		} else {
			if (poll.isCanFix()) {
//				vote.get(0).setChoice(selectedChoice);
				try {
					voteRepository.delete(vote.get(0));
					notificationService.dispatch(currentUser.getUsername(), getSocketChoiceResponse(poll),
							poll.getPost().getId());
//					voteRepository.save(vote.get(0));
				} catch (DataIntegrityViolationException ex) {

					logger.info("User {} has delete vote error in Poll {}", currentUser.getId(), pollId);
					throw new BadRequestException("Sorry! Something went wrong!");

				}
			} else {
				logger.info("User {} has change vote error in Poll {}", currentUser.getId(), pollId);
				throw new BadRequestException("Sorry! This poll can't change your voted!");
			}
		}

		// -- Vote Saved, Return the updated Poll Response now --

		// Retrieve Vote Counts of every choice belonging to the current poll
		PostNumberCount commentCount = commentRepository.countByPostId(poll.getPost().getId());
		PostNumberCount likeCount = likeRepository.countByPostId(poll.getPost().getId());
		Long commentCountValue = Long.valueOf(0);
		Long likeCountValue = Long.valueOf(0);
		if (commentCount != null)
			commentCountValue = commentCount.getCount();
		if (likeCount != null)
			likeCountValue = likeCount.getCount();
		return mapPostToPostResponse(currentUser.getId(), poll.getPost(), commentCountValue, likeCountValue,
				userRepository, voteRepository, likeRepository);
	}

	public void sendNoti(Poll poll, UserPrincipal currentUser, String name) {
		List<User> users = storagePostRepository.findAllUserByPostId(poll.getPost().getId());
		SocketNotification socket = new SocketNotification();
		socket.setType(10);
		socket.setPostId(poll.getPost().getId());
		socket.setName(name);
		for (User user2 : users) {
			
			boolean check = notificationService.dispatch2(user2.getUsername(), socket, currentUser.getUsername());
			if(currentUser.getId()!=user2.getId()) {
				Notification noti = new Notification();
				noti.setType(1);
				noti.setSourceId(poll.getPost().getId());
				noti.setUser(user2);
				noti.setContent(socket.getName());
				if (check)
					noti.setRead(true);
				else
					noti.setRead(false);
				notificationRepository.save(noti);
			}
		}
	}
	public void sendNotiForGroup(GroupPosting group, UserPrincipal currentUser, String name) {
		List<Participant> part = group.getGroup().getParticipants();
		List<User> users = part.stream().map(Participant::getUser).collect(Collectors.toList());
		SocketNotification socket = new SocketNotification();
		socket.setType(10);
		socket.setPostId(group.getPost().getId());
		socket.setName(name);
		for (User user2 : users) {
			boolean check = notificationService.dispatch2(user2.getUsername(), socket, currentUser.getUsername());
			if(currentUser.getId()!=user2.getId()) {
			Notification noti = new Notification();
			noti.setType(1);
			noti.setSourceId(group.getPost().getId());
			noti.setUser(user2);
			noti.setContent(socket.getName());
			if (check)
				noti.setRead(true);
			else
				noti.setRead(false);
			notificationRepository.save(noti);}
		}
	}
	public void sendNotiForRelationship(GroupPosting group, UserPrincipal currentUser, String name) {
		List<Participant> part = group.getGroup().getParticipants();
		List<User> users = part.stream().map(Participant::getUser).collect(Collectors.toList());
		SocketNotification socket = new SocketNotification();
		socket.setType(10);
		socket.setPostId(group.getPost().getId());
		socket.setName(name);
		for (User user2 : users) {
			boolean check = notificationService.dispatch2(user2.getUsername(), socket, currentUser.getUsername());
			if(currentUser.getId()!=user2.getId()) {
			Notification noti = new Notification();
			noti.setType(1);
			noti.setSourceId(group.getPost().getId());
			noti.setUser(user2);
			noti.setContent(socket.getName());
			if (check)
				noti.setRead(true);
			else
				noti.setRead(false);
			notificationRepository.save(noti);}
		}
	}
	@Transactional
	public PostResponse endPollAndGetUpdate(Long pollId, UserPrincipal currentUser) {
		Poll poll = pollRepository.findById(pollId)
				.orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

		if (poll.getExpirationDateTime() != null && poll.getExpirationDateTime().isBefore(Instant.now())) {
			throw new BadRequestException("Sorry! This Poll has already expired");
		}
//		User creator = userRepository.findById(poll.getPost().getCreatedBy())
//				.orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getPost().getCreatedBy()));
//		User user = userRepository.getOne(currentUser.getId());
		if (poll.getPost().getCreatedBy() != currentUser.getId())
			throw new BadRequestException("Sorry! You can't end this poll!");
		try {
			poll.setExpirationDateTime(Instant.now());
			pollRepository.save(poll);
			SocketEndPoll socketEndPoll = new SocketEndPoll();
			socketEndPoll.setType(2);
			socketEndPoll.setExpirationDateTime(poll.getExpirationDateTime());
			socketEndPoll.setIsExpired(true);
			socketEndPoll.setPollId(poll.getId());
			socketEndPoll.setPostId(poll.getPost().getId());
			notificationService.dispatch(currentUser.getUsername(), socketEndPoll, poll.getPost().getId());
			sendNoti(poll, currentUser, "Post " + poll.getPost().getId() + " has a Poll end!");
		} catch (DataIntegrityViolationException ex) {
			logger.info("User {} has delete choice voted error in Poll {}", currentUser.getId(), pollId);
			throw new BadRequestException("Sorry! Something went wrong!");
		}
		PostNumberCount commentCount = commentRepository.countByPostId(poll.getPost().getId());
		PostNumberCount likeCount = likeRepository.countByPostId(poll.getPost().getId());
		Long commentCountValue = Long.valueOf(0);
		Long likeCountValue = Long.valueOf(0);
		if (commentCount != null)
			commentCountValue = commentCount.getCount();
		if (likeCount != null)
			likeCountValue = likeCount.getCount();
		return mapPostToPostResponse(currentUser.getId(), poll.getPost(), commentCountValue, likeCountValue,
				userRepository, voteRepository, likeRepository);
	}

	public SocketChoiceResponse getSocketChoiceResponse(Poll poll) {
		SocketChoiceResponse socketChoiceResponse = new SocketChoiceResponse();
		socketChoiceResponse.setType(1);
		socketChoiceResponse.setPollId(poll.getId());
		socketChoiceResponse.setPostId(poll.getPost().getId());
		Long totalVotes = Long.valueOf(0);
		for (Choice choice : poll.getChoices()) {
			Long voteCount = voteRepository.countByPollIdByChoiceId(choice.getId());
			ChoiceResponse choiceResponse = mapChoiceToChoiceResponseForSocket(choice, voteCount);
			socketChoiceResponse.addChoiceResponse(choiceResponse);
			if (voteCount != null)
				totalVotes = totalVotes + voteCount;
		}
		socketChoiceResponse.setTotalVotes(totalVotes);
		return socketChoiceResponse;
	}

	@Transactional
	public PostResponse castVoteAndGetUpdatedPost(Long pollId, VoteRequest voteRequest, UserPrincipal currentUser) {
		Poll poll = pollRepository.findById(pollId)
				.orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

		if (poll.getExpirationDateTime() != null && poll.getExpirationDateTime().isBefore(Instant.now())) {
			throw new BadRequestException("Sorry! This Poll has already expired");
		}
		if (poll.getPost().getPublicDate() != null) {
			boolean check = poll.getPost().getPublicDate().isAfter(Instant.now());
			if (check) {
				throw new BadRequestException("Sorry! This Post hasn't started yet");
			} else {
				System.out.println(check);
			}
		}

		User user = userRepository.getOne(currentUser.getId());
		if (poll.getPost().getShowCase() == 1 && poll.getPost().getCreatedBy() != currentUser.getId()) {
			throw new BadRequestException("Sorry! You can't vote for this poll");
		} else if (poll.getPost().getShowCase() == 4) {
			boolean check = true;
			Iterator<Relationship> i = poll.getPost().getRelationships().iterator();
			while (check && i.hasNext()) {
				Relationship relationship = i.next();
				if (relationship.getUser().getId() == currentUser.getId()) {
					check = false;
				}
			}
			if (check) {
				throw new BadRequestException("Sorry! You can't vote for this poll");
			}
		} else if (poll.getPost().getShowCase() == 5) {
			boolean check = true;
			Iterator<GroupPosting> i = poll.getPost().getGroupPostings().iterator();
			while (check && i.hasNext()) {
				GroupPosting groupPosting = i.next();

				Iterator<Participant> i2 = groupPosting.getGroup().getParticipants().iterator();
				boolean check2 = true;
				while (check2 && i2.hasNext()) {
					Participant participant = i2.next();
					if (participant.getUser().getId() == currentUser.getId()) {
						check = false;
						check2 = false;
					}
				}
			}
			if (check) {
				throw new BadRequestException("Sorry! You can't vote for this poll");
			}
		}
		Choice selectedChoice = poll.getChoices().stream()
				.filter(choice -> choice.getId().equals(voteRequest.getChoiceId())).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Choice", "id", voteRequest.getChoiceId()));
		List<Vote> votes = voteRepository.findByUserIdAndPollId(currentUser.getId(), pollId);
		if (votes.isEmpty()) {
			Vote vote2 = new Vote();
			vote2.setUser(user);
			vote2.setChoice(selectedChoice);
			try {
				vote2 = voteRepository.save(vote2);
				notificationService.dispatch(currentUser.getUsername(), getSocketChoiceResponse(poll),
						poll.getPost().getId());
				String name;
				if (poll.isUnseenUserForVote())
					name = "Post " + poll.getPost().getId() + " has new vote!";
				else
					name = "Post " + poll.getPost().getId() + " has new vote from " + currentUser.getName();
				sendNoti(poll, currentUser, name);

			} catch (DataIntegrityViolationException ex) {

				logger.info("User {} has new vote error in Poll {}", currentUser.getId(), pollId);
				throw new BadRequestException("Sorry! Something went wrong!");

			}
		} else if (poll.getTimeLoad() <= 0) {
			if (poll.isCanFix() && poll.getMaxVotePerTimeLoad() == 1 && poll.getMaxVotePerChoice() == 1) {
				votes.get(0).setChoice(selectedChoice);
				try {
					voteRepository.save(votes.get(0));
					notificationService.dispatch(currentUser.getUsername(), getSocketChoiceResponse(poll),
							poll.getPost().getId());
				} catch (DataIntegrityViolationException ex) {

					logger.info("User {} has change vote error in Poll {}", currentUser.getId(), pollId);
					throw new BadRequestException("Sorry! Something went wrong!");

				}
			} else if (!poll.isCanFix() && poll.getMaxVotePerTimeLoad() == 1 && poll.getMaxVotePerChoice() == 1) {
				logger.info("User {} has change vote error in Poll {}", currentUser.getId(), pollId);
				throw new BadRequestException("Sorry! This poll can't change your voted!");
			} else if (poll.getMaxVotePerTimeLoad() > votes.size()) {
				List<Vote> choiceVote = voteRepository.findByUserIdAndChoiceId(currentUser.getId(),
						selectedChoice.getId());
				if (choiceVote == null || choiceVote.size() < poll.getMaxVotePerChoice()) {
					Vote vote2 = new Vote();
					vote2.setUser(user);
					vote2.setChoice(selectedChoice);
					try {
						vote2 = voteRepository.save(vote2);
						notificationService.dispatch(currentUser.getUsername(), getSocketChoiceResponse(poll),
								poll.getPost().getId());
					} catch (DataIntegrityViolationException ex) {

						logger.info("User {} has already voted in Poll {}", currentUser.getId(), pollId);
						throw new BadRequestException("Sorry! You have already cast your vote in this poll");

					}
				}

			}

		} else {

		}

//		try {
//			vote = voteRepository.save(vote);
//		} catch (DataIntegrityViolationException ex) {
//
//			logger.info("User {} has already voted in Poll {}", currentUser.getId(), pollId);
//			throw new BadRequestException("Sorry! You have already cast your vote in this poll");
//
//		}
		// -- Vote Saved, Return the updated Poll Response now --

		// Retrieve Vote Counts of every choice belonging to the current poll
//		List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);
//
//		Map<Long, Long> choiceVotesMap = votes.stream()
//				.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));
//
//		// Retrieve poll creator details
//		User creator = userRepository.findById(poll.getCreatedBy())
//				.orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy()));
//		vote = voteRepository.findByUserIdAndPollId(currentUser.getId(), pollId);
		PostNumberCount commentCount = commentRepository.countByPostId(poll.getPost().getId());
		PostNumberCount likeCount = likeRepository.countByPostId(poll.getPost().getId());
		Long commentCountValue = Long.valueOf(0);
		Long likeCountValue = Long.valueOf(0);
		if (commentCount != null)
			commentCountValue = commentCount.getCount();
		if (likeCount != null)
			likeCountValue = likeCount.getCount();
		return mapPostToPostResponse(currentUser.getId(), poll.getPost(), commentCountValue, likeCountValue,
				userRepository, voteRepository, likeRepository);
	}

	@Transactional
	public PostResponse castLikeAndGetUpdatedPost(Long postId, UserPrincipal currentUser) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
		User user = new User();
		user.setId(currentUser.getId());
		List<LikedPost> l = likeRepository.findByPostIdAndUserId(post.getId(), currentUser.getId(),
				PageRequest.of(0, 1));
		if (l == null || l.isEmpty()) {
			LikedPost like = new LikedPost();
			like.setIsLike(true);
			like.setPost(post);
			like.setUser(user);
			try {
				likeRepository.save(like);
				SocketLikeComment socket = new SocketLikeComment();
				socket.setType(5);
				socket.setPostId(postId);
				notificationService.dispatch(currentUser.getUsername(), socket, postId);
			} catch (DataIntegrityViolationException ex) {

				logger.info("User {} has like error in Post {}", currentUser.getId(), postId);
				throw new BadRequestException("Sorry! Something went wrong!");

			}
		} else {
			LikedPost like = l.get(0);
			if (like.getIsLike()) {
				like.setIsLike(false);

			} else {
				like.setIsLike(true);
			}
			try {
				likeRepository.save(like);
				SocketLikeComment socket = new SocketLikeComment();
				if (like.getIsLike())
					socket.setType(5);
				else
					socket.setType(6);
				socket.setPostId(postId);
				notificationService.dispatch(currentUser.getUsername(), socket, postId);
				// notificationService.dispatch(getSocketChoiceResponse(poll),
				// poll.getPost().getId());
			} catch (DataIntegrityViolationException ex) {

				logger.info("User {} has like error in Post {}", currentUser.getId(), postId);
				throw new BadRequestException("Sorry! Something went wrong!");

			}
		}
//		LikedPost like = new LikedPost();
//		like.setIsLike(true);
//		like.setPost(post);
//		like.setUser(user);
//		try {
//			likeRepository.save(like);
//			//notificationService.dispatch(getSocketChoiceResponse(poll), poll.getPost().getId());
//		} catch (DataIntegrityViolationException ex) {
//
//			logger.info("User {} has like error in Post {}", currentUser.getId(), postId);
//			throw new BadRequestException("Sorry! Something went wrong!");
//
//		}

		PostNumberCount commentCount = commentRepository.countByPostId(postId);
		PostNumberCount likeCount = likeRepository.countByPostId(postId);
		Long commentCountValue = Long.valueOf(0);
		Long likeCountValue = Long.valueOf(0);
		if (commentCount != null)
			commentCountValue = commentCount.getCount();
		if (likeCount != null)
			likeCountValue = likeCount.getCount();
		return mapPostToPostResponse(currentUser.getId(), post, commentCountValue, likeCountValue, userRepository,
				voteRepository, likeRepository);
	}

	public PagedResponse<CommentResponse> getCommentList(Long postId, UserPrincipal currentUser, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Comment> comments = commentRepository.getCommentByPostId(postId, pageable);

		List<CommentResponse> commentResponse = comments.map(comment -> {
			return mapCommentToCommentResponse(comment);
		}).getContent();

		return new PagedResponse<>(commentResponse, comments.getNumber(), comments.getSize(),
				comments.getTotalElements(), comments.getTotalPages(), comments.isLast());
	}

	public CommentResponse commentPost(Long postId, String text, UserPrincipal currentUser) {
		Comment comment = new Comment();
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
		comment.setPost(post);
		User user = userRepository.findById(currentUser.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
		comment.setUser(user);
		comment.setText(text);
		try {
			commentRepository.save(comment);
			CommentResponse commentResponse = mapCommentToCommentResponse(comment);
			SocketLikeComment socket = new SocketLikeComment();
			socket.setType(7);
			socket.setPostId(postId);
			socket.setComment(commentResponse);
			notificationService.dispatch(currentUser.getUsername(), socket, postId);
			return commentResponse;
		} catch (DataIntegrityViolationException ex) {

			logger.info("User {} has comment error in Post {}", currentUser.getId(), postId);
			throw new BadRequestException("Sorry! Something went wrong!");

		}

	}

	public ReplyResponse replyComment(Long postId, Long commentId, String text, UserPrincipal currentUser) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

		User user = userRepository.findById(currentUser.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

		Reply reply = new Reply();
		reply.setComment(comment);
		reply.setText(text);
		reply.setUser(user);
		comment.addReply(reply);
		try {
			replyRepository.save(reply);

			ReplyResponse replyItem = new ReplyResponse();
			replyItem.setId(reply.getId());
			replyItem.setCreateDate(reply.getCreatedAt());
			replyItem.setText(reply.getText());
			replyItem.setUpdateDate(reply.getUpdatedAt());
			replyItem.setUser(new UserSummary(reply.getUser().getId(), reply.getUser().getUsername(),
					reply.getUser().getName(), reply.getUser().getPhoto()));
			replyItem.setCommentId(commentId);

			SocketLikeComment socket = new SocketLikeComment();
			socket.setType(8);
			socket.setPostId(postId);
			socket.setReply(replyItem);

			notificationService.dispatch(currentUser.getUsername(), socket, postId);
			return replyItem;
			// notificationService.dispatch(getSocketChoiceResponse(poll),
			// poll.getPost().getId());
		} catch (DataIntegrityViolationException ex) {

			logger.info("User {} has reply error in Post {}", currentUser.getId(), postId);
			throw new BadRequestException("Sorry! Something went wrong!");

		}

	}

	public List<UserProfile> getAllUser() {
		List<User> users = userRepository.findAll();
		List<UserProfile> userProfile = new ArrayList<UserProfile>();
		List<Long> userIds = users.stream().map((user) -> {
			return user.getId();
		}).collect(Collectors.toList());
		List<UserIdCount> postCount = postRepository.countByCreatedByIn(userIds);
		// List<>Long postCount = postRepository.countByCreatedByIn(user.getId());
		List<UserIdCount> voteCount = voteRepository.countByUserIdIn(userIds);
		List<UserIdCount> storageCount = storagePostRepository.countByUserIdIn(userIds);
		Map<Long, Long> postCountMap = postCount.stream()
				.collect(Collectors.toMap(UserIdCount::getUserId, UserIdCount::getCount));
		Map<Long, Long> voteCountMap = voteCount.stream()
				.collect(Collectors.toMap(UserIdCount::getUserId, UserIdCount::getCount));
		Map<Long, Long> storageCountMap = storageCount.stream()
				.collect(Collectors.toMap(UserIdCount::getUserId, UserIdCount::getCount));
		users.forEach((user) -> {
			UserProfile u = new UserProfile(user.getId(), user.getUsername(), user.getName(), user.getCreatedAt(),
					postCountMap.get(user.getId()), voteCountMap.get(user.getId()), storageCountMap.get(user.getId()),
					user.getPhoto());
			if (user.getRoles().stream().anyMatch(a -> a.getId() == 2)) {
				u.setIsAdmin(true);
			} else
				u.setIsAdmin(false);
			u.setEmail(user.getEmail());
			u.setIsActive(user.isActive());
			u.setIsBlocked(user.isBlocked());
			u.setIsReported(user.isReported());
			userProfile.add(u);
		});

		return userProfile;
	}

	public List<GroupResponse> getAllGroup() {
		List<GroupResponse> groups = groupRepository.findAllResponse();
		return groups;
	}

	@Transactional
	public Boolean deleteGroup(Long groupId) {
		try {
			groupRepository.deleteById(groupId);
			// notificationService.dispatch(getSocketChoiceResponse(poll),
			// poll.getPost().getId());
		} catch (DataIntegrityViolationException ex) {

			throw new BadRequestException("Error to delete this Group");

		}

		return true;
	}

	public PagedResponse<PostResponse> getAllPollsAdmin(int page, int size) {
		validatePageNumberAndSize(page, size);
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Post> posts = postRepository.findAllForAdmin(pageable);

		if (posts.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), posts.getNumber(), posts.getSize(),
					posts.getTotalElements(), posts.getTotalPages(), posts.isLast());
		}
		// Map Polls to PollResponses containing vote counts and poll creator details
		List<Long> postIds = posts.map(Post::getId).getContent();
		List<PostNumberCount> commentCount = commentRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> commentCountMap = commentCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostNumberCount> likeCount = likeRepository.countByPostIdsInGroupByPostId(postIds);
		Map<Long, Long> likeCountMap = likeCount.stream()
				.collect(Collectors.toMap(PostNumberCount::getPostId, PostNumberCount::getCount));

		List<PostResponse> postResponses = posts.map(post -> {
			return mapPostToPostResponse(null, post, commentCountMap.get(post.getId()), likeCountMap.get(post.getId()),
					userRepository, voteRepository, likeRepository);
		}).getContent();

		return new PagedResponse<>(postResponses, posts.getNumber(), posts.getSize(), posts.getTotalElements(),
				posts.getTotalPages(), posts.isLast());
	}
	
	public PagedResponse<NotificationResponse> getAllNotification(UserPrincipal currentUser,int page, int size) {
		Sort sort = Sort.by(Sort.Order.asc("isRead"),Sort.Order.desc("createdAt"));
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<NotificationResponse> notification = notificationRepository.findAllResponseByUserId(currentUser.getId(), pageable);
		return new PagedResponse<>(notification.getContent(), notification.getNumber(), notification.getSize(), notification.getTotalElements(),
				notification.getTotalPages(), notification.isLast());
	}
	@Transactional
	public void readNotification(UserPrincipal currentUser,Long id) {
		Notification notification = notificationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
		if(!currentUser.getId().equals(notification.getUser().getId())) throw new BadRequestException("It not notification of user");
		try {
			notification.setRead(true);
			notificationRepository.save(notification);
		} catch (DataIntegrityViolationException ex) {

			throw new BadRequestException("Error to update this Notification");

		}
	}
	@Transactional
	public boolean deletePostAdmin(Long postId) {
		try {
			voteRepository.deleteVoteByPostId(postId);
			storagePostRepository.deleteStorageByPostId(postId);
			postRepository.deleteById(postId);
		} catch (DataIntegrityViolationException ex) {

			throw new BadRequestException("Error to delete this Post");

		}

		return true;
	}

	@Transactional
	public Boolean deleteUserGroupByGroupAdmin(Long groupId, Long userId, UserPrincipal currentUser) {
		if (userId == currentUser.getId())
			throw new BadRequestException("Can't delete yourself");
		Participant p = participantRepository.findByUserIdAndGroupId(userId, groupId);
		System.out.println(p.getId());
		if (p.getGroup().getGroupAdmin().getId() != currentUser.getId()) {
			throw new BadRequestException("User not a group admin");
		}

		try {
			Group gr = p.getGroup();
			gr.removeParticipant(p);
			System.out.println(p.getId());
			participantRepository.delete(p);
			System.out.println(p.getId());
			// notificationService.dispatch(getSocketChoiceResponse(poll),
			// poll.getPost().getId());
		} catch (DataIntegrityViolationException ex) {

			throw new BadRequestException("Error to remove User");

		}

		return true;
	}

	@Transactional
	public Boolean leaveGroup(Long groupId, UserPrincipal currentUser) {
		Participant p = participantRepository.findByUserIdAndGroupId(currentUser.getId(), groupId);
		if (p.getGroup().getGroupAdmin().getId() == currentUser.getId()) {
			throw new BadRequestException("Admin can't leave group");
		}
		try {
			p.getGroup().removeParticipant(p);
			participantRepository.delete(p);
			// notificationService.dispatch(getSocketChoiceResponse(poll),
			// poll.getPost().getId());
		} catch (DataIntegrityViolationException ex) {

			throw new BadRequestException("Error to leave this Group");

		}

		return true;
	}

	@Transactional
	public Boolean deleteGroupByGroupAdmin(Long groupId, UserPrincipal currentUser) {
		Group g = groupRepository.findById(groupId)
				.orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
		if (g.getGroupAdmin().getId() != currentUser.getId()) {
			throw new BadRequestException("User not is admin of this Group");
		}
		try {
			groupRepository.delete(g);
			// notificationService.dispatch(getSocketChoiceResponse(poll),
			// poll.getPost().getId());
		} catch (DataIntegrityViolationException ex) {

			throw new BadRequestException("Error to delete this Group");

		}

		return true;
	}

	private void validatePageNumberAndSize(int page, int size) {
		if (page < 0) {
			throw new BadRequestException("Page number cannot be less than zero.");
		}

		if (size > AppConstants.MAX_PAGE_SIZE) {
			throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
		}
	}

//
//	private Map<Long, Long> getPostCommentCountMap(List<Long> postIds) {
//		// Retrieve Vote Counts of every Choice belonging to the given pollIds
//		List<ChoiceVoteCount> votes = voteRepository.countByPollIdInGroupByChoiceId(pollIds);
//
//		Map<Long, Long> choiceVotesMap = votes.stream()
//				.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));
//
//		return choiceVotesMap;
//	}
	private static CommentResponse mapCommentToCommentResponse(Comment comment) {
		CommentResponse commentResponse = new CommentResponse();
		commentResponse.setId(comment.getId());
		commentResponse.setCreateDate(comment.getCreatedAt());
		commentResponse.setText(comment.getText());
		commentResponse.setUpdateDate(comment.getUpdatedAt());
		commentResponse.setUser(new UserSummary(comment.getUser().getId(), comment.getUser().getUsername(),
				comment.getUser().getName(), comment.getUser().getPhoto()));
		List<ReplyResponse> replyReponse = new ArrayList<ReplyResponse>();
		comment.getReplys().forEach((reply) -> {
			ReplyResponse replyItem = new ReplyResponse();
			replyItem.setId(reply.getId());
			replyItem.setCreateDate(reply.getCreatedAt());
			replyItem.setText(reply.getText());
			replyItem.setUpdateDate(reply.getUpdatedAt());
			replyItem.setUser(new UserSummary(reply.getUser().getId(), reply.getUser().getUsername(),
					reply.getUser().getName(), reply.getUser().getPhoto()));
			replyReponse.add(replyItem);
		});
		commentResponse.setReplys(replyReponse);
		return commentResponse;
	}

	private static ChoiceResponse mapChoiceToChoiceResponse(Choice choice, Long voteCount, Long userVoteCount,
			UserRepository userRepository, VoteRepository voteRepository) {
		ChoiceResponse choiceResponse = new ChoiceResponse();
		choiceResponse.setId(choice.getId());
		choiceResponse.setText(choice.getText());
		if (voteCount != null)
			choiceResponse.setVoteCount(voteCount);
		if (!choice.getPoll().isUnseenUserForAddChoice() && !choice.getPoll().getPost().isUnseenOwner()) {
			User user = userRepository.findById(choice.getCreatedBy())
					.orElseThrow(() -> new ResourceNotFoundException("User", "id", choice.getCreatedBy()));
			UserSummary userSummary = new UserSummary(user.getId(), user.getUsername(), user.getName(),
					user.getPhoto());
			choiceResponse.setCreatedBy(userSummary);
		} else {
			UserSummary userSummary = new UserSummary(Long.valueOf(-1), null, "Anonymous", null);
			choiceResponse.setCreatedBy(userSummary);
		}
//		if (!choice.getPoll().isUnseenUserForVote()) {
//			choiceResponse.setUsersVoted(voteRepository.findUserSummarysByChoiceId(choice.getId()));
//		}
		choiceResponse.setCreationDateTime(choice.getCreatedAt());
		if (userVoteCount != null)
			choiceResponse.setUserVoteCount(userVoteCount.intValue());
		if (choice.getPrime() != null) {
			ChoicePrimeResponse choicePrimeResponse = new ChoicePrimeResponse();
			choicePrimeResponse.setId(choice.getPrime().getId());
			choicePrimeResponse.setPhoto(choice.getPrime().getPhoto());
			choicePrimeResponse.setReview(choice.getPrime().getReview());
			choiceResponse.setChoicePrime(choicePrimeResponse);
		}

		return choiceResponse;
	}

	private static ChoiceResponse mapChoiceToChoiceResponseForSocket(Choice choice, Long voteCount) {
		ChoiceResponse choiceResponse = new ChoiceResponse();
		choiceResponse.setId(choice.getId());
		if (voteCount != null)
			choiceResponse.setVoteCount(voteCount);
		return choiceResponse;
	}

	private static PollResponse mapPollToPollResponse(Poll poll, Map<Long, Long> voteCount,
			Map<Long, Long> userVoteCount, UserRepository userRepository, VoteRepository voteRepository) {
		PollResponse pollResponse = new PollResponse();
		pollResponse.setId(poll.getId());
		pollResponse.setQuestion(poll.getQuestion());

		pollResponse.setPhoto(poll.getPhoto());
		pollResponse.setCreationDateTime(poll.getCreatedAt());
		pollResponse.setExpirationDateTime(poll.getExpirationDateTime());
		Instant now = Instant.now();
		if (poll.getExpirationDateTime() != null)
			pollResponse.setIsExpired(poll.getExpirationDateTime().isBefore(now));
		else
			pollResponse.setIsExpired(false);

		pollResponse.setIsUnseenUserForVote(poll.isUnseenUserForVote());
		pollResponse.setIsUnseenUserForAddChoice(poll.isUnseenUserForAddChoice());
		pollResponse.setIsAddChoice(poll.isAddChoice());
		pollResponse.setIsCanFix(poll.isCanFix());

		pollResponse.setShowResultCase(poll.getShowResultCase());
		pollResponse.setMaxVotePerTimeLoad(poll.getMaxVotePerTimeLoad());
		pollResponse.setMaxVotePerChoice(poll.getMaxVotePerChoice());
		pollResponse.setMaxVoteOfPoll(poll.getMaxVoteOfPoll());
		pollResponse.setTimeLoad(poll.getTimeLoad());

		if (userVoteCount != null) {
			long totalUserVotes = poll.getChoices().stream().mapToLong(choice -> {
				if (userVoteCount.get(choice.getId()) != null)
					return userVoteCount.get(choice.getId());
				else
					return 0;
			}).sum();
			pollResponse.setTotalUserVotes(totalUserVotes);

			List<ChoiceResponse> choiceResponses = poll.getChoices().stream().map(choice -> {
				if (pollResponse.getShowResultCase() == 1
						|| (pollResponse.getShowResultCase() == 2 && pollResponse.getIsExpired())
						|| (pollResponse.getShowResultCase() == 3 && totalUserVotes != 0))
					return mapChoiceToChoiceResponse(choice, voteCount.get(choice.getId()),
							userVoteCount.get(choice.getId()), userRepository, voteRepository);
				else
					return mapChoiceToChoiceResponse(choice, null, userVoteCount.get(choice.getId()), userRepository,
							voteRepository);

			}).collect(Collectors.toList());

			pollResponse.setChoices(choiceResponses);
		} else {

			List<ChoiceResponse> choiceResponses = poll.getChoices().stream().map(choice -> {
				if (pollResponse.getShowResultCase() == 1
						|| (pollResponse.getShowResultCase() == 2 && pollResponse.getIsExpired()))
					return mapChoiceToChoiceResponse(choice, voteCount.get(choice.getId()), null, userRepository,
							voteRepository);
				else
					return mapChoiceToChoiceResponse(choice, null, null, userRepository, voteRepository);

			}).collect(Collectors.toList());

			pollResponse.setChoices(choiceResponses);
		}
		long totalVotes = poll.getChoices().stream().mapToLong(choice -> {
			if (voteCount.get(choice.getId()) != null)
				return voteCount.get(choice.getId());
			else
				return 0;
		}).sum();
		// long totalVotes =
		// pollResponse.getChoices().stream().mapToLong(ChoiceResponse::getVoteCount).sum();
		pollResponse.setTotalVotes(totalVotes);
		return pollResponse;
	}

	public PostResponse mapPostToPostResponse(Long currentUser, Post post, Long totalComment, Long totalLike,
			UserRepository userRepository, VoteRepository voteRepository, LikeRepository likeRepository) {
		PostResponse postResponse = new PostResponse();
		postResponse.setId(post.getId());
		postResponse.setQuestion(post.getQuestion());
		List<Long> pollIds = post.getPolls().stream().map(poll -> {
			return poll.getId();
		}).collect(Collectors.toList());
		List<ChoiceVoteCount> voteCount = voteRepository.countByPollIdInGroupByChoiceId(pollIds);
		Map<Long, Long> voteCountMap = voteCount.stream()
				.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));
		Map<Long, Long> userVoteCountMap;
		if (currentUser != null) {
			List<ChoiceVoteCount> userVoteCount = voteRepository.countByPollIdInGroupByChoiceIdByUserId(pollIds,
					currentUser);

			userVoteCountMap = userVoteCount.stream()
					.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));
		} else {
			userVoteCountMap = null;
		}
		List<PollResponse> pollResponses = post.getPolls().stream().map(poll -> {
			return mapPollToPollResponse(poll, voteCountMap, userVoteCountMap, userRepository, voteRepository);
		}).collect(Collectors.toList());
		postResponse.setPolls(pollResponses);
		postResponse.setIsUnseenOwner(post.isUnseenOwner());
		if (!postResponse.getIsUnseenOwner()) {
			User user = userRepository.findById(post.getCreatedBy())
					.orElseThrow(() -> new ResourceNotFoundException("User", "id", post.getCreatedBy()));
			UserSummary userSummary = new UserSummary(user.getId(), user.getUsername(), user.getName(),
					user.getPhoto());
			postResponse.setCreatedBy(userSummary);
		} else {
			UserSummary userSummary2 = new UserSummary(Long.valueOf(-1), null, "Anonymous", null);
			postResponse.setCreatedBy(userSummary2);
		}
		List<LikedPost> l;
		if (currentUser != null) {
			l = likeRepository.findByPostIdAndUserIdAndIsLike(post.getId(), currentUser, true, PageRequest.of(0, 1));
		} else {
			l = likeRepository.findByPostIdAndUserIdAndIsLike(post.getId(), Long.valueOf(-1), true,
					PageRequest.of(0, 1));
		}
		if (!l.isEmpty())
			postResponse.setIsUserLike(l.get(0).getIsLike());
		else
			postResponse.setIsUserLike(false);
		postResponse.setTotalComment(totalComment);
		postResponse.setTotalLike(totalLike);
		postResponse.setShowCase(post.getShowCase());
		if (postResponse.getShowCase() == 5) {
			post.getGroupPostings().forEach(group -> {

//				Boolean check = true;
//				Iterator<Participant> i = group.getGroup().getParticipants().iterator();
//				while(check&&i.hasNext()) {
//					Participant participant = i.next();
//					if(participant.getUser().getId()==currentUser.getId()) {
//						check=false;
				GroupResponse groupResponse = new GroupResponse(group.getGroup().getId(), group.getGroup().getName(),
						group.getGroup().getGroupCode());
				groupResponse.setCreateDate(group.getGroup().getCreatedAt());
				UserSummary userC = new UserSummary(group.getGroup().getGroupAdmin().getId(),
						group.getGroup().getGroupAdmin().getUsername(), group.getGroup().getGroupAdmin().getName(),
						group.getGroup().getGroupAdmin().getPhoto());
				groupResponse.setAdmin(userC);
				postResponse.addGroupResponse(groupResponse);
//					}
//				}
			});

		}
		StoragePost storagePost = storagePostRepository.findByUserIdAndPostId(post.getId(), currentUser);
		if (storagePost != null)
			postResponse.setIsUserStorage(true);
		else
			postResponse.setIsUnseenOwner(false);
		postResponse.setPublicDate(post.getPublicDate());

		return postResponse;
	}

//
//	private Map<Long, List<Vote>> getPollUserVoteMap(UserPrincipal currentUser, List<Long> pollIds) {
//		// Retrieve Votes done by the logged in user to the given pollIds
//		Map<Long, List<Vote>> pollUserVoteMap = null;
//		if (currentUser != null) {
////			List<Vote> userVotes = voteRepository.findByUserIdAndPollIdIn(currentUser.getId(), pollIds);
//			pollUserVoteMap = pollIds.stream().collect(Collectors.toMap(vote->vote, vote->{
//				return voteRepository.findByUserIdAndPollId(currentUser.getId(), vote);
//			}));
//		}
//		return pollUserVoteMap;
//	}
//	private Map<Long, List<Vote>> getPollUserVoteMap2(UserPrincipal currentUser, List<Long> pollIds) {
//		// Retrieve Votes done by the logged in user to the given pollIds
//		Map<Long, List<Vote>> pollUserVoteMap = null;
//		if (currentUser != null) {
////			List<Vote> userVotes = voteRepository.findByUserIdAndPollIdIn(currentUser.getId(), pollIds);
//			pollUserVoteMap = pollIds.stream().collect(Collectors.toMap(vote->vote, vote->{
//				return voteRepository.findByUserIdAndPollId(currentUser.getId(), vote);
//			},(address1, address2) -> {
//                System.out.println("duplicate key found!");
//                return address1;
//            }));
//		}
//		return pollUserVoteMap;
//	}
//
	Map<Long, User> getPostCreatorMap(List<Post> posts) {
		// Get Poll Creator details of the given list of polls
		List<Long> creatorIds = posts.stream().map(Post::getCreatedBy).distinct().collect(Collectors.toList());

		List<User> creators = userRepository.findByIdIn(creatorIds);
		Map<Long, User> creatorMap = creators.stream().collect(Collectors.toMap(User::getId, Function.identity()));

		return creatorMap;
	}
}
