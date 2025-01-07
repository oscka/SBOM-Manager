# SBOM-Manager

템플릿 코드 테스트를 위한 PostgreSQL DB 컨테이너 생성 및 초기화 방법

-- 1. 도커 이미지 Pull 및 컨테이너 생성
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

-- 2. PostgreSQL 접속 후 DB 생성
```
docker exec -it postgres-db /bin/sh
psql -U postgres
CREATE DATABASE test_spring;
```

-- 3. 사용자 생성 및 권한 부여
```
CREATE USER test WITH PASSWORD '1234';
ALTER USER test WITH SUPERUSER;
```

-- 4. 생성한 DB로 접속 후 스키마 생성
```
exit
psql -U test -d test_spring -W
CREATE SCHEMA test_schema;
```

-- 5. 생성한 스키마에 테이블 생성
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
    bom_format VARCHAR(255),
    spec_version VARCHAR(255),
    component_type VARCHAR(255),
    name VARCHAR(255),
    component_count INTEGER,
    client_tool VARCHAR(255),
    client_tool_version VARCHAR(255),
    data JSONB
);
```

-- 6. 생성한 테이블에 테스트 데이터 삽입
```
INSERT INTO test_schema.users (username, email, password, full_name, phone) 
VALUES 
('john_doe', 'john@example.com', 'password123', 'John Doe', '010-1234-5678'),
('jane_smith', 'jane@example.com', 'password456', 'Jane Smith', '010-8765-4321'),
('mike_wilson', 'mike@example.com', 'password789', 'Mike Wilson', '010-9999-8888');
```
---

## API 테스트(Swagger, Postman..)


### Simple Test API

#### GET ALL User(GET)

URL : http://localhost:8088/sample-api/v1/test/user

#### Create User(POST)

URL : http://localhost:8088/sample-api/v1/test/user

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

---
### SBOM API

### Create Sbom(POST)

URL : http://localhost:8088/sample-api/v1/test/managed/sbom

EX) Body
[https://osc-korea.atlassian.net/wiki/spaces/consulting/pages/1274150926/SBOM+Generator#%EA%B0%81-%EC%96%B8%EC%96%B4%EC%9D%98-%ED%8C%8C%EC%9D%BC,%EC%9D%B4%EB%AF%B8%EC%A7%80-%EA%B8%B0%EB%B0%98-SBOM](https://osc-korea.atlassian.net/wiki/spaces/consulting/pages/1279983707/SBOM#Create-Sbom(POST))
링크에 존재하는 Syft 로 생성한 CycloneDX, SPDX Form의 json을 붙여넣은 후 Send

---
## OAuth2 Proxy Test(작성중)

### OAuth 2.0 - Client 등록(Google, Github)
#### Google
1. OAuth 동의 화면 구성 요약 (링크 참조)

![Google EX 02](https://github.com/user-attachments/assets/19d58fed-3e59-4bc5-aa2f-3c08588c7ca9)

2. OAuth Client ID 만들기

![Google EX](https://github.com/user-attachments/assets/57ac04f4-9805-43e0-a8a6-2088c0111af3)

> 승인된 리디렉션 URL : {Proxy Server URL}/oauth2/callback
> EX) http://localhost:4180/oauth2/callback
<br>

#### GitHub
OAuth App 구성 예시

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

### Test Upstream Server APIs Through a Proxy Server

> Default EndPoints (version : 7.7.x)
> <br>
> https://oauth2-proxy.github.io/oauth2-proxy/features/endpoints/

#### GET Users Email(GET)
##### ALL

> URL : http://localhost:{Proxy Server URL}/sample-api/v1/test/email
> <br>
> EX)
> <br>
> http://localhost:4180/sample-api/v1/test/email  (Google)    
> http://localhost:4181/sample-api/v1/test/email  (GitHub)
> <br>
> Describe : 해당 요청은 인증 후 Upstream 서버로 전달되는 OICD 토큰 및 헤더 정보를 통해 추출

#### GET UserInfo(GET)
##### Google

> URL : http://localhost:4180/sample-api/v1/test/userinfo
> <br>
> Describe : 해당 요청은 인증 후 Upstream 서버로 전달되는 액세스 토큰을 사용한 Provider의 리소스 서버 조회 API를 사용한 사용자 정보 추출

#### GET UserInfo(GET)
##### GitHub

> URL : http://localhost:4181/sample-api/v1/test/userinfoGit
> <br>
> Describe : 해당 요청은 인증 후 Upstream 서버로 전달되는 액세스 토큰을 사용한 Provider의 리소스 서버 조회 API를 사용한 사용자 정보 추출



