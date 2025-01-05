package com.osckorea.sbommanager.controller;

import com.osckorea.sbommanager.domian.entity.User;
import com.osckorea.sbommanager.service.TestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/sample-api/v1/test")
@RequiredArgsConstructor
@Tag(name = "Sample API", description = "템플릿 샘플 API 그룹")
@Slf4j
public class TestController {
    //    private final CommandBoardService commandService;
    private final TestService testService;

    @GetMapping("/email")
    public String getUserEmail(@RequestHeader(name = "X-Forwarded-Email", required = false) String email, HttpServletRequest request) {
        if (email != null && !email.isEmpty()) {
            return "Authenticated user email: " + email;
        } else {
            return "No authenticated user email found";
        }
    }

    @GetMapping("/userinfo")
    public ResponseEntity<String> getUserInfo(@RequestHeader("Authorization") String authorizationHeader) {
        // Bearer Token 추출
        String accessToken = authorizationHeader.replace("Bearer ", "");

        // Google API 호출
        String userInfo = fetchGoogleUserInfo(accessToken);

        return ResponseEntity.ok(userInfo);
    }

    private String fetchGoogleUserInfo(String accessToken) {
        String googleUserInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo";

        // Authorization 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        // Google API 호출
        ResponseEntity<String> response = restTemplate.exchange(googleUserInfoEndpoint,
                org.springframework.http.HttpMethod.GET,
                entity,
                String.class);

        return response.getBody();
    }

    @Operation(summary = "Server Test", description = "서버를 테스트합니다.")
    @GetMapping()
    public String test01() {
        return "hello";
    }

    //직접 생성한 쿼리용
    @Operation(summary = "Get All User", description = "직접 생성한 쿼리로 User를 가져옵니다.")
    @GetMapping("/user02")
    public ResponseEntity<List<User>> getAllUser() {
        List<User> users  = testService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get All User", description = "모든 User를 가져옵니다.")
    @GetMapping("/user")
    public ResponseEntity<Iterable<User>> getAllUser02() {
        Iterable<User> users  = testService.getAllUsers02();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "User Create", description = "User를 생성합니다.")
    @PostMapping("/user")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = testService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(summary = "User Edit", description = "User를 수정합니다.")
    @PutMapping("/user/{id}")
    public ResponseEntity<User> editUser(@PathVariable("id") Long id, @RequestBody User user) {
        User createdUser = testService.editUser(id, user);
        return new ResponseEntity<>(createdUser, HttpStatus.OK);
    }

    @Operation(summary = "User Delete", description = "User를 삭제합니다.")
    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("id") Long id) {
        testService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }




//	@Operation(summary = "사용자 수정", description = "사용자 수정 기능입니다.")
//	@PutMapping("/user")
//	public ResponseEntity<ResultMessage> getAllUser() {
//		List<User> users  = testUserService.getAllUsers();
//
//		ResultMessage resultMessage = new ResultMessage();
//		resultMessage.setObj(users);
//		resultMessage.setMessage("모든 사용자 정보를 성공적으로 조회했습니다.");
//		resultMessage.setSuccessYn("success");
//		return ResponseEntity.ok(resultMessage);
//	}
//
//	@Operation(summary = "사용자 삭제", description = "사용자 삭제 기능입니다.")
//	@DeleteMapping("/user")
//	public ResponseEntity<ResultMessage> getAllUser() {
//		List<User> users  = testUserService.getAllUsers();
//
//		ResultMessage resultMessage = new ResultMessage();
//		resultMessage.setObj(users);
//		resultMessage.setMessage("모든 사용자 정보를 성공적으로 조회했습니다.");
//		resultMessage.setSuccessYn("success");
//		return ResponseEntity.ok(resultMessage);
//	}



//	@Operation(summary = "게시판 입력", description = "게시판 입력 기능입니다.")
//	@PostMapping()
//	public ResponseEntity<ResultMessage> insert( @RequestBody  Board paramBoard) {
//		Board board  = commandService.insert(paramBoard);
//        if(board.getNum() > 0){
//            return getResponseEntity(1);
//        }
//		return getResponseEntity(0);
//
//	}

//	@Operation(summary = "게시판 수정", description = "게시판 수정 기능입니다.")
//	@PutMapping()
//	public ResponseEntity<ResultMessage> updateBoard(@RequestBody Board paramBoard) {
//		int result = commandService.updateBoard(paramBoard);
//		return getResponseEntity(result);
//
//	}

//	@Operation(summary = "게시판 삭제", description = "게시판 삭제 기능입니다.")
//	@DeleteMapping("/{num}")
//	public ResponseEntity<ResultMessage> delete(@PathVariable("num") int num) {
//		int result = commandService.delete(num);
//		return getResponseEntity(result);
//
//	}
//
//    private ResponseEntity<ResultMessage> getResponseEntity(int result) {
//		if(result > 0) {
//			ResultMessage resultMessage= ResultMessage.builder().successYn("Y").message("정상").build();
//			return ResponseEntity.ok(resultMessage);
//		}
//		ResultMessage resultMessage= ResultMessage.builder().successYn("N").message("오류").build();
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultMessage);
//	}


}
