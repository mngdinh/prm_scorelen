package com.scorelens.Service;

import com.scorelens.DTOs.Request.EventRequest;
import com.scorelens.DTOs.Response.EventResponse;
import com.scorelens.Entity.Event;
import com.scorelens.Exception.AppException;
import com.scorelens.Exception.ErrorCode;
import com.scorelens.Mapper.EventMapper;
import com.scorelens.Repository.EventRepo;
import com.scorelens.Repository.GameSetRepository;
import com.scorelens.Repository.PlayerRepo;
import com.scorelens.Service.Filter.BaseSpecificationService;
import com.scorelens.Service.KafkaService.KafkaProducer;
import com.scorelens.Service.Interface.IEventService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EventService extends BaseSpecificationService<Event, EventResponse> implements IEventService {

    EventRepo eventRepo;

    EventMapper eventMapper;

    GameSetRepository gameSetRepo;

    PlayerRepo playerRepo;

    KafkaProducer kafkaProducer;

    @Override
    protected JpaSpecificationExecutor<Event> getRepository() {
        return eventRepo;
    }

    @Override
    protected Function<Event, EventResponse> getMapper() {
        return eventMapper::toEventResponse;
    }

    @Override
    protected Specification<Event> buildSpecification(Map<String, Object> filters) {
        return (Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

//            String playerId = String.valueOf(filters.get("playerId"));
//            String gameSetId = String.valueOf(filters.get("gameSetId"));
            Integer playerId = (Integer) filters.get("playerId");
            Integer gameSetId = (Integer) filters.get("gameSetId");
            String queryType = (String) filters.get("queryType");

            if ("byGameSet".equals(queryType)) {
                predicates.add(cb.equal(root.get("gameSet").get("gameSetID"), gameSetId));
            }

            if ("byPlayer".equals(queryType)) {
                predicates.add(cb.equal(root.get("player").get("playerID"), playerId));
            }

            if ("byPlayerAndGameSet".equals(queryType)) {
                predicates.add(cb.equal(root.get("player").get("playerID"), playerId));
                predicates.add(cb.equal(root.get("gameSet").get("gameSetID"), gameSetId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public EventResponse addEvent(EventRequest eventRequest) {
        if (!gameSetRepo.existsById(eventRequest.getGameSetID()))
            throw new AppException(ErrorCode.SET_NOT_FOUND);
        if (!playerRepo.existsById(eventRequest.getPlayerID()))
            throw new AppException(ErrorCode.PLAYER_NOT_FOUND);
        Event event = eventMapper.toEventRequest(eventRequest);
        //set timestamp
        event.setTimeStamp(LocalDateTime.now());
        eventRepo.save(event);
        return eventMapper.toEventResponse(event);
    }

    @Override
    public List<EventResponse> getAllEvents() {
        List<Event> events = eventRepo.findAll();
        if (events.isEmpty())
            throw new AppException(ErrorCode.NULL_EVENT);
        return eventMapper.toEventResponses(events);
    }


    @Override
    public List<EventResponse> getEventsByPlayerID(int playerID) {
        List<Event> events = eventRepo.findAllByPlayer_PlayerID(playerID);
        if (events.isEmpty())
            throw new AppException(ErrorCode.NULL_EVENT_PLAYERID);
        return eventMapper.toEventResponses(events);
    }

    @Override
    public List<EventResponse> getEventsByGameSetID(int gamseSetID) {
        List<Event> events = eventRepo.findAllByGameSet_GameSetID(gamseSetID);
        if (events.isEmpty())
            throw new AppException(ErrorCode.NULL_EVENT_GAMESETID);
        return eventMapper.toEventResponses(events);
    }

    @Override
    public boolean deleteEventByPlayerID(int playerID) {
        List<Event> events = eventRepo.findAllByPlayer_PlayerID(playerID);
        if (events.isEmpty())
            throw new AppException(ErrorCode.NULL_EVENT_PLAYERID);
        eventRepo.deleteAll(events);
        try {
            kafkaProducer.deleteEventByPlayer(playerID);
        } catch (Exception e) {
            throw new AppException(ErrorCode.KAFKA_SEND_FAILED);
        }
        return true;
    }

    @Override
    public boolean deleteEventByGameSetID(int gameSetID) {
        List<Event> events = eventRepo.findAllByGameSet_GameSetID(gameSetID);
        if (events.isEmpty())
            throw new AppException(ErrorCode.NULL_EVENT_GAMESETID);
        eventRepo.deleteAll(events);
        try {
            kafkaProducer.deleteEventByGameSet(gameSetID);
        } catch (Exception e) {
            throw new AppException(ErrorCode.KAFKA_SEND_FAILED);
        }
        return true;
    }

    @Override
    public List<EventResponse> getEventsByPlayerIDAndGameSetID(int gameSetID, int playerID) {
        List<Event> list = eventRepo.findAllByGameSet_GameSetIDAndPlayer_PlayerID(gameSetID, playerID);
        if (list.isEmpty())
            throw new AppException(ErrorCode.EMPTY_LIST);
        return eventMapper.toEventResponses(list);
    }

    @Override
    public Event geteventByID(int eventID) {
        Event e = eventRepo.findById(eventID)
                .orElseThrow(() -> new AppException(ErrorCode.NULL_EVENT));
        return e;
    }

    @Override
    public int countEventsGameSetID(int gameSetID) {
        List<Event> events = eventRepo.findAllByGameSet_GameSetID(gameSetID);
        return events != null ? events.size() : 0;
    }
}
