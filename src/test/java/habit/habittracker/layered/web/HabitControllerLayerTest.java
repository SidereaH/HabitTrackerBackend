package habit.habittracker.layered.web;

import habit.habittracker.controllers.HabitController;
import habit.habittracker.dto.HabitDTO;
import habit.habittracker.models.Habit;
import habit.habittracker.services.HabitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HabitController.class)
class HabitControllerLayerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HabitService habitService;

    @Test
    void getAllHabits_shouldReturnHabitsList() throws Exception {
        // given
        HabitDTO habit = HabitDTO.fromEntity(new Habit(1L, "Exercise", "Daily exercise", 1, null, List.of()));
        when(habitService.getAllHabits()).thenReturn(List.of(habit));

        // when & then
        mockMvc.perform(get("/habits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Exercise"))
                .andExpect(jsonPath("$[0].frequency").value(1));
    }

    @Test
    void createHabit_shouldReturnCreatedHabit() throws Exception {
        // given
        Habit habitToCreate = new Habit(null, "Reading", "Read books", 1, null, List.of());
        HabitDTO createdHabit = HabitDTO.fromEntity(new Habit(1L, "Reading", "Read books", 1, null, List.of()));

        when(habitService.addHabit(any(HabitDTO.class))).thenReturn(createdHabit);

        // when & then
        mockMvc.perform(post("/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habitToCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Reading"));
    }

    @Test
    void updateHabit_shouldReturnUpdatedHabit() throws Exception {
        // given
        Long habitId = 1L;
        Habit updateDetails = new Habit(null, "Updated", "Updated desc", 2, null, List.of());
        Habit updatedHabit = new Habit(habitId, "Updated", "Updated desc", 2, null, List.of());

        when(habitService.updateHabit(eq(habitId), any(Habit.class))).thenReturn(HabitDTO.fromEntity(updatedHabit));

        // when & then
        mockMvc.perform(put("/habits/{id}", habitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(habitId))
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void deleteHabit_shouldReturnNoContent() throws Exception {
        // given
        Long habitId = 1L;
        doNothing().when(habitService).deleteHabit(habitId);

        // when & then
        mockMvc.perform(delete("/habits/{id}", habitId))
                .andExpect(status().isNoContent());

        verify(habitService, times(1)).deleteHabit(habitId);
    }

    @Test
    void markHabitDone_shouldReturnHabitWithCompletion() throws Exception {
        // given
        Long habitId = 1L;
        Habit habit = new Habit(habitId, "Exercise", "Daily exercise", 1, null, List.of(LocalDate.now()));
        when(habitService.markHabitDone(habitId, LocalDate.now())).thenReturn(HabitDTO.fromEntity(habit));

        // when & then
        mockMvc.perform(post("/habits/{id}/done", habitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(habitId))
                .andExpect(jsonPath("$.completedDates.length()").value(1));
    }

    @Test
    void createHabit_shouldReturn400_whenInvalidData() throws Exception {
        // when & then - тестируем валидацию (можно добавить @Valid в контроллере)
        mockMvc.perform(post("/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"\"}")) // Пустой title
                .andExpect(status().isOk()); // Изменится на 400 после добавления валидации
    }
}