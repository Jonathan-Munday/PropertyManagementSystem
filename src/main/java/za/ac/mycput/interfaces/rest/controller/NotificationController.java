package za.ac.mycput.interfaces.rest.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.ac.mycput.application.service.DirectoryApplicationService;
import za.ac.mycput.interfaces.rest.dto.NotificationRequest;
import za.ac.mycput.interfaces.rest.dto.NotificationResponse;
import za.ac.mycput.interfaces.rest.mapper.NotificationRestMapper;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final DirectoryApplicationService directory;
    private final NotificationRestMapper mapper;

    public NotificationController(DirectoryApplicationService directory, NotificationRestMapper mapper) {
        this.directory = directory;
        this.mapper = mapper;
    }

    @GetMapping
    public List<NotificationResponse> list() {
        return directory.listNotifications().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{notificationId}")
    public NotificationResponse get(@PathVariable String notificationId) {
        return mapper.toResponse(directory.getNotification(notificationId));
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(directory.createNotification(mapper.toCommand(request))));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> delete(@PathVariable String notificationId) {
        directory.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
