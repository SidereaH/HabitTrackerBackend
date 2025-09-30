package habit.habittracker.models;

import habit.habittracker.dto.HabitDTO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "habits")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@EntityListeners(AuditingEntityListener.class)
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private Integer frequency;
    @CreatedDate
    private OffsetDateTime createdAt;
    @ElementCollection
    @CollectionTable(name = "habit_completions", joinColumns = @JoinColumn(name = "habit_id"))
    @Column(name = "done_date")
    private List<LocalDate> completedDates = new ArrayList<>();

    public static Habit fromDto(HabitDTO habitDto) {
        var habit =new Habit();
        habit.setId(habitDto.getId());
        habit.setTitle(habitDto.getTitle());
        habit.setDescription(habitDto.getDescription());
        habit.setFrequency(habitDto.getFrequency());
        if (habitDto.getCompletedDates() != null) {
            habit.setCompletedDates(habitDto.getCompletedDates());
        }
        return habit;
    }

}
