package com.example.LibDev.borrow.controller;

import com.example.LibDev.borrow.dto.BorrowResDto;
import com.example.LibDev.borrow.dto.ExtendResDto;
import com.example.LibDev.borrow.dto.ReturnResDto;
import com.example.LibDev.borrow.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BorrowAPIController {
    private final BorrowService borrowService;

    /* 회원별 대출 현황 조회 */
    @GetMapping("/api/v1/my/borrow-status")
    public ResponseEntity<List<BorrowResDto>> getBorrowStatus() {
        return ResponseEntity.ok(borrowService.getCurrentBorrowsByUser());
    }

    /* 전체 대출 조회 */
    @GetMapping("/api/v1/borrow-list")
    public ResponseEntity<List<BorrowResDto>> getBorrowList() {
        return ResponseEntity.ok(borrowService.getAllBorrows());
    }

    /* 대출 생성 */
    @PostMapping("/api/v1/borrow")
    public ResponseEntity<?> borrow(@RequestParam Long bookId) {
        borrowService.borrow(bookId);
        return ResponseEntity.ok().build();
    }

    /* 대출 연장 */
    @PatchMapping("/api/v1/extend/{borrowId}")
    public ResponseEntity<ExtendResDto> extend(@PathVariable Long borrowId) {
        log.debug("대출 연장 - borrowId:{}", borrowId);
        return ResponseEntity.ok(borrowService.extendReturnDate(borrowId));
    }

    /* 도서 반납 신청 */
    @PatchMapping("/api/v1/return/{borrowId}")
    public ResponseEntity<ReturnResDto> requestReturn(@PathVariable Long borrowId) {
        log.debug("반납 신청 - borrowId:{}", borrowId);
        return ResponseEntity.ok(borrowService.requestReturn(borrowId));
    }

    /* 도서 반납 승인 */
    @PatchMapping("/api/v1/approve-return/{borrowId}")
    public ResponseEntity<?> approveReturn(@PathVariable Long borrowId) {
        borrowService.approveReturn(borrowId);
        return ResponseEntity.ok().build();
    }
}
