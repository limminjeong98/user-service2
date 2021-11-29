package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
// @RequestMapping("/user-service/")    apigateway-service의 application.yml에서 변경해서 더 이상 필요 없음
public class UserController {

    private Environment env;
    private UserService userService;

    @Autowired
    private Greeting greeting;

    //    @Autowired
//    public UserController(Environment env) {
//        this.env = env;
//    }
    @Autowired
    public UserController(Environment env, UserService userService) {
        this.env = env;
        this.userService = userService;
    }

    // @GetMapping("/health_check")
    // User Microservice와 Spring Cloud Gateway를 연동하기 위해 URI에 일괄적으로 '/user-service'추가 변경
    // => 다시 Controller 상단에 @RequestMapping에 추가하여 prefix 격으로 사용
//    @GetMapping("/user-service/health_check")
    @GetMapping("/health_check")
    public String status() {
//        return "It's Working in User Service";
        return String.format("It's Working in User Service"
                + ", port(local.server.port)= " + env.getProperty("local.server.port")
                + ", port(server.port)= " + env.getProperty("server.port")
                + ", token expiration time= " + env.getProperty("token.expiration_time")
                + ", token secret " + env.getProperty("token.secret")
        );
    }

    @GetMapping("/welcome")
    public String welcome() {
//        return env.getProperty("greeting.message");
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(user, UserDto.class);
        userService.createUser(userDto);

        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
//        return new ResponseEntity(HttpStatus.CREATED);
    }

    @GetMapping(value = "/users")
    public ResponseEntity<List<ResponseUser>> getUsers() {
        Iterable<UserEntity> userList = userService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();
        userList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseUser.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping(value = "/users/{userId}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable("userId") String userId) {
        UserDto userDto = userService.getUserByUserId(userId);
        ResponseUser returnValue = new ModelMapper().map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.OK).body(returnValue);
    }

}
