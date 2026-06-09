# TraceURL 🔗

**TraceURL**은 사용자가 긴 URL을 짧게 단축하고, 생성된 단축 URL을 통해 유입되는 트래픽에 대한 상세한 분석(클릭 수, 유입 국가, 브라우저, 디바이스, 리퍼러 등)을 실시간으로 제공하는 URL 단축 및 트래킹 웹 서비스입니다.

이 프로젝트는 대규모 트래픽 처리 시 발생할 수 있는 병목을 극복하기 위한 **비동기 데이터 처리**, 외부 라이브러리를 활용한 **상세 트래픽 분석**, 그리고 사용자 편의성을 높이기 위한 **QR 코드 자동 생성 및 OAuth 2.0 보안 연동** 등을 깊이 있게 고민하여 구현하였습니다.

---

## 🛠 기술 스택 (Tech Stack)

### Backend
- **Framework**: Spring Boot 4.0.0, Java 24
- **Security**: Spring Security, OAuth2 Client, JJWT (JWT Auth)
- **Database**: PostgreSQL (Spring Data JPA)

### Libraries & Tools
- **Analytics**: MaxMind GeoIP2, uap-java (User-Agent Parsing)
- **Utility**: ZXing (QR Code), dotenv-java
- **Build Tool**: Gradle

---

## 🏗 프로젝트 구조 (Project Architecture)

```text
com.traceurl.traceurl
 ├── common/             # 전역 예외 처리, 공통 유틸리티(IP 처리 등), 상수
 └── core/               # 핵심 비즈니스 로직
      ├── RootRedirectController.java # 단축 URL 리다이렉트 진입점
      ├── analytics/     # 트래픽 분석, 로그 비동기 저장 및 통계 집계
      ├── auth/          # OAuth2 소셜 로그인, JWT 발급 및 검증 로직
      ├── shorturl/      # URL 단축 처리, QR 코드 생성 및 수명 관리
      └── user/          # 사용자 정보 및 프로필 관리
```

*   **`common/`**: 애플리케이션 전반에서 재사용되는 IP 마스킹/해싱 처리, 공통 예외 응답 로직이 위치하여 도메인 간 결합도를 낮췄습니다.
*   **`core/`**: 도메인(기능) 단위로 패키지를 분리(`shorturl`, `analytics`, `auth` 등)하여 기능의 응집도를 높이고 유지보수를 용이하게 하는 구조를 취하고 있습니다.

---

## 🗄️ 데이터베이스 모델링 (ERD)

> **💡 데이터베이스 설계 주안점**
> 사용자(User), 단축 URL(ShortUrl), 클릭 이벤트 로그(ClickEvent), QR 코드(QrCode) 간의 관계형 데이터를 설계했습니다. 특히 대용량 데이터가 쌓이는 클릭 이벤트 테이블은 조회를 최적화하기 위해 비정규화(Denormalization) 및 인덱싱 설계를 고려하였습니다.


---
## 🛠 주요 사용 기술 및 구현 사례

### 1. 사용자 트래픽 비동기 분석 및 위치/환경 추적

*   **사용 라이브러리/기술**: `Spring @Async`, `MaxMind GeoIP2`, `uap-java`
*   **어떤 기술인가?**: 
    *   `@Async`: 메인 스레드와 별개의 스레드에서 백그라운드 작업을 처리하게 해주는 Spring 내장 기술입니다.
    *   `MaxMind GeoIP2`: 사용자의 IP 주소를 기반으로 접속 국가, 도시 등의 위치 정보를 오프라인 데이터베이스로 매핑해 주는 라이브러리입니다.
    *   `uap-java`: 클라이언트가 전송한 User-Agent 문자열을 파싱하여 운영체제, 디바이스 종류, 브라우저 등의 정보를 손쉽게 추출해 주는 라이브러리입니다.
*   **사용 방법 (코드 예시)**:
    사용자가 단축 URL에 접속하면 리다이렉트 응답은 메인 스레드에서 즉시 처리하고, 상세 접속 로그 기록은 백그라운드 스레드에서 비동기로 분리하여 수행하도록 설계했습니다.

```java
// ClickEventService.java
@Async // 비동기 스레드에서 실행되도록 설정하여 리다이렉트 응답 지연을 방지
@Transactional
public void logClick(UUID shortUrlId, String clientIp, String uaRaw, String referrer, String visitorId, boolean isNewVisitor, boolean isValid) {
    try {
        // 1. GeoIP2를 활용한 IP 기반 위치 정보 파싱
        GeoLocationDto geo = geoLocationService.getLocation(clientIp);

        // 2. uap-java를 이용한 User-Agent 환경 파싱 (디바이스, OS, 브라우저 추출)
        Client client = uaParser.parse(uaRaw);

        // 3. 수집된 상세 분석 정보 엔티티로 변환 후 DB 저장
        ClickEvent event = ClickEvent.builder()
                .shortUrlId(shortUrlId)
                .uaDeviceType(parseDeviceType(uaRaw))
                .uaOs(client.os.family)
                .uaBrowser(client.userAgent.family)
                .geoCountry(geo.getCountry())
                // ... 생략
                .build();
        clickEventRepository.save(event);

        // 4. 실시간 집계 테이블에 통계 데이터 누적 업데이트
        LocalDate today = LocalDate.now();
        int uvAdd = isNewVisitor ? 1 : 0;
        breakdownRepository.upsertBreakdown(shortUrlId, today, "DEVICE", event.getUaDeviceType(), uvAdd);
        // ... 생략
    } catch (Exception e) {
        log.error("Failed to log click event for shortUrlId {}", shortUrlId, e);
    }
}
```

