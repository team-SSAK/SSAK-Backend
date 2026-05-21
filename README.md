# SSAK
> AI 기반의 잔반 인식과 보상을 통한 잔반 절감을 추구하는 플랫폼
>
> "싹 비우고, 싹 틔우다"

<p align="center">
    <img src="/src/main/resources/static/ssak_info_img.jpeg" alt="SSAK System Architecture">
</p>

<br>

## 📌Project Overview (프로젝트 소개)
**SSAK**은 무분별하게 버려지는 음식물 쓰레기 문제를 해결하기 위해 AI 기술과 보상형 시스템을 결합한 플랫폼입니다.

### 📈 주요 성과
- **실제 현장 도입 및 검증:** 마포구청 및 이화여자대학 식당 내 캠페인 2차례 진행
- **비용 절감 효과:** 도입 처의 **음식물 쓰레기 처리 비용 약 30% 절감** 달성

<br>

## 🛠 Tech Stack (기술 스택)
### Backend
- **Language:** Java, Python
- **Framework:** Spring Boot, FastAPI
- **Build Tool:** Gradle

### Infrastructure & DevOps
- **Cloud:** AWS (EC2, S3, RDS)
- **Container:** Docker
- **Database / Cache :** MySQL, Redis

<br>

## 🗺 System Architecture (시스템 아키텍처)
<p align="center">
    <img src="/src/main/resources/static/ssak-system-architecture.png" alt="SSAK System Architecture">
</p>
- Spring Boot 기반의 메인 백엔드 서버와 FastAPI 기반의 AI 추론 서버 분리 운영
- AWS Cloud 환경 위에 Docker 컨테이너를 활용한 안정적인 애플리케이션 배출 및 관리
- S3를 통한 이미지/정적 자원 관리 및 MySQL를 통한 데이터 정규화 및 적재
- Redis(이미지에서는 제외)를 통한 토큰 TTL 관리

## 🚀 시작 가이드 (How to Run)
환경 변수 설정 후
```bash
git clone [https://github.com/](https://github.com/)[your-repository]/ssak.git
cd ssak
./gradlew bootRun
```

## ⚙️환경 변수 파일 구성 (Environment Variables)
```bash
apple.client.id=
apple.key-id=
apple.key-path=
apple.redirect-uri=
apple.team-id=
aws.access-key=
aws.bucket-name=
aws.secret-key=
CUSTOM_SCHEME_URL=
db.password=
db.url=
db.username=
frontend-url=
google.client.id=
google.client.secret=
google.redirect-uri=
jwt.access-expiration=
jwt.refresh-expiration=
jwt.secret=
kakao.admin-key=
kakao.client.id=
kakao.client.secret=
kakao.redirect-uri=
mail.password=
mail.username=
redis.host=
redis.password=
redis.port=
slack.webhook-url=
```

## 📁 Project Structure (프로젝트 파일 구조)
```text
ssak/
├── .github/                   # GitHub Action 및 워크플로우 관리
├── src/                       # 메인 플랫폼 서버 (Spring Boot)
│   ├── main/
│   │   ├── java/
│   │   │   ├── /com/ssak/ssak/
│   │   │   │   ├── config          # Cors, S3, Security 등 환경 설정
│   │   │   │   ├── controller      # API 컨트롤러 계층
│   │   │   │   ├── domain          # 도메인 Entity 및 DTO
│   │   │   │   ├── exception       # 글로벌 예외 처리 관련 파일
│   │   │   │   ├── security        # OAuth2 및 인증/인가 관련 파일
│   │   │   │   ├── service         # 서비스 비즈니스 로직
│   │   │   │   └── util
│   │   │── resources/
│   │   │   ├── static/    # 시스템 아키텍처 이미지 등 정적 파일 위치
│   │   │   └── application.yml
├── build.gradle
├── README.md
└── Dockerfile                 # Spring Boot 애플리케이션 Docker 빌드 파일