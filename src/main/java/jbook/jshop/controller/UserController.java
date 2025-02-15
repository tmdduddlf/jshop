package jbook.jshop.controller;

import jbook.jshop.dto.ApiResponse;
import jbook.jshop.dto.UserDto;
import jbook.jshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/findAllUsers")
    public ResponseEntity<ApiResponse> findAllUsers() {
        ApiResponse response = new ApiResponse();
        response.setCode(0);
        response.setData(userService.findAllUsers());
        response.setMessage("");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/insertOrUpdateUsers")
    public ResponseEntity<ApiResponse> insertOrUpdateUsers(@RequestBody List<UserDto> userList) {
        userService.insertOrUpdateUsers(userList);

        ApiResponse response = new ApiResponse();
        response.setCode(0);
        response.setData(null);
        response.setMessage("User data inserted/updated successfully.");

//        return "User data inserted/updated successfully.";
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/test")
    @GetMapping("/liveness")
    public ResponseEntity<ApiResponse> liveness() {
        ApiResponse response = new ApiResponse();
        response.setCode(0);
        response.setData(userService.liveness());
        response.setMessage("");
        return ResponseEntity.ok(response);
    }



//    @PostMapping("/create-table")
//    public String createTable() {
//        userService.createTable();
//        return "Table created successfully!";
//    }
//
//    @PostMapping("/insert-user")
//    public String insertUser(@RequestParam String name) {
//        userService.insertUser(name);
//        return "User inserted successfully!";
//    }


}
