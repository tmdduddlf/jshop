// src/main/java/com/example/mapper/DrinkStatusMapper.java
package jbook.jshop.mapper;

import jbook.jshop.dto.DrinkStatus;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DrinkStatusMapper {
    List<DrinkStatus> findByDateRange(String startDate, String endDate);
    DrinkStatus findById(Long id);
    void insert(DrinkStatus drinkStatus);
    void update(DrinkStatus drinkStatus);
    void delete(Long id);
}
