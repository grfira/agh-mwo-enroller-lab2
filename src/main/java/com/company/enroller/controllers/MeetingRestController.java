package com.company.enroller.controllers;

import com.company.enroller.model.Meeting;
import com.company.enroller.model.Participant;
import com.company.enroller.persistence.MeetingService;
import com.company.enroller.persistence.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/meetings")
public class MeetingRestController {

    @Autowired
    MeetingService meetingService;
    @Autowired
    ParticipantService participantService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getMeetings() {
        Collection<Meeting> meetings = meetingService.getAll();
        return new ResponseEntity<>(meetings, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getMeeting(@PathVariable("id") Long id) {
        Meeting meeting = meetingService.findById(id);
        if (meeting == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);

    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> addMeeting(@RequestBody Meeting meeting) {
        Meeting foundMeeting = meetingService.findById(meeting.getId());
        if (foundMeeting != null) {
            return new ResponseEntity<>("Unable to create. A meeting with id " + meeting.getId() + " already exist.", HttpStatus.CONFLICT);
        }
        meetingService.addMeeting(meeting);
        return new ResponseEntity<>(meeting, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateMeeting(@PathVariable("id") Long id, @RequestBody Meeting meeting) {
        if (meetingService.findById(id) == null) {
            return new ResponseEntity<>("Unable to update. A meeting with id " + id + " does not exist.", HttpStatus.CONFLICT);
        }
        meeting.setId(id);
        meetingService.updateMeeting(meeting);
        return new ResponseEntity<>(meeting, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteMeeting(@PathVariable("id") Long id) {
        Meeting meeting = meetingService.findById(id);
        if (meeting == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        meetingService.deleteMeeting(meeting);
        return new ResponseEntity<>(meeting, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/participants", method = RequestMethod.GET)
    public ResponseEntity<?> getMeetingParticipants(@PathVariable("id") Long id) {
        Meeting meeting = meetingService.findById(id);
        if (meeting == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Collection<Participant> participants = meeting.getParticipants();
        return new ResponseEntity<Collection<Participant>>(participants, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/participants/{login}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteMeetingParticipant(@PathVariable("id") Long id, @PathVariable("login") String login) {
        Meeting meeting = meetingService.findById(id);
        if (meeting == null)
            return new ResponseEntity<>("A meeting with id " + id + " not found", HttpStatus.NOT_FOUND);
        Set<Participant> participants = meeting.getParticipants().stream().filter(x -> x.getLogin().equals(login)).collect(Collectors.toSet());
        if (participants.isEmpty())
            return new ResponseEntity<>("A participant with login " + login + " not found", HttpStatus.NOT_FOUND);
        else meeting.removeParticipant(participants.iterator().next());
        return new ResponseEntity<>("A participant with login " + login + " was deleted", HttpStatus.OK);

    }


    @RequestMapping(value = "/{id}/participants", method = RequestMethod.POST)
    public ResponseEntity<?> addMeetingParticipant(@PathVariable("id") Long id, @RequestBody Participant participant) {
        Meeting meeting = meetingService.findById(id);
        if (meeting == null)
            return new ResponseEntity<>("A meeting with id " + id + " does not exist.", HttpStatus.NOT_FOUND);
        if (participantService.findByLogin(participant.getLogin()) == null) {
            return new ResponseEntity<String>("Unable to add. A participant with login "
                    + participant.getLogin() + " does not exist.", HttpStatus.NOT_FOUND);
        }
        if (meeting.getParticipants().stream().anyMatch(x -> x.getLogin().equals(participant.getLogin())))
            return new ResponseEntity<>("User with id " + participant.getLogin()
                    + " has already been added to meeting", HttpStatus.CONFLICT);
        meeting.addParticipant(participant);

        return new ResponseEntity<>(meeting.getParticipants(), HttpStatus.CREATED);
    }
}
