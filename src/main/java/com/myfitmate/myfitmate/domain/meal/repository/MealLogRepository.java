package com.myfitmate.myfitmate.domain.meal.repository;

import com.myfitmate.myfitmate.domain.meal.entity.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MealLogRepository extends JpaRepository<MealLog, Long> {
    List<MealLog> findByMealId(Long mealId);

    @Query("SELECT m FROM MealLog m WHERE m.userId = :userId AND DATE(m.actionTime) = :date")
    List<MealLog> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

}
