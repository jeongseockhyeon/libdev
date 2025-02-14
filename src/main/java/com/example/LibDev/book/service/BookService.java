package com.example.LibDev.book.service;

import com.example.LibDev.book.dto.BookRequestDto;
import com.example.LibDev.book.dto.BookResponseDto;
import com.example.LibDev.book.entity.Book;
import com.example.LibDev.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BookService {
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

    private static final String KAKAO_API_KEY = "KakaoAK 77a416b84bd8bc56ccf085a8b028dce6"; // 실제 API 키로 변경
    private static final String LIBRARY_API_KEY = "47d8bf9a7a5de436887527621acaf71b54853c2418438855dd869ffbcaf10981";

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

        System.out.println("Kakao API Response: " + responseBody);

        if (responseBody != null) {
            Object documentsObj = responseBody.get("documents");
            System.out.println("Documents Object: " + documentsObj);

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
                        System.out.println("청구기호가 없으므로 저장하지 않습니다: " + title);
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
    private Map<String, String> fetchLibraryData(String isbn) {
        String url = "https://www.nl.go.kr/NL/search/openApi/search.do?key=" + LIBRARY_API_KEY
                + "&detailSearch=true&isbnOp=isbn&isbnCode=" + isbn;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_XML_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();

        Map<String, String> result = Map.of("callNumber", "N/A", "topicId", "0"); // 기본값 설정

        if (responseBody != null) {
            System.out.println("Library API XML Response: " + responseBody);

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
}
