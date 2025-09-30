package habit.habittracker.services;

import habit.habittracker.dto.HabitDTO;
import habit.habittracker.models.Habit;
import habit.habittracker.repositories.HabitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HabitService {
    private final HabitRepository habitRepository;

    public HabitService(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public List<HabitDTO> getAllHabits() {
        return habitRepository.findAll().stream()
                .map(HabitDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public HabitDTO addHabit(HabitDTO habit) {
        return HabitDTO.fromEntity(habitRepository.save(Habit.fromDto(habit)));
    }

    public HabitDTO updateHabit(Long id, Habit habitDetails) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habit not found: " + id));

        habit.setTitle(habitDetails.getTitle());
        habit.setDescription(habitDetails.getDescription());
        habit.setFrequency(habitDetails.getFrequency());

        return HabitDTO.fromEntity(habitRepository.save(habit));
    }

    public void deleteHabit(Long id) {
        if (!habitRepository.existsById(id)) {
            throw new RuntimeException("Habit not found: " + id);
        }
        habitRepository.deleteById(id);
    }

    public HabitDTO markHabitDone(Long id) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habit not found: " + id));

        habit.getCompletedDates().add(LocalDate.now());

        return HabitDTO.fromEntity(habitRepository.save(habit));
    }
}
