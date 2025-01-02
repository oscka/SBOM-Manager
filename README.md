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
    name VARCHAR(255),
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

## API Test Tool로 Controller에 매핑되어있는 주소로 API 테스트

### GET ALL User(GET)

http://localhost:8088/sample-api/v1/test/user

### Create User(POST)

http://localhost:8088/sample-api/v1/test/user

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
### Edit User(PUT)

http://localhost:8088/sample-api/v1/test/user/{id}

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
### Delete User(DELETE)

http://localhost:8088/sample-api/v1/test/user/{id}

### Create Sbom(POST)

http://localhost:8088/sample-api/v1/test/managed/sbom

EX) Body
https://osc-korea.atlassian.net/wiki/spaces/consulting/pages/1274150926/SBOM+Generator#%EA%B0%81-%EC%96%B8%EC%96%B4%EC%9D%98-%ED%8C%8C%EC%9D%BC,%EC%9D%B4%EB%AF%B8%EC%A7%80-%EA%B8%B0%EB%B0%98-SBOM
링크에 존재하는 Syft CycloneDX Form의 json을 붙여넣기
