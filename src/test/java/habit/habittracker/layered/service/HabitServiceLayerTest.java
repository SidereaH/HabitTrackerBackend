package habit.habittracker.layered.service;

import habit.habittracker.dto.HabitDTO;
import habit.habittracker.dto.HabitStatsDTO;
import habit.habittracker.models.Habit;
import habit.habittracker.repositories.HabitRepository;
import habit.habittracker.services.HabitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(HabitService.class)
class HabitServiceLayerTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitService habitService;

    private Habit habit;

    @BeforeEach
    void setup() {
        habit = new Habit();
        habit.setTitle("Test");
        habit.setDescription("Desc");
        habit.setFrequency(3);
        habit.setCreatedAt(LocalDate.now().minusDays(5).atStartOfDay());

        habit.setCompletedDates(new ArrayList<>());
        habit = entityManager.persistAndFlush(habit);
    }

    @Test
    void addHabit_shouldPersistThroughService() {
        HabitDTO dto = new HabitDTO(null, "Run", "Morning run", 2, LocalDateTime.now(), List.of());
        HabitDTO result = habitService.addHabit(dto);

        assertNotNull(result.getId());
        assertEquals("Run", result.getTitle());
        Habit persisted = entityManager.find(Habit.class, result.getId());
        assertNotNull(persisted);
    }

    @Test
    void getAllHabits_shouldReturnAllFromDatabase() {
        Habit h2 = new Habit();
        h2.setTitle("Second");
        entityManager.persistAndFlush(h2);

        List<HabitDTO> result = habitService.getAllHabits();
        assertTrue(result.size() >= 2);
        assertTrue(result.stream().anyMatch(h -> h.getTitle().equals("Test")));
    }

    @Test
    void updateHabit_shouldUpdateExistingHabit() {
        Habit update = new Habit();
        update.setTitle("Updated");
        update.setDescription("Updated Desc");
        update.setFrequency(5);

        HabitDTO result = habitService.updateHabit(habit.getId(), update);
        assertEquals("Updated", result.getTitle());
        assertEquals(5, result.getFrequency());

        Habit updated = habitRepository.findById(habit.getId()).orElseThrow();
        assertEquals("Updated", updated.getTitle());
    }

    @Test
    void updateHabit_shouldThrowIfNotFound() {
        Habit update = new Habit();
        update.setTitle("x");
        assertThrows(RuntimeException.class, () -> habitService.updateHabit(999L, update));
    }

    @Test
    void deleteHabit_shouldRemoveFromDatabase() {
        habitService.deleteHabit(habit.getId());
        assertFalse(habitRepository.findById(habit.getId()).isPresent());
    }

    @Test
    void deleteHabit_shouldThrowIfNotFound() {
        assertThrows(RuntimeException.class, () -> habitService.deleteHabit(123L));
    }

    @Test
    void markHabitDone_shouldAddDateOnce() {
        LocalDate date = LocalDate.now();
        HabitDTO dto = habitService.markHabitDone(habit.getId(), date);
        assertTrue(dto.getCompletedDates().contains(date));

        // второй вызов — не дублирует дату
        HabitDTO again = habitService.markHabitDone(habit.getId(), date);
        assertEquals(1, again.getCompletedDates().size());
    }

    @Test
    void markHabitDone_shouldThrowIfNotFound() {
        assertThrows(RuntimeException.class, () -> habitService.markHabitDone(999L, LocalDate.now()));
    }

    @Test
    void toggleHabitDone_shouldAddAndRemoveDate() {
        LocalDate date = LocalDate.now();

        // add
        HabitDTO added = habitService.toggleHabitDone(habit.getId(), date);
        assertTrue(added.getCompletedDates().contains(date));

        // remove
        HabitDTO removed = habitService.toggleHabitDone(habit.getId(), date);
        assertFalse(removed.getCompletedDates().contains(date));
    }

    @Test
    void toggleHabitDone_shouldThrowIfNotFound() {
        assertThrows(RuntimeException.class, () -> habitService.toggleHabitDone(999L, LocalDate.now()));
    }

    @Test
    void getStats_shouldCalculateCorrectly() {
        Habit h = new Habit();
        h.setTitle("Stats Habit");
        h.setDescription("Track streaks");
        h.setFrequency(1);
        h.setCreatedAt(LocalDate.now().minusDays(10).atStartOfDay());
        h.setCompletedDates(List.of(
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                LocalDate.now()
        ));
        Habit persisted = entityManager.persistAndFlush(h);

        HabitStatsDTO stats = habitService.getStats(persisted.getId());
        assertEquals(3, stats.getTotalDone());
        assertTrue(stats.getSuccessRate() > 0);
        assertEquals(3, stats.getCurrentStreak());
        assertTrue(stats.getLongestStreak() >= 3);
    }

    @Test
    void getStats_shouldHandleEmptyDatesAndNotFound() {
        Habit h = new Habit();
        h.setTitle("Empty");
        habit.setCreatedAt(LocalDate.now().minusDays(5).atStartOfDay());

        habit.setCompletedDates(new ArrayList<>());

        h = entityManager.persistAndFlush(h);

        HabitStatsDTO stats = habitService.getStats(h.getId());
        assertEquals(0, stats.getTotalDone());
        assertEquals(0, stats.getCurrentStreak());

        assertThrows(RuntimeException.class, () -> habitService.getStats(999L));
    }
}