*   **얻은 결과 (도입 효과)**:
    리다이렉트를 수행할 때 위치 정보 추적이나 User-Agent 분석 로직 때문에 응답이 지연되는 것을 근본적으로 차단했습니다. 사용자는 딜레이 없이 원본 페이지로 즉각 이동하며, 서버 시스템은 안전하게 다차원(디바이스, 위치, 시간대) 통계 데이터를 지속적으로 수집 및 집계할 수 있게 되었습니다.

---

### 2. 고성능 단축 URL 리다이렉트 및 Visitor 식별

*   **사용 라이브러리/기술**: `Spring MVC (@RestController, @CookieValue)`, `Spring Data JPA`
*   **어떤 기술인가?**: 
    클라이언트의 HTTP 요청을 가장 먼저 받아 처리하는 엔드포인트 계층과 영속성 계층입니다. 특히 프레임워크가 제공하는 쿠키 스캐닝 기능(`@CookieValue`)을 활용하여 고유 방문자(UV)를 편리하게 식별합니다.
*   **사용 방법 (코드 예시)**:
    요청 경로의 단축 코드(`shortCode`)를 가로채어 기존 URL을 조회하고, 방문자 구분을 위한 고유 쿠키(`v_id`)가 없으면 새로 발급하여 리다이렉트 응답 시 브라우저에 심어줍니다. 또한 블랙리스트로 등록된 IP일 경우 즉각적으로 HTTP 403 예외 응답을 반환합니다.

```java
// RootRedirectController.java
@GetMapping("/{shortCode}")
public ResponseEntity<Void> redirect(
        @PathVariable String shortCode,
        HttpServletRequest request,
        HttpServletResponse response,
        @CookieValue(name = "v_id", required = false) String vId // 고유 방문자 식별용 쿠키 자동 추출
) {
    // 1. 단축 코드로 DB에서 원본 URL 정보 조회
    ShortUrl shortUrlEntity = shortUrlRepository.findActiveByShortCode(shortCode);
    if (shortUrlEntity == null) return ResponseEntity.notFound().build();

    // 2. 쿠키 기반 Visitor ID 처리 (새 방문자일 경우 UUID 새로 생성)
    String visitorId = (vId == null) ? UUID.randomUUID().toString() : vId;
    boolean isNewVisitor = (vId == null);
    if (isNewVisitor) { 
        Cookie cookie = new Cookie("v_id", visitorId);
        // 쿠키 보안 설정 후 response에 삽입 (코드 일부 생략)
    }

    // 3. 차단된 IP(블랙리스트)일 경우 접근 거부 처리
    String clientIp = IpUtils.getClientIp(request);
    if (ipBlocklistService.isBlocked(shortUrlEntity.getId(), clientIp)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // 4. (위 1번 항목 참조) 수집된 헤더 및 IP 정보를 넘겨 트래픽 로깅을 백그라운드로 실행
    clickEventService.logClick(shortUrlEntity.getId(), clientIp, /* ... */, visitorId, isNewVisitor, true);

    // 5. HTTP 302 FOUND 코드를 통해 클라이언트 브라우저를 원본 주소로 이동 지시
    return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(shortUrlEntity.getOriginalUrl()))
            .build();
}
```

*   **얻은 결과 (도입 효과)**:
    가장 빈번하게 호출되는 리다이렉트 API 로직의 응집도를 높여 빠르고 정확한 처리가 가능해졌습니다. Cookie 기반의 무작위 UUID 식별자를 도입함으로써 IP 주소가 변경되어도 동일 브라우저 접속을 추적(UV 식별)할 수 있어 데이터 통계의 신뢰성을 크게 향상시켰습니다.

---

### 3. OAuth 2.0 커스텀 및 JWT를 활용한 무상태 보안 시스템

*   **사용 라이브러리/기술**: `Spring Security`, `OAuth2 Client`, `jjwt`
*   **어떤 기술인가?**: 
    *   `Spring Security OAuth2 Client`: Google, GitHub 등 소셜 로그인 제공자와의 복잡한 인증 흐름(접근 토큰 발급, 사용자 정보 획득 등)을 대신 처리해 주는 스프링 공식 모듈입니다.
    *   `jjwt`: Java 진영에서 널리 쓰이는 표준 JWT(JSON Web Token) 생성 및 서명 검증 라이브러리로, 무상태(Stateless) API 인증 서버를 구축하는 데 필수적입니다.
