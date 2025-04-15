# LibDev

![header](https://capsule-render.vercel.app/api?type=waving&color=gradient&height=300&section=header&text=LibDev&fontSize=90&fontAlignY=40&desc=Elice%20Cloud%20Track%205기&descAlign=70)

## 💬 설명

> 도서 데이터 및 추천 알고리즘을 활용한 도서관 대출 서비스 LibDev입니다.
<br>

## ⚙️ 기술 스택

### 백엔드

<img src="https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
<img src="https://img.shields.io/badge/OAUTH2-4285F4?style=for-the-badge&logo=google&logoColor=white">
<img src="https://img.shields.io/badge/JWT-003545?style=for-the-badge&logo=jsonwebtokens&logoColor=white">
<img src="https://img.shields.io/badge/REDIS-FF4438?style=for-the-badge&logo=redis&logoColor=white">
<img src="https://img.shields.io/badge/WEB SOCKET-F3702A?style=for-the-badge&logoColor=white">
<img src="https://img.shields.io/badge/nginx-%23009639?style=for-the-badge&logo=nginx&logoColor=white">


### 프론트엔드

<img src="https://img.shields.io/badge/HTML-239120?style=for-the-badge&logo=html5&logoColor=white">
<img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=JavaScript&logoColor=white">
<img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=HTML5&logoColor=white"/>
<img src="https://img.shields.io/badge/css-1572B6?style=for-the-badge&logo=css3&logoColor=white">
<img src="https://img.shields.io/badge/Bootstrap-563D7C?style=for-the-badge&logo=bootstrap&logoColor=white">
<img src="https://img.shields.io/badge/thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white"/>

<br><br>

## 🧩 ERD

![LibDevERDERD](./assets/LibDevERD.png)
<br><br>

## ⛓️ 와이어 프레임

![LibDevWAF](./assets/LibDevWAF.png)
<br><br>

## 🔥 주요기능
### **1. 회원**

- **회원 가입**
    - 이메일 형식 아이디
    - 이메일
    - 이메일, 비밀번호, 전화번호 유효성 검사
    - OAUTH
        - 구글
        - 네이버
- **마이 페이지**
    - 서비스 이력 확인
        - 현재 대출 현황
        - 대출 이력
        - 예약 현황
        - 본인이 쓴 한줄평
    - 내 정보 수정
        - 이름
        - 비밀번호
        - 전화번호
- **권한**
    - 일반 회원
    - 관리자
        - 회원 정보 조회
        - 회원 권한 변경
        - 도서 반납 승인
        - 도서 추가 가능
- **로그인**
    - 로그인 시 쿠키로 AccessToken, RefreshToken 발급
    - RefreshToken과 Redis를 활용한 AccessToken 재발급
    - 이메일 주소로 인증 번호를 받아 비밀번호 수정 기능 제공
- **보안**
    - 로그아웃 시 기존 쿠키들 무효화 및 엑세스 토큰 블랙리스트로 레디스에 관리
  

### **2. 도서**

- **도서 등록**
    - 관리자(ADMIN) 권한을 가진 경우 도서 등록 가능
    - 카카오 API에서 도서 정보를 받아오고, 받아온 고유 번호로 국립중앙도서관 API에서 나머지 도서 정보를 받아옴
    - 중복된 도서 등록 시, 청구 번호 뒤에 =2, =3 추가
- **통합 자료 검색**
    - 키워드로 도서 검색
    - 검색 유형은 전체, 제목, 저자, 출판사
    - 해당되는 도서 목록 출력
    - 목록 최신 등록 순 정렬
    - 도서 정보와 대출 예약 가능 여부 조회
- **주제 별 검색**
    - 동양서분류기호 대분류 코드로 주제 분류
    - 주제 별 도서 목록 출력
- **신착 자료**
    - 최근 일주일 안에 등록된 도서 목록 출력
- **도서 상세**
    - 도서 상세 정보 조회
    - 대출, 예약 가능
    - 해당 도서와 유사한 추천 도서 조회
- **도서 관리**
    - 관리자 권한을 가진 경우 도서 검색, 목록 조회, 도서 삭제 가능

### **3. 대출+반납**

- **대출**
    - 도서 상세 페이지에 대출이 가능할 경우 버튼 활성화
    - 연체 페널티가 있는 회원은 대출 불가
    - 최대 7권까지 대출 가능
    - 대출 기간은 2주
- **연장**
    - 대출 현황 페이지에서 연장 신청
    - 연체 페널티가 없고, 해당 도서에 예약자가 없는 경우 연장 가능
    - 1회에 한해 7일 연장 가능
- **연체**
    - 매일 일괄 처리
    - 반납 예정일이 지난 대출들의 상태를 연체 중으로 업데이트
        - 해당 회원은 연체 페널티 생김
    - 반납 완료 전까지 연체일 수 증가
- **반납**
    - 회원이 대출 현황 페이지에서 반납 신청
    - 관리자가 대출 관리 페이지에서 반납 승인
    - 반납 승인하면 반납 예정일 업데이트
    - 연체 된 대출의 경우 회원의 페널티 만료일 업데이트
- **대출 현황/연장**
    - 대출 중, 연체 중(미반납), 반납 신청 상태인 대출 조회 가능
    - 연장 및 반납 신청
- **대출 이력**
    - 반납 완료된 대출 조회(최신순 / 과거순)
- **대출 관리**
    - 전체 대출 조회
    - 대출 상태에 따른 조회 가능
    - 반납 신청 된 대출 반납 승인 처리

### **4. 예약**

- **예약 생성**
    - 대출 중인 도서에만 예약하기 버튼 활성화
    - 본인이 예약 중인 경우 예약 불가
    - 패널티 만료일이 남은 유저는 예약 불가
    - 유저당 7권 넘게 예약 불가
    - 권 당 7명 이상 예약 불가
- **예약자 수 확인**
    - 이벤트 발생 시 자동 최신화
- **예약 정보 조회**
    - 제목, 예약일, 만료일, 대기 순번, 상태 표시
- **예약 삭제**
    - 예약 삭제 시 자동 최신화
- **대출 생성**
    - 예약 현황 페이지에서 1순위 예약자이면서 반납 완료 상태인 도서에 한해서 활성화
- **예약 관리**
    - 전체 예약 조회 가능
    - 예약 삭제 가능

### **5. 알림**

- **이메일 발송**
    - 반납 완료가 되었을 때 1순위 예약자가 존재할 경우 발송
    - 기한 만료 등에 의해 1순위 예약자의 예약이 만료되었을 경우 2순위 예약자가 존재할 경우 발송
    - SMTP 사용하여 발송 후, 이메일 내부에서 대출하러 가기 버튼으로 예약 조회 페이지로 연결.
    - 대출하러 가기 버튼 누를 시 비로그인 상태라면, 로그인 페이지로 다이렉트
- **실시간 알림 발송**
    - 웹소켓을 이용하여 실시간으로 발송
    - 반납 완료가 되었을 때 1순위 예약자가 존재할 경우 발송
    - 기한 만료 등에 의해 1순위 예약자의 예약이 만료되었을 경우 2순위 예약자가 존재할 경우 발송
    - 토스트 알림창을 제거하지 않을 시 DB에 알림 보존
- **안 읽은 알림 발송**
    - 토스트 알림창을 제거하지 않은 알림에 대해 모든 페이지에서 웹소켓에 연결될 때마다 발송
    - 알림창을 제거할 경우 DB에서 해당 알림 삭제

### **6. 한줄평**

- **한줄평 작성**
    - 로그인한 유저만 작성 가능
    - 도서 별 한줄평 페이지에서만 작성 가능
- **한줄평 수정**
    - 본인이 작성한 한줄평만 수정 가능
    - 관리자(ADMIN) 권한을 가진 경우 모든 한줄평 수정 가능
- **한줄평 삭제**
    - 본인이 작성한 한줄평만 삭제 가능
    - 관리자(ADMIN) 권한을 가진 경우 모든 한줄평 삭제 가능
- **한줄평 조회**
    - 전체 한줄평, 도서 별 한줄평은 로그인 없이 조회 가능
    - 본인이 작성한 한줄평은 로그인 후 마이페이지를 통해 조회 가능
- **비속어 필터링**
    - Profanity Filter API를 활용하여 필터링 수행
        - https://github.com/Whale0928/profanity-filter-api
    - 한줄평 작성 및 수정 시 비속어 포함 여부 검사
    - 비속어가 포함된 경우 등록/수정 불가

### **7. 도서 추천**

- **유사 도서 추천**
    - 특정 도서의 상세 페이지에서 해당 도서와 유사한 도서를 추천
- **사용자 기반 추천**
    - 사용자의 대출 내역을 분석하여 적합한 도서를 추천
    - 사용자가 한 번도 대출한 적이 없는 경우, **대출 횟수가 높은 인기 도서**를 추천
- **인기 도서 추천**
- **Redis를 활용하여 추천 결과를 캐싱하여 성능 최적화**
    - 동일한 도서 또는 사용자가 반복적으로 요청할 경우 DB를 직접 조회하지 않고 캐시된 데이터를 반환

<br><br>
# 🛠️ 커밋 컨벤션

* 타입은 태그와 제목으로 구성되고, 태그는 영어로 쓰되 첫 문자는 대문자로 한다.
* "태그: 제목"의 형태이며, : 뒤에만 공백이 있음에 유의

|  태그 이름   |                설명                |
|:--------:|:--------------------------------:|
|   Feat   |          새로운 기능을 추가할 경우          |
|   Fix    |            버그를 고친 경우             |
|  Design  |       CSS 등 사용자 UI 디자인 변경        |
|  Style   | 코드 포맷 변경, 세미 콜론 누락, 코드 수정이 없는 경우 |
| Refactor |           프로덕션 코드 리팩토링           |
|   Docs   |           	문서를 수정한 경우            |
|  Rename  |     	파일 or 폴더명 수정하거나 옮기는 경우      |    
