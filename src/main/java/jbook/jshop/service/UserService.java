package jbook.jshop.service;

import jbook.jshop.dto.UserDto;
import jbook.jshop.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public List<UserDto> findAllUsers() {
        return userMapper.findAllUsers();
    }

    // 사용자 목록을 처리하고 DB에 삽입 또는 업데이트
    public void insertOrUpdateUsers(List<UserDto> userList) {
        for (UserDto userDto : userList) {
            if (userMapper.existsById(userDto.getId())) {
                userMapper.updateUser(userDto); // 기존 사용자 업데이트
            } else {
                userMapper.insertUser(userDto); // 새로운 사용자 삽입
            }
        }
    }

    public String liveness() {
        String result = "success";
        try {
            if(StringUtils.equals(result, "success")) {
                int number = Integer.parseInt(result);
//                throw new RuntimeException("강제 익셉션 발생!");
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("[in service] ", e);
        }
    }
}
