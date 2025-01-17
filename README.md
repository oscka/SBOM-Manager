# SBOM-Manager

> 이 Spring Boot 프로젝트는 소프트웨어 자재 명세서(SBOM) 관리 기능과 OAuth2 프록시 서버와의 통합을 통해 사용자 정보에 안전하게 접근하는 기능을 제공합니다.

## 주요 기능
1. SBOM 관리:
- 생성된 SBOM JSON을 데이터베이스에 저장
- SBOM 데이터 조회 및 검색
2. OAuth2 프록시 통합:
- OAuth2 프록시 서버와 연동
- 인증된 사용자의 액세스 토큰을 사용하여 프로바이더의 리소스 서버에서 사용자 정보 조회

## PostgreSQL DB 컨테이너 생성 및 초기화 방법

### 1. 도커 이미지 Pull 및 컨테이너 생성
```
docker pull postgres:16.2
docker run -d \
    -p 5432:5432 \
    --name postgres-db \
    -e POSTGRES_PASSWORD={비밀번호} \
    -e TZ=Asia/Seoul \
    -v {로컬 마운트 Path}:/var/lib/postgresql/data \
    postgres:16.2
```

### 2. PostgreSQL 접속 후 DB 생성
```
docker exec -it postgres-db /bin/sh
psql -U postgres
CREATE DATABASE test_spring;
```

### 3. 사용자 생성 및 권한 부여
```
CREATE USER test WITH PASSWORD '1234';
ALTER USER test WITH SUPERUSER;
```

### 4. 생성한 DB로 접속 후 스키마 생성
```
exit
psql -U test -d test_spring -W
CREATE SCHEMA test_schema;
```

### 5. 생성한 스키마에 테이블 생성
```
CREATE TABLE test_schema.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE test_schema.sboms (
    id SERIAL PRIMARY KEY,
    uuid UUID NOT NULL,
    bom_format VARCHAR(255),
    spec_version VARCHAR(255),
    component_type VARCHAR(255),
    name VARCHAR(255),
    component_count INTEGER,
    client_tool VARCHAR(255),
    client_tool_version VARCHAR(255),
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data JSONB
);
```

### 6. 생성한 테이블에 테스트 데이터 삽입
```
INSERT INTO test_schema.users (username, email, password, full_name, phone) 
VALUES 
('john_doe', 'john@example.com', 'password123', 'John Doe', '010-1234-5678'),
('jane_smith', 'jane@example.com', 'password456', 'Jane Smith', '010-8765-4321'),
('mike_wilson', 'mike@example.com', 'password789', 'Mike Wilson', '010-9999-8888');
```
---

## OAuth 2.0 - Client 등록(Google, Github)
### Google
#### 1. OAuth 동의 화면 구성 요약 (링크 참조)

![Google EX 02](https://github.com/user-attachments/assets/19d58fed-3e59-4bc5-aa2f-3c08588c7ca9)

#### 2. OAuth Client ID 만들기

![Google EX](https://github.com/user-attachments/assets/57ac04f4-9805-43e0-a8a6-2088c0111af3)

> 승인된 리디렉션 URL : {Proxy Server URL}/oauth2/callback
> EX) http://localhost:4180/oauth2/callback
<br>

### GitHub
#### OAuth App 구성 예시

![Git EX](https://github.com/user-attachments/assets/7bb06862-8349-4a61-b41a-58d075fc52df)

> Homepage URL : {Proxy Server URL}
> EX) http://localhost:4181
> Authorization callback URl : {Proxy Server URL}/oauth2/callback
> EX) http://localhost:4181/oauth2/callback
<br>
> 참고 : (Google)https://velog.io/@sdb016/OAuth-2.0-Client-%EB%93%B1%EB%A1%9D, (GitHub)https://developer-nyong.tistory.com/m/60


### OAuth2 Proxy Generate(Docker)
> OAuth2 Proxy는 현재 멀티 프로바이더를 지원에 한계가 있기에 각각의 OAuth2 Proxy 인스턴스 생성 후 테스트 진행
> 참고 : https://github.com/oauth2-proxy/oauth2-proxy/issues/926
#### Image Pull (Google, Github)
```
docker pull quay.io/oauth2-proxy/oauth2-proxy:v7.7.1
```
#### Container Run 
##### Google

