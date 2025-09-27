package habit.habittracker.dto;

import habit.habittracker.models.Habit;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;// DTO для возврата из API
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HabitDTO {
    private Long id;
    private String title;
    private String description;
    private Integer frequency;
    private OffsetDateTime createdAt;
    private List<LocalDate> completedDates;

    public static HabitDTO fromEntity(Habit habit) {
        return new HabitDTO(
                habit.getId(),
                habit.getTitle(),
                habit.getDescription(),
                habit.getFrequency(),
                habit.getCreatedAt(),
                new ArrayList<>(habit.getCompletedDates()) // Копируем коллекцию
        );
    }
}
