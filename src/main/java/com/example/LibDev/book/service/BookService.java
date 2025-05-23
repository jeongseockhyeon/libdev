package com.example.LibDev.book.service;

import com.example.LibDev.book.dto.BookRequestDto;
import com.example.LibDev.book.dto.BookResponseDto;
import com.example.LibDev.book.dto.KakaoBookResponseDto;
import com.example.LibDev.book.entity.Book;
import com.example.LibDev.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    @Value("${kakao.api.key}")
    private String KAKAO_API_KEY;

    @Value("${library.api.key}")
    private String LIBRARY_API_KEY;

    private final BookRepository bookRepository;

    /*
    // 도서 직접 등록
    @Transactional
    public Book saveBook(BookRequestDto bookRequestDto) {
        Book book = bookRequestDto.toEntity();
        return bookRepository.save(book);
    }
    */

    private final RestTemplate restTemplate = new RestTemplate(); // API 호출을 위한 RestTemplate

    // Kakao API에서 도서 정보를 가져와 DB에 저장
    public void saveBookFromKakao(String query) {
        String url = "https://dapi.kakao.com/v3/search/book?query=" + query;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Authorization", KAKAO_API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 응답을 Map으로 받기
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null) {
            Object documentsObj = responseBody.get("documents");

            if (documentsObj instanceof List) {
                List<Map<String, Object>> books = (List<Map<String, Object>>) documentsObj;

                // 도서 정보 타입 변환
                for (Map<String, Object> bookData : books) {
                    String title = String.valueOf(bookData.get("title"));

                    List<String> authorsList = (List<String>) bookData.get("authors");
                    String author = (authorsList != null && !authorsList.isEmpty()) ? String.join(", ", authorsList) : "알 수 없음";

                    String publisher = String.valueOf(bookData.get("publisher"));

                    // ISBN 값이 여러 개일 경우 첫 번째 값만 저장
                    String isbnRaw = String.valueOf(bookData.get("isbn"));
                    String isbn = isbnRaw.contains(" ") ? isbnRaw.split(" ")[0] : isbnRaw;

                    String datetime = String.valueOf(bookData.get("datetime"));
                    String contents = String.valueOf(bookData.get("contents"));

                    // 표지 이미지 (thumbnail) 가져오기
                    String thumbnail = String.valueOf(bookData.get("thumbnail"));

                    // 날짜 형식 LocalDateTime으로 수정
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                    LocalDateTime publishedDate = LocalDateTime.parse(datetime, formatter);

                    // 국립중앙도서관 API에서 청구기호, 주제 ID 가져오기
                    Map<String, String> libraryData = fetchLibraryData(isbn);
                    String callNumber = libraryData.get("callNumber");
                    Integer topicId = Integer.parseInt(libraryData.get("topicId"));

                    // 청구기호가 "N/A"라면 저장하지 않음
                    if ("N/A".equals(callNumber)) {
                        continue; // 현재 루프를 건너뛰고 다음 책으로 진행
                    }

                    // 도서 정보 DB에 저장
                    Book book = Book.builder()
                            .title(title)
                            .author(author)
                            .publisher(publisher)
                            .isbn(isbn)
                            .publishedDate(publishedDate.toLocalDate())  //LocalDate로 변환
                            .contents(contents)
                            .isAvailable(true)
                            .callNumber(callNumber)
                            .thumbnail(thumbnail)
                            .topicId(topicId)
                            .build();

                    bookRepository.save(book);
                    //break;  // 첫 번째 책만 저장
                }
            } else {
                throw new IllegalStateException("Invalid response format: documents is not a List");
            }
        }
    }

    // 국립중앙도서관 API에서 청구기호와 주제 ID를 가져오는 메서드
    public Map<String, String> fetchLibraryData(String isbn) {
        String url = "https://www.nl.go.kr/NL/search/openApi/search.do?key=" + LIBRARY_API_KEY
                + "&detailSearch=true&isbnOp=isbn&isbnCode=" + isbn;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_XML_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();

        Map<String, String> result = Map.of("callNumber", "N/A", "topicId", "0"); // 기본값 설정

        if (responseBody != null) {
            // 정규식으로 청구기호(call_no) 추출
            Matcher callNumberMatcher = Pattern.compile("<call_no><!\\[CDATA\\[(.*?)]]></call_no>").matcher(responseBody);
            if (callNumberMatcher.find()) {
                result = Map.of("callNumber", callNumberMatcher.group(1), "topicId", "0");
            }

            // 정규식으로 주제 ID(kdc_code_1s) 추출
            Matcher topicIdMatcher = Pattern.compile("<kdc_code_1s><!\\[CDATA\\[(\\d+)]]></kdc_code_1s>").matcher(responseBody);
            if (topicIdMatcher.find()) {
                result = Map.of("callNumber", result.get("callNumber"), "topicId", topicIdMatcher.group(1));
            }
        }
        return result;
    }


    // 도서 등록 페이지에서 도서 검색
    public List<KakaoBookResponseDto> searchBooksToRegister(String query) {
        String url = "https://dapi.kakao.com/v3/search/book?query=" + query;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Authorization", KAKAO_API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null && responseBody.containsKey("documents")) {
            List<Map<String, Object>> books = (List<Map<String, Object>>) responseBody.get("documents");

            return books.stream().map(bookData -> new KakaoBookResponseDto(
                    String.valueOf(bookData.get("title")),
                    ((List<String>) bookData.get("authors")).stream().collect(Collectors.joining(", ")),
                    String.valueOf(bookData.get("publisher")),
                    String.valueOf(bookData.get("thumbnail")),
                    formatPublishedDate(String.valueOf(bookData.get("datetime"))),
                    extractPrimaryIsbn(String.valueOf(bookData.get("isbn"))), // ISBN 첫 번째 값만 저장
                    String.valueOf(bookData.get("contents"))
            )).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    // ISBN 공백 기준 앞부분만 추출하는 메서드
    private String extractPrimaryIsbn(String isbn) {
        return Optional.ofNullable(isbn)
                .filter(s -> !s.trim().isEmpty())
                .map(s -> s.split(" ")[0]) // 공백 기준 첫 번째 값만 사용
                .orElse("정보 없음");
    }

    // 발행일 형식 변경
    private String formatPublishedDate(String dateTime) {
        try {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTime);
            LocalDate localDate = offsetDateTime.toLocalDate();
            return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return dateTime;
        }
    }

    @Transactional
    public void registerBook(BookRequestDto bookRequestDto) {
        // BookRequestDto를 Book 엔티티로 변환
        Book book = bookRequestDto.toEntity();
        book.setAvailable(true);

        // 기존 도서가 있으면, callNumber 뒤에 숫자 증가
        List<Book> existingBooks = bookRepository.findByCallNumberStartingWith(book.getCallNumber());

        if (!existingBooks.isEmpty()) {
            String newCallNumber = generateNewCallNumber(book.getCallNumber(), existingBooks);
            book.setCallNumber(newCallNumber);
        }

        bookRepository.save(book);
    }

    // 새로운 청구번호 생성
    private String generateNewCallNumber(String baseCallNumber, List<Book> existingBooks) {
        int maxSuffix = 1;

        for (Book existingBook : existingBooks) {
            String existingCallNumber = existingBook.getCallNumber();
            if (existingCallNumber.contains("=")) {
                String[] parts = existingCallNumber.split("=");
                try {
                    int number = Integer.parseInt(parts[1]);
                    maxSuffix = Math.max(maxSuffix, number);
                } catch (NumberFormatException ignored) {}
            }
        }
        return baseCallNumber + "=" + (maxSuffix + 1);
    }

    public List<BookResponseDto> searchBooks(String query) {
        List<Book> books;
        if (query != null && !query.trim().isEmpty()) {
            books = bookRepository.findByTitleContainingOrAuthorContainingOrPublisherContainingOrderByCreatedAtDesc(query, query, query);
        } else {
            books = bookRepository.findAllByOrderByCreatedAtDesc();
        }
        return books.stream().map(BookResponseDto::fromEntity).collect(Collectors.toList());
    }

    public List<BookResponseDto> searchByTitle(String query) {
        List<Book> books = bookRepository.findByTitleContainingOrderByCreatedAtDesc(query);
        return books.stream().map(BookResponseDto::fromEntity).collect(Collectors.toList());
    }

    public List<BookResponseDto> searchByAuthor(String query) {
        List<Book> books = bookRepository.findByAuthorContainingOrderByCreatedAtDesc(query);
        return books.stream().map(BookResponseDto::fromEntity).collect(Collectors.toList());
    }

    public List<BookResponseDto> searchByPublisher(String query) {
        List<Book> books = bookRepository.findByPublisherContainingOrderByCreatedAtDesc(query);
        return books.stream().map(BookResponseDto::fromEntity).collect(Collectors.toList());
    }

    public Book findBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("도서를 찾을 수 없습니다."));
    }

    public List<BookResponseDto> findBooksByTopic(int topicId) {
        List<Book> books = bookRepository.findByTopicIdOrderByCreatedAtDesc(topicId);
        return books.stream()
                .map(BookResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 도서 삭제
    @Transactional
    public boolean deleteBook(Long bookId) {
        if (bookRepository.existsById(bookId)) {
            bookRepository.deleteById(bookId);
            return true;
        }
        return false;
    }

    // bookId 도서 정보 조회해서 반환
    @Transactional(readOnly = true)
    public BookResponseDto getBookDetails(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("해당 도서를 찾을 수 없습니다."));
        return BookResponseDto.fromEntity(book);
    }

    // 신착자료
    public List<BookResponseDto> findNewBooks() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<Book> books = bookRepository.findByCreatedAtAfterOrderByCreatedAtDesc(oneWeekAgo);
        return books.stream().map(BookResponseDto::fromEntity).collect(Collectors.toList());
    }

}
