package habit.habittracker.integration.api;

import habit.habittracker.dto.HabitDTO;
import habit.habittracker.models.Habit;
import habit.habittracker.repositories.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HabitApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private HabitRepository habitRepository;

    @BeforeEach
    void setUp() {
        habitRepository.deleteAll();
    }

    @Test
    void createHabit_throughFullStack() {
        // given
        Habit habit = new Habit();
        habit.setTitle("Integration Test Habit");
        habit.setDescription("Test Description");
        habit.setFrequency(1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Habit> request = new HttpEntity<>(habit, headers);

        // when
        ResponseEntity<Habit> response = restTemplate.postForEntity(
                "/habits", request, Habit.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Integration Test Habit", response.getBody().getTitle());

        // Verify in database
        List<Habit> allHabits = habitRepository.findAll();
        assertEquals(1, allHabits.size());
        assertEquals("Integration Test Habit", allHabits.get(0).getTitle());
    }

    @Test
    void getAllHabits_throughFullStack() {
        // given
        Habit habit1 = new Habit();
        habit1.setTitle("Habit 1");
        Habit habit2 = new Habit();
        habit2.setTitle("Habit 2");
        habitRepository.saveAll(List.of(habit1, habit2));

        // when
        ResponseEntity<Habit[]> response = restTemplate.getForEntity("/habits", Habit[].class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }

    @Test
    void updateHabit_throughFullStack() {
        // given
        Habit habit = new Habit();
        habit.setTitle("Original Title");
        Habit saved = habitRepository.save(habit);

        Habit updateDetails = new Habit();
        updateDetails.setTitle("Updated Title");
        updateDetails.setDescription("Updated Description");
        updateDetails.setFrequency(2);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Habit> request = new HttpEntity<>(updateDetails, headers);

        // when
        ResponseEntity<Habit> response = restTemplate.exchange(
                "/habits/" + saved.getId(), HttpMethod.PUT, request, Habit.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Title", response.getBody().getTitle());

        // Verify update in database
        Habit updated = habitRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated Description", updated.getDescription());
        assertEquals(2, updated.getFrequency());
    }


    @Test
    void deleteHabit_throughFullStack() {
        // given
        Habit habit = new Habit();
        habit.setTitle("To Delete");
        Habit saved = habitRepository.save(habit);

        // when
        ResponseEntity<Void> response = restTemplate.exchange(
                "/habits/" + saved.getId(), HttpMethod.DELETE, null, Void.class);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify deletion from database
        assertFalse(habitRepository.existsById(saved.getId()));
    }
}