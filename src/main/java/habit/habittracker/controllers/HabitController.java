package habit.habittracker.controllers;

import habit.habittracker.dto.HabitDTO;
import habit.habittracker.models.Habit;
import habit.habittracker.services.HabitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/habits")
public class HabitController {
    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }


    @GetMapping
    public List<HabitDTO> getAllHabits() {
        return habitService.getAllHabits();
    }

    @PostMapping
    public HabitDTO createHabit(@RequestBody Habit habit) {
        return habitService.addHabit(habit);
    }

    @PutMapping("/{id}")
    public HabitDTO updateHabit(@PathVariable Long id, @RequestBody Habit habit) {
        return habitService.updateHabit(id, habit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/done")
    public HabitDTO markHabitDone(@PathVariable Long id) {
        return habitService.markHabitDone(id);
    }
}
