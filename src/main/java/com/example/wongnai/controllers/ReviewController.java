package com.example.wongnai.controllers;

import com.example.wongnai.entitys.Review;
import com.example.wongnai.entitys.ReviewIndex;
import com.example.wongnai.entitys.ReviewIndexId;
import com.example.wongnai.exceptions.ResourceNotFoundException;
import com.example.wongnai.json.request.ReviewRequest;
import com.example.wongnai.model.FoodKeywordQueue;
import com.example.wongnai.repositories.ReviewIndexRepository;
import com.example.wongnai.repositories.ReviewRepository;
import com.example.wongnai.services.InvertedIndex;
import com.example.wongnai.services.impl.FoodKeyWordIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

@RestController
@RequestMapping(value = "/api")
@Slf4j
public class ReviewController {
    private final ReviewRepository reviewRepository;
    private final ReviewIndexRepository reviewIndexRepository;

    public ReviewController(ReviewRepository reviewRepository, ReviewIndexRepository reviewIndexRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewIndexRepository = reviewIndexRepository;
    }

    private InvertedIndex foodKeyWordIndexService = FoodKeyWordIndexService.getInstance();

    @GetMapping
    public List<Review> get() {
        List<Review> reviews = reviewRepository.findAll();
        log.info("reviews={}", reviews);
        return reviews;
    }

    @GetMapping(params = "txt")
    public List<Review> getByReview(@RequestParam String txt) {
        log.info("txt={}", txt);
        List<Review> reviews;
        List<Long> indexes = foodKeyWordIndexService.getIndexByKey(txt);
        if (indexes.size() == 0) {
            reviews = reviewRepository.findByReviewContaining(txt);
            FoodKeywordQueue queue = FoodKeywordQueue.builder().keyword(txt).reviews(reviews).build();
            FoodKeyWordIndexService.getInstance().addQueue(queue);
            return reviews;
        }
        reviews = reviewRepository.findAllById(indexes);
        return reviews;
    }

    @PostMapping
    public Review create(@RequestBody Review req) {
        Review review = reviewRepository.save(req);
        return review;
    }

    @PutMapping("/{id}")
    public Review update(@PathVariable long id, @RequestBody ReviewRequest req) {
        Optional<Review> reviewOptional = reviewRepository.findById(id);
        if (reviewOptional.isEmpty()) {
            throw new ResourceNotFoundException(Review.class);
        }
        Review review = reviewOptional.get();
        log.info("id={}, req={}", id, req);
        review.setReview(req.getMessage());
        return reviewRepository.save(review);
    }

    @GetMapping("/test")
    public Map<String, List<Long>> getIndex() {
        return FoodKeyWordIndexService.getInstance().getHashMap();
    }

    @GetMapping("/queue")
    public Queue<FoodKeywordQueue> getQueues() {
        return FoodKeyWordIndexService.getInstance().getQueue();
    }

    @PostMapping("/post-review-index")
    public ReviewIndex postReviewIndex(@RequestBody ReviewIndexId reviewIndexId) {
        ReviewIndex reviewIndex = new ReviewIndex();
        reviewIndex.setReviewIndexId(reviewIndexId);
        return reviewIndexRepository.save(reviewIndex);
    }

    @GetMapping("/get-review-index-keyword/{keyword}")
    public List<ReviewIndex> getReviewIndexByKeyword(@PathVariable String keyword) {
        return reviewIndexRepository.findByReviewIndexIdKeyword(keyword);
    }

    @GetMapping("/get-review-index-review-id/{reviewId}")
    public List<ReviewIndex> getReviewIndexByReviewId(@PathVariable Long reviewId){
        return reviewIndexRepository.findByReviewIndexIdReviewId(reviewId);
    }
}
