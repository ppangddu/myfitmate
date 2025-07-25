package com.myfitmate.myfitmate.domain.ai;

import com.myfitmate.myfitmate.domain.exercise.entity.ExerciseLog;
import com.myfitmate.myfitmate.domain.exercise.repository.ExerciseLogRepository;
import com.myfitmate.myfitmate.domain.food.entity.Food;
import com.myfitmate.myfitmate.domain.meal.entity.Meal;
import com.myfitmate.myfitmate.domain.meal.entity.MealFood;
import com.myfitmate.myfitmate.domain.meal.entity.MealLog;
import com.myfitmate.myfitmate.domain.meal.entity.MealType;
import com.myfitmate.myfitmate.domain.meal.repository.MealFoodRepository;
import com.myfitmate.myfitmate.domain.meal.repository.MealLogRepository;
import com.myfitmate.myfitmate.domain.meal.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final ExerciseLogRepository exerciseLogRepository;
    private final MealLogRepository mealLogRepository;
    private final MealRepository mealRepository;
    private final MealFoodRepository mealFoodRepository;
    private final OpenAiService openAiService;

    public String generateTodayFeedback(Long userId) {
        LocalDate today = LocalDate.now();

        // 운동 요약
        List<ExerciseLog> exerciseLogs = exerciseLogRepository.findByUserIdAndDate(userId, today);
        StringBuilder exerciseSummary = new StringBuilder();
        for (ExerciseLog log : exerciseLogs) {
            exerciseSummary.append("- ")
                    .append(log.getExercise().getName())
                    .append(" ")
                    .append(log.getDurationMinutes())
                    .append("분\n");
        }

        // 식단 요약 + 탄단지 계산
        List<MealLog> mealLogs = mealLogRepository.findByUserIdAndDate(userId, today);
        Map<MealType, List<String>> meals = new HashMap<>();
        float totalCarb = 0, totalProtein = 0, totalFat = 0;

        for (MealLog log : mealLogs) {
            Meal meal = mealRepository.findById(log.getMealId()).orElse(null);
            if (meal == null) continue;

            List<MealFood> mealFoods = mealFoodRepository.findByMeal(meal);
            List<String> foodNames = new ArrayList<>();

            for (MealFood mf : mealFoods) {
                Food food = mf.getFood();
                float multiplier = mf.getQuantity() / (food.getStandardAmount() != null ? food.getStandardAmount() : 1f);
                totalCarb += Optional.ofNullable(food.getCarbohydrate()).orElse(0f) * multiplier;
                totalProtein += Optional.ofNullable(food.getProtein()).orElse(0f) * multiplier;
                totalFat += Optional.ofNullable(food.getFat()).orElse(0f) * multiplier;

                foodNames.add(food.getName());
            }

            meals.computeIfAbsent(log.getMealType(), k -> new ArrayList<>()).addAll(foodNames);
        }

        StringBuilder mealSummary = new StringBuilder();
        for (Map.Entry<MealType, List<String>> entry : meals.entrySet()) {
            mealSummary.append("- ").append(entry.getKey()).append(": ")
                    .append(String.join(", ", entry.getValue()))
                    .append("\n");
        }

        // 영양소 요약
        String nutritionSummary = String.format("""
                [섭취한 영양소 요약]
                - 탄수화물: %.1fg
                - 단백질: %.1fg
                - 지방: %.1fg
                """, totalCarb, totalProtein, totalFat);

        // GPT 프롬프트 생성
        String prompt = String.format("""
                아래는 사용자 %d의 오늘 운동 및 식단 요약입니다.
                
                이 사용자는 다이어트 목적이 아닌, 건강 유지를 위해 식단과 운동을 관리 중입니다.
                총 섭취 칼로리와 소모 칼로리를 비교하고, 탄수화물·단백질·지방의 섭취량과 비율이 균형 잡혔는지 확인해주세요.
                전체 섭취량이 부족한지 또는 특정 영양소가 과하거나 부족한지도 함께 고려해 주세요.
                소모한 칼로리에 대해서 운동같은거 추천해줘도 좋아요. 맞춤법도 신경써주세요.
                
                답변은 건강 코치처럼 조언을 주는 말투로, **3줄 이내의 평문 텍스트**로 작성해주세요.
                HTML, 마크다운, 구체적인 숫자 반복 없이 말로만 요약해주세요.
                답변은 꼭 1회만 작성하고, 중복하지 마세요.
                
                [운동 요약]
                %s
                
                [식단 요약]
                %s
                
                [섭취한 영양소 요약]
                %s
                """, userId, exerciseSummary, mealSummary, nutritionSummary).trim();

        return openAiService.ask(prompt);
    }
}
