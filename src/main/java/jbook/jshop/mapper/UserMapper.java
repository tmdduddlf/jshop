package jbook.jshop.mapper;

import jbook.jshop.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserMapper {

    // 사용자 조회 쿼리 메서드
    List<UserDto> findAllUsers();

    // 사용자 추가
    void insertUser(UserDto user);

    // 사용자 업데이트
    void updateUser(UserDto user);

    // ID로 존재 여부 확인
    boolean existsById(String id);

}
