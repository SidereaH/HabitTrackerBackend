package habit.habittracker.unit.services;

import habit.habittracker.dto.HabitDTO;
import habit.habittracker.dto.HabitStatsDTO;
import habit.habittracker.models.Habit;
import habit.habittracker.repositories.HabitRepository;
import habit.habittracker.services.HabitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceUnitTest {

    @Mock
    private HabitRepository habitRepository;

    @InjectMocks
    private HabitService habitService;

    @Test
    void getAllHabits_shouldReturnAllHabits() {
        Habit habit1 = new Habit(1L, "Exercise", "Daily exercise", 1, null, List.of());
        Habit habit2 = new Habit(2L, "Reading", "Read books", 1, null, List.of());
        when(habitRepository.findAll()).thenReturn(List.of(habit1, habit2));

        List<HabitDTO> result = habitService.getAllHabits();

        assertEquals(2, result.size());
        verify(habitRepository).findAll();
    }

    @Test
    void addHabit_shouldSaveAndReturnHabit() {
        Habit habitToSave = new Habit(null, "Meditation", "Daily meditation", 1, null, List.of());
        Habit savedHabit = new Habit(1L, "Meditation", "Daily meditation", 1, null, List.of());
        when(habitRepository.save(any(Habit.class))).thenReturn(savedHabit);

        HabitDTO result = habitService.addHabit(HabitDTO.fromEntity(habitToSave));

        assertEquals(1L, result.getId());
        assertEquals("Meditation", result.getTitle());
        verify(habitRepository).save(any(Habit.class));
    }

    @Test
    void updateHabit_shouldUpdateExistingHabit() {
        Long habitId = 1L;
        Habit existingHabit = new Habit(habitId, "Old", "Desc", 1, null, List.of());
        Habit details = new Habit(null, "New", "Updated", 2, null, List.of());
        when(habitRepository.findById(habitId)).thenReturn(Optional.of(existingHabit));
        when(habitRepository.save(existingHabit)).thenReturn(existingHabit);

        HabitDTO result = habitService.updateHabit(habitId, details);

        assertEquals("New", result.getTitle());
        assertEquals(2, result.getFrequency());
        verify(habitRepository).save(existingHabit);
    }

    @Test
    void updateHabit_shouldThrow_whenNotFound() {
        when(habitRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> habitService.updateHabit(99L, new Habit()));
    }

    @Test
    void deleteHabit_shouldDeleteExistingHabit() {
        when(habitRepository.existsById(1L)).thenReturn(true);
        habitService.deleteHabit(1L);
        verify(habitRepository).deleteById(1L);
    }

    @Test
    void deleteHabit_shouldThrow_whenNotFound() {
        when(habitRepository.existsById(1L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> habitService.deleteHabit(1L));
    }

    @Test
    void markHabitDone_shouldAddDateOnce() {
        LocalDate date = LocalDate.now();
        Habit habit = new Habit(1L, "Exercise", "Daily", 1, null, new ArrayList<>());
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(habitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HabitDTO result = habitService.markHabitDone(1L, date);

        assertTrue(result.getCompletedDates().contains(date));
        assertEquals(1, result.getCompletedDates().size());

        // повторный вызов не добавляет дубль
        HabitDTO again = habitService.markHabitDone(1L, date);
        assertEquals(1, again.getCompletedDates().size());
    }

    @Test
    void markHabitDone_shouldThrow_whenNotFound() {
        when(habitRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> habitService.markHabitDone(1L, LocalDate.now()));
    }

    @Test
    void toggleHabitDone_shouldAddAndRemoveDate() {
        LocalDate today = LocalDate.now();
        Habit habit = new Habit(1L, "Toggle", "Test", 1, null, new ArrayList<>());
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(habitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HabitDTO added = habitService.toggleHabitDone(1L, today);
        assertTrue(added.getCompletedDates().contains(today));

        HabitDTO removed = habitService.toggleHabitDone(1L, today);
        assertFalse(removed.getCompletedDates().contains(today));
    }

    @Test
    void toggleHabitDone_shouldThrow_whenNotFound() {
        when(habitRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> habitService.toggleHabitDone(1L, LocalDate.now()));
    }

    @Test
    void getStats_shouldCalculateStreaksAndRate() {
        LocalDate today = LocalDate.now();
        List<LocalDate> done = List.of(today.minusDays(2), today.minusDays(1), today);
        Habit habit = new Habit(1L, "Stats", "Test", 1, LocalDate.now().minusDays(5).atStartOfDay(), done);

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));

        HabitStatsDTO stats = habitService.getStats(1L);

        assertEquals(3, stats.getTotalDone());
        assertTrue(stats.getSuccessRate() > 0);
        assertEquals(3, stats.getCurrentStreak());
        assertTrue(stats.getLongestStreak() >= 3);
    }

    @Test
    void getStats_shouldHandleEmptyDates() {
        Habit habit = new Habit(1L, "Empty", "No dates", 1,
                LocalDate.now().minusDays(5).atStartOfDay(), new ArrayList<>());
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));

        HabitStatsDTO stats = habitService.getStats(1L);

        assertEquals(0, stats.getTotalDone());
        assertEquals(0, stats.getCurrentStreak());
        assertEquals(0, stats.getLongestStreak());
        assertTrue(stats.getSuccessRate() >= 0);
    }

    @Test
    void getStats_shouldThrow_whenNotFound() {
        when(habitRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> habitService.getStats(1L));
    }
}
