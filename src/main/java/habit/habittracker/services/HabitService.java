package habit.habittracker.services;

import habit.habittracker.models.Habit;
import habit.habittracker.repositories.HabitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HabitService {
    private final HabitRepository habitRepository;

    public HabitService(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public List<Habit> getAllHabits() {
        return habitRepository.findAll();
    }

    public Habit addHabit(Habit habit) {
        return habitRepository.save(habit);
    }

    public Habit updateHabit(Long id, Habit habitDetails) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habit not found: " + id));

        habit.setTitle(habitDetails.getTitle());
        habit.setDescription(habitDetails.getDescription());
        habit.setFrequency(habitDetails.getFrequency());

        return habitRepository.save(habit);
    }

    public void deleteHabit(Long id) {
        if (!habitRepository.existsById(id)) {
            throw new RuntimeException("Habit not found: " + id);
        }
        habitRepository.deleteById(id);
    }

    public Habit markHabitDone(Long id) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habit not found: " + id));

        habit.getCompletedDates().add(LocalDate.now());

        return habitRepository.save(habit);
    }
}
