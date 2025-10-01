package habit.habittracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HabitStatsDTO {
    private Long habitId;
    private int totalDone;
    private double successRate;
    private int currentStreak;
    private int longestStreak;

}