> OAUTH2_PROXY_CLIENT_ID, OAUTH2_PROXY_CLIENT_SECRET : Client 등록 후 발급 받은 Key 입력
> <br>
> OAUTH2_PROXY_COOKIE_SECRE : 인증 후 사용될 쿠키의 Secret Key
> <br>
> OAUTH2_PROXY_PROVIDER : Oauth2 Provider 명시
> <br>
> OAUTH2_PROXY_EMAIL_DOMAINS : 허용할 이메일 도메인
> <br>
> OAUTH2_PROXY_UPSTREAMS : 프록시할 업스트림 서버 지정
> <br>
> OAUTH2_PROXY_COOKIE_SECURE : 쿠키 보안 비활성화
> <br>
> OAUTH2_PROXY_REVERSE_PROXY : 리버스 프록시 모드 활성화
> <br>
> OAUTH2_PROXY_HTTP_ADDRESS : Oauth2 Proxy가 Listening 할 주소와 포트
> <br>
> OAUTH2_PROXY_REDIRECT_URL : Oauth2 콜백 URL
> <br>
> OAUTH2_PROXY_LOGGING_LEVEL : Loggin Level 설정
> <br>
> OAUTH2_PROXY_SET_XAUTHREQUEST : X-Auth-Request-User와 X-Auth-Request-Email 헤더 설정을 추가
> <br>
> OAUTH2_PROXY_PASS_AUTHORIZATION_HEADER : Authorization 헤더를 업스트림 서버로 전달
> <br>
> OAUTH2_PROXY_PASS_USER_HEADERS : 사용자 관련 헤더를 업스트림 서버로 전달
> <br>
> OAUTH2_PROXY_PASS_ACCESS_TOKEN : 리소스 서버에 접근하기 위한 액세스 토큰을 업스트림 서버로 전달 (중요)

```
docker run -d --name oauth2-proxy \
  -p 4180:4180 \
  -e OAUTH2_PROXY_CLIENT_ID={Client ID} \
  -e OAUTH2_PROXY_CLIENT_SECRET={Client Secret} \
  -e OAUTH2_PROXY_COOKIE_SECRET=DZ_GFz3zd7lv-7lGY97lblCJE8P1YaKe2bjVqZCU6ew= \
  -e OAUTH2_PROXY_PROVIDER=google \
  -e OAUTH2_PROXY_EMAIL_DOMAINS=* \
  -e OAUTH2_PROXY_UPSTREAMS=http://host.docker.internal:8088 \
  -e OAUTH2_PROXY_COOKIE_SECURE=false \
  -e OAUTH2_PROXY_REVERSE_PROXY=true \
  -e OAUTH2_PROXY_HTTP_ADDRESS="0.0.0.0:4180" \
  -e OAUTH2_PROXY_REDIRECT_URL=http://localhost:4180/oauth2/callback \
  -e OAUTH2_PROXY_LOGGING_LEVEL=debug \
  -e OAUTH2_PROXY_SET_XAUTHREQUEST=true \
  -e OAUTH2_PROXY_PASS_AUTHORIZATION_HEADER=true \
  -e OAUTH2_PROXY_PASS_USER_HEADERS=true \
  -e OAUTH2_PROXY_PASS_ACCESS_TOKEN=true \
  quay.io/oauth2-proxy/oauth2-proxy:v7.7.1
```

##### GitHub
```
docker run -d --name oauth2-proxy-github \
  -p 4181:4181 \
  -e OAUTH2_PROXY_CLIENT_ID={Client ID} \
  -e OAUTH2_PROXY_CLIENT_SECRET={Client Secret} \
  -e OAUTH2_PROXY_COOKIE_SECRET=DZ_GFz3zd7lv-7lGY97lblCJE8P1YaKe2bjVqZCU6ew= \
  -e OAUTH2_PROXY_PROVIDER=github \
  -e OAUTH2_PROXY_EMAIL_DOMAINS=* \
  -e OAUTH2_PROXY_UPSTREAMS=http://host.docker.internal:8088 \
  -e OAUTH2_PROXY_COOKIE_SECURE=false \
  -e OAUTH2_PROXY_REVERSE_PROXY=true \
  -e OAUTH2_PROXY_HTTP_ADDRESS="0.0.0.0:4181" \
  -e OAUTH2_PROXY_REDIRECT_URL=http://localhost:4181/oauth2/callback \
  -e OAUTH2_PROXY_LOGGING_LEVEL=debug \
  -e OAUTH2_PROXY_SET_XAUTHREQUEST=true \
  -e OAUTH2_PROXY_PASS_AUTHORIZATION_HEADER=true \
  -e OAUTH2_PROXY_PASS_USER_HEADERS=true \
  -e OAUTH2_PROXY_PASS_ACCESS_TOKEN=true \
  -e OAUTH2_PROXY_COOKIE_NAME=_oauth2_proxy_git \
  quay.io/oauth2-proxy/oauth2-proxy:v7.7.1
```
---

## API Test(Swagger, Postman..)

