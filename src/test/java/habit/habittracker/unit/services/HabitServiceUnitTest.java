package habit.habittracker.unit.services;

import habit.habittracker.dto.HabitDTO;
import habit.habittracker.models.Habit;
import habit.habittracker.repositories.HabitRepository;
import habit.habittracker.services.HabitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceUnitTest {

    @Mock
    private HabitRepository habitRepository;

    @InjectMocks
    private HabitService habitService;

    @Test
    void getAllHabits_shouldReturnAllHabits() {
        // given
        Habit habit1 = new Habit(1L, "Exercise", "Daily exercise", 1, null, List.of());
        Habit habit2 = new Habit(2L, "Reading", "Read books", 1, null, List.of());
        when(habitRepository.findAll()).thenReturn(List.of(habit1, habit2));

        // when
        List<HabitDTO> result = habitService.getAllHabits();

        // then
        assertEquals(2, result.size());
        verify(habitRepository, times(1)).findAll();
    }

    @Test
    void addHabit_shouldSaveAndReturnHabit() {
        // given
        Habit habitToSave = new Habit(null, "Meditation", "Daily meditation", 1, null, List.of());
        Habit savedHabit = new Habit(1L, "Meditation", "Daily meditation", 1, null, List.of());
        when(habitRepository.save(habitToSave)).thenReturn(savedHabit);

        // when
        HabitDTO result = habitService.addHabit(HabitDTO.fromEntity(habitToSave));

        // then
        assertNotNull(result.getId());
        assertEquals("Meditation", result.getTitle());
        verify(habitRepository, times(1)).save(habitToSave);
    }

    @Test
    void updateHabit_shouldUpdateExistingHabit() {
        // given
        Long habitId = 1L;
        Habit existingHabit = new Habit(habitId, "Old Title", "Old Desc", 1, null, List.of());
        Habit updateDetails = new Habit(null, "New Title", "New Desc", 2, null, List.of());

        when(habitRepository.findById(habitId)).thenReturn(Optional.of(existingHabit));
        when(habitRepository.save(any(Habit.class))).thenReturn(existingHabit);

        // when
        HabitDTO result = habitService.updateHabit(habitId, updateDetails);

        // then
        assertEquals("New Title", result.getTitle());
        assertEquals("New Desc", result.getDescription());
        assertEquals(2, result.getFrequency());
        verify(habitRepository, times(1)).findById(habitId);
        verify(habitRepository, times(1)).save(existingHabit);
    }

    @Test
    void updateHabit_shouldThrowException_whenHabitNotFound() {
        // given
        Long habitId = 999L;
        when(habitRepository.findById(habitId)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> habitService.updateHabit(habitId, new Habit()));

        assertEquals("Not found", exception.getMessage());
        verify(habitRepository, never()).save(any());
    }

    @Test
    void deleteHabit_shouldDeleteExistingHabit() {
        // given
        Long habitId = 1L;
        when(habitRepository.existsById(habitId)).thenReturn(true);

        // when
        habitService.deleteHabit(habitId);

        // then
        verify(habitRepository, times(1)).existsById(habitId);
        verify(habitRepository, times(1)).deleteById(habitId);
    }

    @Test
    void deleteHabit_shouldThrowException_whenHabitNotFound() {
        // given
        Long habitId = 999L;
        when(habitRepository.existsById(habitId)).thenReturn(false);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> habitService.deleteHabit(habitId));

        assertEquals("Not found", exception.getMessage());
        verify(habitRepository, never()).deleteById(anyLong());
    }

    @Test
    void markHabitDone_shouldAddCompletionDate() {
        // given
        Long habitId = 1L;
        Habit existingHabit = new Habit(habitId, "Exercise", "Daily exercise", 1, null, new ArrayList<>());
        when(habitRepository.findById(habitId)).thenReturn(Optional.of(existingHabit));
        when(habitRepository.save(any(Habit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        HabitDTO result = habitService.markHabitDone(habitId, LocalDate.now());

        // then
        assertTrue(result.getCompletedDates().contains(LocalDate.now()));
        assertEquals(1, result.getCompletedDates().size());
        verify(habitRepository, times(1)).findById(habitId);
        verify(habitRepository, times(1)).save(existingHabit);
    }

    @Test
    void markHabitDone_shouldThrowException_whenHabitNotFound() {
        // given
        Long habitId = 999L;
        when(habitRepository.findById(habitId)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> habitService.markHabitDone(habitId, LocalDate.now()));

        assertEquals("Not found", exception.getMessage());
        verify(habitRepository, never()).save(any());
    }
}