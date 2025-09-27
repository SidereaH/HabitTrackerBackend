package habit.habittracker.integration.database;

import habit.habittracker.models.Habit;
import habit.habittracker.repositories.HabitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class HabitPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HabitRepository habitRepository;

    @Test
    void shouldPersistHabitWithCompletedDates() {
        // given
        Habit habit = new Habit();
        habit.setTitle("Habit with completions");
        habit.getCompletedDates().add(LocalDate.now());
        habit.getCompletedDates().add(LocalDate.now().minusDays(1));

        // when
        Habit saved = habitRepository.save(habit);
        entityManager.flush();
        entityManager.clear();

        // then
        Habit found = habitRepository.findById(saved.getId()).orElseThrow();
        assertEquals(2, found.getCompletedDates().size());
        assertTrue(found.getCompletedDates().contains(LocalDate.now()));
    }

    @Test
    void shouldUpdateHabitProperties() {
        // given
        Habit habit = new Habit();
        habit.setTitle("Original");
        habit.setFrequency(1);
        Habit saved = entityManager.persistAndFlush(habit);

        // when
        saved.setTitle("Updated");
        saved.setFrequency(2);
        Habit updated = habitRepository.save(saved);
        entityManager.flush();

        // then
        Habit found = entityManager.find(Habit.class, saved.getId());
        assertEquals("Updated", found.getTitle());
        assertEquals(2, found.getFrequency());
    }

    @Test
    void shouldMaintainCompletedDatesAfterUpdate() {
        // given
        Habit habit = new Habit();
        habit.setTitle("Test Habit");
        habit.getCompletedDates().add(LocalDate.now());
        Habit saved = entityManager.persistAndFlush(habit);

        // when
        saved.setTitle("Updated Title");
        Habit updated = habitRepository.save(saved);
        entityManager.flush();

        // then
        Habit found = entityManager.find(Habit.class, saved.getId());
        assertEquals("Updated Title", found.getTitle());
        assertEquals(1, found.getCompletedDates().size());
    }
}