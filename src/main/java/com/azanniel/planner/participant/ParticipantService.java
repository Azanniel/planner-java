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

    public void registerParticipantToTrip(List<String> participantsToInvite, Trip trip) {
        List<Participant> participants = participantsToInvite.stream().map(email -> new Participant(email, trip)).toList();
        participantRepository.saveAll(participants);

        System.out.println(participants.get(0).getId());
    }

    public void triggerConfirmationEmailToParticipants(UUID tripId){}
}
