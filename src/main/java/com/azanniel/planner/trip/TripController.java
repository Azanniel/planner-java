package com.azanniel.planner.trip;

import com.azanniel.planner.participant.ParticipantCreateResponse;
import com.azanniel.planner.participant.ParticipantData;
import com.azanniel.planner.participant.ParticipantRequestPayload;
import com.azanniel.planner.participant.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private TripRepository tripRepository;

    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload, UriComponentsBuilder uriComponentsBuilder) {
        Trip newTrip = new Trip(payload);

        this.tripRepository.save(newTrip);
        this.participantService.registerParticipantsToTrip(payload.emails_to_invite(), newTrip);

        var uri = uriComponentsBuilder.path("/trips/{id}").buildAndExpand(newTrip.getId()).toUri();

        return ResponseEntity.created(uri).body(new TripCreateResponse(newTrip.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID id) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if(trip.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Trip rawTrip = trip.get();
        List<ParticipantData> participants = this.participantService.getAllParticipantsFromEvent(rawTrip.getId());

        return ResponseEntity.ok(participants);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if(trip.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Trip rawTrip = trip.get();

        rawTrip.setDestination(payload.destination());
        rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
        rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));

        this.tripRepository.save(rawTrip);

        return ResponseEntity.ok(rawTrip);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if(trip.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Trip rawTrip = trip.get();
        rawTrip.setIsConfirmed(true);

        this.tripRepository.save(rawTrip);
        this.participantService.triggerConfirmationEmailToParticipants(rawTrip.getId());

        return ResponseEntity.ok(rawTrip);
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id, @RequestBody ParticipantRequestPayload payload) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if(trip.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Trip rawTrip = trip.get();

        ParticipantCreateResponse participantCreateResponse = this.participantService.registerParticipantToTrip(payload.email(), rawTrip);

        if(rawTrip.getIsConfirmed()) {
            this.participantService.triggerConfirmationEmailToParticipant(payload.email());
        }

        return ResponseEntity.ok(participantCreateResponse);
    }
}
