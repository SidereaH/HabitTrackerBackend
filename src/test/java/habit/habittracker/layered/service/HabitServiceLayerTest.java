package habit.habittracker.layered.service;

import habit.habittracker.dto.HabitDTO;
import habit.habittracker.models.Habit;
import habit.habittracker.repositories.HabitRepository;
import habit.habittracker.services.HabitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
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

    @Test
    void addHabit_shouldPersistThroughService() {
        // given
        Habit habit = new Habit();
        habit.setTitle("Test Habit");
        habit.setDescription("Test Description");
        habit.setFrequency(1);

        // when
        HabitDTO result = habitService.addHabit(habit);

        // then
        assertNotNull(result.getId());
        assertEquals("Test Habit", result.getTitle());

        // Проверяем через EntityManager
        Habit persisted = entityManager.find(Habit.class, result.getId());
        assertNotNull(persisted);
        assertEquals("Test Habit", persisted.getTitle());
    }

    @Test
    void getAllHabits_shouldReturnAllFromDatabase() {
        // given
        Habit habit1 = new Habit();
        habit1.setTitle("Habit 1");
        Habit habit2 = new Habit();
        habit2.setTitle("Habit 2");

        entityManager.persist(habit1);
        entityManager.persist(habit2);
        entityManager.flush();

        // when
        List<HabitDTO> result = habitService.getAllHabits();

        // then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(h -> "Habit 1".equals(h.getTitle())));
        assertTrue(result.stream().anyMatch(h -> "Habit 2".equals(h.getTitle())));
    }

    @Test
    void updateHabit_shouldUpdateInDatabase() {
        // given
        Habit habit = new Habit();
        habit.setTitle("Original Title");
        habit.setDescription("Original Desc");
        habit.setFrequency(1);
        Habit persisted = entityManager.persistAndFlush(habit);

        Habit updateDetails = new Habit();
        updateDetails.setTitle("Updated Title");
        updateDetails.setDescription("Updated Desc");
        updateDetails.setFrequency(2);

        // when
        HabitDTO result = habitService.updateHabit(persisted.getId(), updateDetails);

        // then
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Desc", result.getDescription());
        assertEquals(2, result.getFrequency());

        // Проверяем через репозиторий
        Optional<Habit> updated = habitRepository.findById(persisted.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated Title", updated.get().getTitle());
    }

    @Test
    void markHabitDone_shouldAddCompletionDateToDatabase() {
        // given
        Habit habit = new Habit();
        habit.setTitle("Test Habit");
        Habit persisted = entityManager.persistAndFlush(habit);

        // when
        HabitDTO result = habitService.markHabitDone(persisted.getId());

        // then
        assertTrue(result.getCompletedDates().contains(LocalDate.now()));
        assertEquals(1, result.getCompletedDates().size());

        // Проверяем через репозиторий
        Optional<Habit> updated = habitRepository.findById(persisted.getId());
        assertTrue(updated.isPresent());
        assertEquals(1, updated.get().getCompletedDates().size());
    }

    @Test
    void deleteHabit_shouldRemoveFromDatabase() {
        // given
        Habit habit = new Habit();
        habit.setTitle("To Delete");
        Habit persisted = entityManager.persistAndFlush(habit);

        // when
        habitService.deleteHabit(persisted.getId());

        // then
        Optional<Habit> found = habitRepository.findById(persisted.getId());
        assertFalse(found.isPresent());
    }
}