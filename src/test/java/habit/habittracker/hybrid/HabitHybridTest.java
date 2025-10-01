package habit.habittracker.hybrid;

import habit.habittracker.controllers.HabitController;
import habit.habittracker.dto.HabitDTO;
import habit.habittracker.models.Habit;
import habit.habittracker.repositories.HabitRepository;
import habit.habittracker.services.HabitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class HabitHybridTest {

    @Autowired
    private HabitController habitController;

    @Autowired
    private HabitService habitService;

    @Autowired
    private HabitRepository habitRepository;



    @Test
    void createHabit_withRealServiceAndRepository() {
        // given
        Habit habit = new Habit();
        habit.setTitle("Hybrid Test");
        habit.setDescription("Test Description");
        habit.setFrequency(1);

        HabitDTO result = habitController.createHabit(HabitDTO.fromEntity(habit));

        // then
        assertNotNull(result.getId());
        assertEquals("Hybrid Test", result.getTitle());

        Optional<Habit> found = habitRepository.findById(result.getId());
        assertTrue(found.isPresent());
        assertEquals("Hybrid Test", found.get().getTitle());
    }

    @Test
    @Transactional
    void markHabitDone_withRealComponents() {
        Habit habit = new Habit();
        habit.setTitle("Test Habit");
        habit.setCompletedDates(new ArrayList<>());
        Habit saved = habitRepository.save(habit);

        HabitDTO result = habitController.markDone(saved.getId(), LocalDate.now().toString());

        assertEquals(1, result.getCompletedDates().size());
        assertTrue(result.getCompletedDates().contains(LocalDate.now()));

        List<HabitDTO> allHabits = habitService.getAllHabits();
        HabitDTO serviceHabit = allHabits.stream()
                .filter(h -> h.getId().equals(saved.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(1, serviceHabit.getCompletedDates().size());
    }
}