package com.osckorea.sbommanager.controller;

import com.osckorea.sbommanager.model.User;
import com.osckorea.sbommanager.service.TestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sample-api/v1/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    //    private final CommandBoardService commandService;
    private final TestService testService;

    @GetMapping()
    public String test01() {
        return "hello";
    }

    //직접 생성한 쿼리용
    @GetMapping("/user02")
    public ResponseEntity<List<User>> getAllUser() {
        List<User> users  = testService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/user")
    public ResponseEntity<Iterable<User>> getAllUser02() {
        Iterable<User> users  = testService.getAllUsers02();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/user")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = testService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<User> editUser(@PathVariable("id") Long id, @RequestBody User user) {
        User createdUser = testService.editUser(id, user);
        return new ResponseEntity<>(createdUser, HttpStatus.OK);
    }

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
