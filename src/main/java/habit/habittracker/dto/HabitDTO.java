package habit.habittracker.dto;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.OffsetDateTime;
import java.util.List;

public class HabitDTO {
    private Long id;
    private String title;
    private String description;
    private Integer frequency;
    private OffsetDateTime createdAt;
    private List<OffsetDateTime> completedDates;
}