*   **사용 방법 (코드 예시)**:
    Spring Security의 `SecurityFilterChain`을 설정하여 세션을 완전히 비활성화(Stateless)하고, JWT 기반 필터를 커스텀 추가했습니다. 소셜 로그인이 성공하면 커스텀 구현한 `OAuthSuccessHandler`에서 JWT 토큰을 발급하여 브라우저에 안전하게 내려주는 구조를 취합니다.

```java
// SecurityConfig.java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // REST API 환경을 위한 CSRF 비활성화
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 무상태 세션 정책
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/oauth2/**", "/{shortCode}").permitAll() // 인증 불필요 경로
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth -> oauth
            .userInfoEndpoint(user -> user.userService(customOAuth2UserService)) // 벤더사 응답 데이터 정규화 및 가입
            .successHandler(oAuth2SuccessHandler) // 로그인 성공 시 JWT 발급 처리
        )
        // Spring Security 기본 폼 로그인 필터 앞에 커스텀 JWT 검증 필터 삽입
        .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

*   **얻은 결과 (도입 효과)**:
    사용자가 매번 비밀번호를 입력 및 관리할 필요 없이 소셜 계정으로 즉시 서비스를 이용하게 하여 회원가입 이탈률을 낮췄습니다. 더불어 세션(Session) 대신 JWT 기반 인증 구조를 채택하여, 차후 서버를 스케일 아웃(Scale-out) 하더라도 복잡한 세션 클러스터링 동기화 이슈를 겪지 않는 안정적인 무상태 API 백엔드를 구축해냈습니다. Spring Security의 강력한 필터 체인을 응용하여 보안 위협을 효과적으로 차단하는 설계 역량을 입증합니다.

---

### 4. ZXing을 활용한 QR 코드 자동 생성 파이프라인

*   **사용 라이브러리/기술**: `ZXing (Zebra Crossing)`
*   **어떤 기술인가?**: 
    Google에서 오픈소스로 공개한 바코드 및 2D 바코드(QR 코드 포함) 이미지 프로세싱 라이브러리입니다. 별도의 외부 API 의존 없이 자바 백엔드 코드로 간단히 문자열(URL 등)을 QR코드 이미지 파일로 렌더링 할 수 있게 해줍니다.
*   **사용 방법 (코드 예시)**:
    사용자가 새로운 단축 URL 생성을 요청하여 DB 저장이 완료되면, 백엔드 서비스 단에서 리다이렉트용 서비스 호스트 주소(단축된 최종 주소)를 내용물로 갖는 QR 코드를 생성하고 시스템 디렉토리에 PNG 이미지 형태로 즉시 저장합니다.

```java
// QrCodeService.java
@Service
public class QrCodeService {

    @Value("${file.upload.path}")
    private String uploadPath; // 저장될 로컬 디렉토리 경로 (ex: /uploads/qr/)

    public void generateAndSave(ShortUrl shortUrl){
        // 1. QR코드 이미지에 담을 페이로드(최종 단축 URL 주소) 구성
        String fullUrl = "https://traceurl.p-e.kr/api/" + shortUrl.getShortCode();
        String fileName = shortUrl.getShortCode() + ".png";

        // 2. 이미지 렌더링 및 파일 쓰기 핵심 로직 호출
        saveFile(fullUrl, fileName);

        // 3. 생성된 파일의 경로와 메타데이터를 DB에 연결하여 저장
        QrCode qrCode = QrCode.builder()
                .shortUrl(shortUrl)
                .payload(fullUrl)
                .filePath(uploadPath + fileName)
                .build();
        qrCodeRepository.save(qrCode);
    }

    private void saveFile(String content, String fileName){
        try{
            // ZXing 라이브러리의 핵심 객체인 QRCodeWriter 호출
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            
            // 내용, 포맷(QR_CODE 타입), 가로, 세로 길이(250x250)를 지정해 비트 매트릭스 형태로 인코딩
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 250, 250);

            // 매트릭스 정보를 바탕으로 지정된 로컬 경로에 PNG 이미지 포맷으로 물리적 파일 쓰기 연산 수행
            Path path = FileSystems.getDefault().getPath(uploadPath + fileName);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        }catch(Exception e){
            throw new BusinessException(CommonError.INTERNAL_SERVER_ERROR);
        }
    }
}
```

*   **얻은 결과 (도입 효과)**:
    외부 QR코드 생성 무료 API(구글 차트 API 등)에 의존하지 않으므로 트래픽 병목이나 API 종속성 및 호출 제한(Rate Limit)을 피할 수 있습니다. 사용자는 단축 주소를 생성함과 동시에 제공된 QR 이미지를 다운로드하여 즉각적으로 오프라인 포스터, 명함, 홍보물 등에 활용할 수 있어, 실용성과 서비스 만족도가 매우 높아졌습니다.