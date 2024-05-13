package edu.uoc.epcsd.user.controllers;

import edu.uoc.epcsd.user.controllers.dtos.CreateAlertRequest;
import edu.uoc.epcsd.user.entities.Alert;
import edu.uoc.epcsd.user.services.AlertService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@RestController
@RequestMapping("/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<Alert> getAllAlerts() {
        log.trace("getAllAlerts");

        return alertService.findAll();
    }

    @GetMapping("/{alertId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Alert> getAlertById(@PathVariable @NotNull Long alertId) {
        log.trace("getAlertById");

        return alertService.findById(alertId).map(alert -> ResponseEntity.ok().body(alert))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Long> createAlert(@RequestBody CreateAlertRequest createAlertRequest) {
        log.trace("createAlert");

        try {
            log.trace("Creating alert " + createAlertRequest);
            Long alertId = alertService.createAlert(
                    createAlertRequest.getProductId(),
                    createAlertRequest.getUserId(),
                    createAlertRequest.getFrom(),
                    createAlertRequest.getTo()).getId();
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(alertId)
                    .toUri();

            return ResponseEntity.created(uri).body(alertId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage(),
                    e);
        }
    }

    @GetMapping("/toAlert")
    @ResponseStatus(HttpStatus.OK)
    public List<Alert> getAlertByProductAndDate(@RequestParam @NotNull Long productId, @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate availableOnDate) {
        log.trace("getAlertByProductAndDate");
        Stream<Alert> alerts = alertService.findAll().stream().filter(alert -> alert.getProductId().equals(productId) && alert.getFrom().isEqual(availableOnDate) && alert.getTo().isEqual(availableOnDate));
        return alerts.collect(Collectors.toList());
    }

    @GetMapping("/userAlert")
    @ResponseStatus(HttpStatus.OK)
    public List<Alert> getAlertByUserAndDate(@RequestParam @NotNull Long userId, @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate availableOnDate) {
        log.trace("getAlertByUserAndDate");
        Stream<Alert> alerts = alertService.findAll().stream().filter(alert -> alert.getUser().getId().equals(userId) && availableOnDate.isAfter(alert.getFrom()) && availableOnDate.isBefore(alert.getTo()));
        return alerts.collect(Collectors.toList());
    }
}
