package habit.habittracker.dto;

import habit.habittracker.models.Habit;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class HabitDTO {
    private Long id;
    private String title;
    private String description;
    private Integer frequency;
    private List<LocalDate> completedDates;

    public static HabitDTO fromEntity(Habit habit) {
        return new HabitDTO(
                habit.getId(),
                habit.getTitle(),
                habit.getDescription(),
                habit.getFrequency(),
                new ArrayList<>(habit.getCompletedDates()) // Копируем коллекцию
        );
    }
}
