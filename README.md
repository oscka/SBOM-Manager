# SBOM-Manager

템플릿 코드 테스트를 위한 PostgreSQL DB 초기화 방법

-- 사용자 생성 및 권한 부여
```
CREATE USER test WITH PASSWORD '1234';
ALTER USER test WITH SUPERUSER;
```


-- 데이터베이스 생성
```
CREATE DATABASE test_spring;
```

-- 테이블 생성
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
```

-- 테스트 데이터 삽입
```
INSERT INTO test_schema.users (username, email, password, full_name, phone) 
VALUES 
('john_doe', 'john@example.com', 'password123', 'John Doe', '010-1234-5678'),
('jane_smith', 'jane@example.com', 'password456', 'Jane Smith', '010-8765-4321'),
('mike_wilson', 'mike@example.com', 'password789', 'Mike Wilson', '010-9999-8888');
```

## API Test Tool로 Controller에 매핑되어있는 주소로 API 테스트

### GET ALL User

http://localhost:8088/sample-api/v1/test/user

### Create User

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
### Edit User

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
### Delete User

http://localhost:8088/sample-api/v1/test/user/{id}
