package habit.habittracker.controllers;

import habit.habittracker.dto.HabitDTO;
import habit.habittracker.dto.HabitStatsDTO;
import habit.habittracker.models.Habit;
import habit.habittracker.services.HabitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/habits")
public class HabitController {
    private final HabitService habitService;
    public HabitController(HabitService habitService) { this.habitService = habitService; }

    @GetMapping
    public List<HabitDTO> getAllHabits() { return habitService.getAllHabits(); }

    @PostMapping
    public HabitDTO createHabit(@RequestBody HabitDTO habit) { return habitService.addHabit(habit); }

    @PutMapping("/{id}")
    public HabitDTO updateHabit(@PathVariable Long id, @RequestBody Habit habit) { return habitService.updateHabit(id, habit); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long id) { habitService.deleteHabit(id); return ResponseEntity.noContent().build(); }

    @PostMapping("/{id}/done")
    public HabitDTO markDone(@PathVariable Long id, @RequestParam(required = false) String date) {
        LocalDate d = (date == null) ? LocalDate.now() : LocalDate.parse(date);
        return habitService.markHabitDone(id, d);
    }

    @PostMapping("/{id}/toggle")
    public HabitDTO toggleDone(@PathVariable Long id, @RequestParam String date) {
        LocalDate d = LocalDate.parse(date);
        return habitService.toggleHabitDone(id, d);
    }

    @GetMapping("/{id}/stats")
    public HabitStatsDTO stats(@PathVariable Long id) { return habitService.getStats(id); }
}
