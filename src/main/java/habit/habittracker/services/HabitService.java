package habit.habittracker.services;

import habit.habittracker.dto.HabitDTO;
import habit.habittracker.dto.HabitStatsDTO;
import habit.habittracker.models.Habit;
import habit.habittracker.repositories.HabitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HabitService {
    private final HabitRepository habitRepository;
    public HabitService(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public List<HabitDTO> getAllHabits() {
        return habitRepository.findAll().stream().map(HabitDTO::fromEntity).collect(Collectors.toList());
    }

    public HabitDTO addHabit(HabitDTO habit) {
        Habit saved = habitRepository.save(Habit.fromDto(habit));
        return HabitDTO.fromEntity(saved);
    }

    public HabitDTO updateHabit(Long id, Habit details) {
        Habit habit = habitRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        habit.setTitle(details.getTitle());
        habit.setDescription(details.getDescription());
        habit.setFrequency(details.getFrequency());
        Habit saved = habitRepository.save(habit);
        return HabitDTO.fromEntity(saved);
    }

    public void deleteHabit(Long id) {
        if (!habitRepository.existsById(id)) throw new RuntimeException("Not found");
        habitRepository.deleteById(id);
    }

    public HabitDTO markHabitDone(Long id, LocalDate date) {
        Habit habit = habitRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        List<LocalDate> dates = habit.getCompletedDates();
        if (!dates.contains(date)) {
            dates.add(date);
            habit.setCompletedDates(dates);
        }
        return HabitDTO.fromEntity(habitRepository.save(habit));
    }

    public HabitDTO toggleHabitDone(Long id, LocalDate date) {
        Habit habit = habitRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        List<LocalDate> dates = habit.getCompletedDates();
        if (dates.contains(date)) dates.remove(date);
        else dates.add(date);
        habit.setCompletedDates(dates);
        return HabitDTO.fromEntity(habitRepository.save(habit));
    }

    public HabitStatsDTO getStats(Long id) {
        Habit habit = habitRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        List<LocalDate> dates = new ArrayList<>(habit.getCompletedDates());
        // total done
        int total = dates.size();
        // successRate = doneDays / totalPossibleDaysForLifetime * 100
        long lifetimeDays = ChronoUnit.DAYS.between(habit.getCreatedAt(), LocalDate.now()) + 1;
        double successRate = lifetimeDays > 0 ? (total * 100.0 / lifetimeDays) : 0.0;

        // compute current streak and longest streak
        Collections.sort(dates);
        int longest = 0;
        int current = 0;
        LocalDate prev = null;
        for (LocalDate d : dates) {
            if (prev == null || prev.plusDays(1).isEqual(d)) {
                current = (prev == null ? 1 : current + 1);
            } else {
                longest = Math.max(longest, current);
                current = 1;
            }
            prev = d;
            longest = Math.max(longest, current);
        }
        // if the last date is not today, current streak should be calculated relative to today:
        int currentStreak = 0;
        LocalDate today = LocalDate.now();
        LocalDate pointer = today;
        while (dates.contains(pointer)) {
            currentStreak++;
            pointer = pointer.minusDays(1);
        }

        return new HabitStatsDTO(habit.getId(), total, successRate, currentStreak, longest);
    }
}

