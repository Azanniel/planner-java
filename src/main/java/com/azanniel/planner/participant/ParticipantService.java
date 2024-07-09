package com.azanniel.planner.participant;

import com.azanniel.planner.trip.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ParticipantService {

    @Autowired
    private ParticipantRepository participantRepository;

    public List<ParticipantData> getAllParticipantsFromEvent(UUID tripId) {
        return this.participantRepository
                .findByTripId(tripId)
                .stream()
                .map(participant -> new ParticipantData(participant.getId(), participant.getName(), participant.getEmail(), participant.getIsConfirmed()))
                .toList();
    }

    public void registerParticipantsToTrip(List<String> participantsToInvite, Trip trip) {
        List<Participant> participants = participantsToInvite.stream().map(email -> new Participant(email, trip)).toList();
        this.participantRepository.saveAll(participants);
    }

    public ParticipantCreateResponse registerParticipantToTrip(String email, Trip trip) {
        Participant participant = new Participant(email, trip);
        this.participantRepository.save(participant);

        return new ParticipantCreateResponse(participant.getId());
    }

    public void triggerConfirmationEmailToParticipants(UUID tripId){}

    public void triggerConfirmationEmailToParticipant(String email) {}
}