> 몇몇 API의 경우 OAuth2 인증 후 요청에 들어오는 헤더 및 액세스 토큰을 사용(EX : Create Sbom(POST))
> <br>
> 해당 API 사용을 위해선 OAuth2 인증 후 발급받은 쿠키를 헤더에 설정해야함(Provider가 Git인 경우엔 _oauth2_proxy_git, Google인 경우엔 _oauth2_proxy 세팅, 브라우저로 인증 후 테스트 시에는 별도의 세팅 필요X)
> <br>
> EX) _oauth2_proxy={cookies value}, _oauth2_proxy_git={cookies value}
> <br>
> ![image](https://github.com/user-attachments/assets/3a8ea3da-01d8-4faa-b6a7-71941e965d90)

### Simple Template API Test

#### GET ALL User(GET)

URL : http://localhost:8088/sample-api/v1/test/user

Example Value => X

#### Create User(POST)

URL : http://localhost:8088/sample-api/v1/test/user

Example Value
```
{
  "username": "string",
  "email": "string",
  "password": "string",
  "fullName": "string",
  "phone": "string",
  "isActive": true,
}
```

EX) Body
```
{
"username": "lo123_wilson",
"email": "lo123@example.com",
"password": "password73211",
"fullName": "Lo123 Wilson",
"phone": "010-9234-8448",
"isActive": true
}
```
#### Edit User(PUT)

URL : http://localhost:8088/sample-api/v1/test/user/{id}

Example Value
```
{
  "username": "string",
  "email": "string",
  "password": "string",
  "fullName": "string",
  "phone": "string",
  "isActive": true,
}
```

EX) Body 
```
{
"username": "lol_wilson",
"email": "lol@example.com",
"password": "password78922",
"fullName": "Lol Wilson",
"phone": "010-9234-7777",
"isActive": true
}
```
#### Delete User(DELETE)

URL : http://localhost:8088/sample-api/v1/test/user/{id}

Example Value 

---
### SBOM API

#### Create Sbom(POST)

> URL EX)
> <br>
> http://localhost:4180/sample-api/v1/test/sbom  (Google)    
> http://localhost:4181/sample-api/v1/test/sbom  (GitHub)

Required Header
```
Key 1   : X-Forwarded-Email => Oauth2 Login을 통해 확인 가능
Value 1 : Oauth2 Login으로 request에 세팅된 X-Forwarded-Email
```

EX) Body
```
하기 링크에 존재하는 Syft 로 생성한 CycloneDX, SPDX Form의 json 파일

[https://osc-korea.atlassian.net/wiki/spaces/consulting/pages/1274150926/SBOM+Generator#%EA%B0%81-%EC%96%B8%EC%96%B4%EC%9D%98-%ED%8C%8C%EC%9D%BC,%EC%9D%B4%EB%AF%B8%EC%A7%80-%EA%B8%B0%EB%B0%98-SBOM](https://osc-korea.atlassian.net/wiki/spaces/consulting/pages/1279983707/SBOM#Create-Sbom(POST))
```

#### Get Sbom(GET)

URL : http://localhost:8088/sample-api/v1/test/sbom/{uuid}

> URL EX) http://localhost:8088/sample-api/v1/test/sbom/d8e6abf6-48a5-4f26-a9c0-375d30f63705
<br>
Example Value => X

---
## OAuth2 Proxy Test
> OAuth2 Proxy API는 브라우저로 테스트 시 별도의 Header 세팅 불필요

### Test Upstream Server APIs Through a Proxy Server

> Default EndPoints (version : 7.7.x)
> <br>
> https://oauth2-proxy.github.io/oauth2-proxy/features/endpoints/
> <br>
> 해당 요청들은 Oauth2-Proxy 서버에서 기본적으로 사용 가능한 API이다.

#### GET Users Email(GET)
##### Google, Github 
URL : http://localhost:{Proxy Server URL}/sample-api/v1/test/email

Required Header
```
Key 1   : X-Forwarded-Email => Oauth2 Login을 통해 확인 가능
Value 1 : Oauth2 Login으로 request에 세팅된 X-Forwarded-Email
```

> EX)
> <br>
> http://localhost:4180/sample-api/v1/test/email  (Google)    
> http://localhost:4181/sample-api/v1/test/email  (GitHub)
> <br>
> Describe : 해당 요청은 인증 후 Upstream 서버로 전달되는 OICD 토큰 및 헤더 정보를 통해 이메일을 응답해주는 API이다.

#### GET UserInfo(GET)
##### Google, GitHub

URL : http://localhost:{Proxy Server URL}/sample-api/v1/test/userinfo

Required Header
```
Key 1   : Cookie => Oauth2 Login을 통해 확인 가능
Value 1 : Oauth2 Login시 개발자 도구에서 확인된 쿠키
GitHub Cookie EX : _oauth2_proxy=g1PiJ..........
Google Cookie EX : _oauth2_proxy_git=_gYHRBg.........
```
> EX)
> <br>
> http://localhost:4180/sample-api/v1/test/userinfo  (Google)    
> http://localhost:4181/sample-api/v1/test/userinfo  (GitHub)
> Describe : 해당 요청은 인증 후 Upstream 서버로 전달되는 액세스 토큰을 사용한 Provider의 리소스 서버 조회 API를 사용한 사용자 정보 추출 API이다.

