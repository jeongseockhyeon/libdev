package com.example.LibDev.review.service;

import com.example.LibDev.book.entity.Book;
import com.example.LibDev.book.repository.BookRepository;
import com.example.LibDev.review.dto.ReviewDto;
import com.example.LibDev.review.dto.ReviewDto.Response;
import com.example.LibDev.review.entity.Review;
import com.example.LibDev.review.mapper.ReviewMapper;
import com.example.LibDev.review.repository.ReviewRepository;
import com.example.LibDev.user.entity.User;
import com.example.LibDev.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    /** 리뷰 저장 **/
    @Transactional
    public void saveReview(ReviewDto.SaveRequest dto){
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(()-> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(()-> new IllegalArgumentException("도서 정보를 찾을 수 없습니다."));


        Review review = Review.builder()
                .content(dto.getContent())
                .user(user)
                .book(book)
                .build();

        reviewRepository.save(review);
    }

    /** 리뷰 삭제 **/
    @Transactional
    public void deleteReview(ReviewDto.DeleteRequest dto, Long userId){
        Review review = reviewRepository.findById(dto.getId())
                .orElseThrow(()-> new IllegalArgumentException("리뷰 정보를 찾을 수 없습니다."));

        // 본인이 작성한 리뷰인지 확인
        if(!review.getUser().getId().equals(userId)){
            throw new IllegalArgumentException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
    }

    /** 전체 리뷰 조회 **/
    public List<ReviewDto.Response> getAllReviews(){
        return reviewRepository.findAll().stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    /** 도서별 리뷰 조회 **/
    public List<ReviewDto.Response> getReviewsByBook(Long bookId){
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new IllegalArgumentException("도서 정보를 찾을 수 없습니다."));

        return reviewRepository.findByBook(book).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    /** 유저별 리뷰 조회 **/
    public List<ReviewDto.Response> getReviewsByUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        return reviewRepository.findByUser(user).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }
}
