package jbook.jshop.service;

import jbook.jshop.dto.UserDto;
import jbook.jshop.mapper.UserMapper;
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
}
