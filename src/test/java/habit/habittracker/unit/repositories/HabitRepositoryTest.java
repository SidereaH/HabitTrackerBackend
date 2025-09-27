package habit.habittracker.unit.repositories;

import habit.habittracker.models.Habit;
import habit.habittracker.repositories.HabitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class HabitRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HabitRepository habitRepository;

    @Test
    void save_shouldPersistHabit() {
        // given
        Habit habit = new Habit();
        habit.setTitle("Test Habit");
        habit.setDescription("Test Description");
        habit.setFrequency(1);

        // when
        Habit saved = habitRepository.save(habit);

        // then
        assertNotNull(saved.getId());
        assertEquals("Test Habit", saved.getTitle());
        assertEquals("Test Description", saved.getDescription());
        assertEquals(1, saved.getFrequency());
    }

    @Test
    void findById_shouldReturnHabit() {
        // given
        Habit habit = new Habit();
        habit.setTitle("Find Me");
        Habit persisted = entityManager.persistAndFlush(habit);

        // when
        Optional<Habit> found = habitRepository.findById(persisted.getId());

        // then
        assertTrue(found.isPresent());
        assertEquals("Find Me", found.get().getTitle());
    }

    @Test
    void findAll_shouldReturnAllHabits() {
        // given
        Habit habit1 = new Habit();
        habit1.setTitle("Habit 1");
        Habit habit2 = new Habit();
        habit2.setTitle("Habit 2");

        entityManager.persist(habit1);
        entityManager.persist(habit2);
        entityManager.flush();

        // when
        List<Habit> habits = habitRepository.findAll();

        // then
        assertEquals(2, habits.size());
    }

    @Test
    void delete_shouldRemoveHabit() {
        // given
        Habit habit = new Habit();
        habit.setTitle("To Delete");
        Habit persisted = entityManager.persistAndFlush(habit);

        // when
        habitRepository.deleteById(persisted.getId());
        entityManager.flush();

        // then
        Optional<Habit> found = habitRepository.findById(persisted.getId());
        assertFalse(found.isPresent());
    }
}