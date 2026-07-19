package za.ac.mycput.controller;

import za.ac.mycput.domain.Manager;

public class ManagerController {

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.bind.annotation .*;
    import za.ac.mycput.domain.Manager;
    import za.ac.mycput.service.IManagerService;

      import java.util.Set;

    @RestController
    @RequestMapping("/manager")
    public class ManagerController {

        private final IManagerService service;

        @Autowired
        public ManagerController(IManagerService service) {
            this.service = service;
        }

        @PostMapping("/create")
        public Manager create(@RequestBody Manager manager) {
            return service.create(manager);
        }

        @GetMapping("/read/{managerId}")
        public Manager read(@PathVariable String managerId) {
            return service.read(managerId);
        }

        @PutMapping("/update")
        public Manager update(@RequestBody Manager manager) {
            return service.update(manager);
        }

        @DeleteMapping("/delete/{managerId}")
        public boolean delete(@PathVariable String managerId) {
            return service.delete(managerId);
        }

        @GetMapping("/getAll")
        public Set<Manager> getAll() {
            return service.getAll();
        }
    }

}
