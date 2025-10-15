package com.scorelens.Service;

import com.scorelens.DTOs.Request.NotificationRequest;
import com.scorelens.DTOs.Response.NotificationResponse;
import com.scorelens.Entity.Notification;
import com.scorelens.Exception.AppException;
import com.scorelens.Exception.ErrorCode;
import com.scorelens.Mapper.NotificationMapper;
import com.scorelens.Repository.BilliardMatchRepository;
import com.scorelens.Repository.NotificationRepo;
import com.scorelens.Service.Filter.BaseSpecificationService;
import com.scorelens.Service.Interface.INotificationService;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationService extends BaseSpecificationService<Notification, NotificationResponse> implements INotificationService {

    NotificationMapper notificationMapper;

    NotificationRepo notificationRepo;

    BilliardMatchRepository billiardMatchRepo;

    SimpMessagingTemplate messagingTemplate;

    @Override
    protected JpaSpecificationExecutor<Notification> getRepository() {
        return notificationRepo;
    }

    @Override
    protected Function<Notification, NotificationResponse> getMapper() {
        return notificationMapper::toNotiResponse;
    }

    @Override
    protected Specification<Notification> buildSpecification(Map<String, Object> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String queryType = (String) filters.get("queryType");
            Integer notificationId = (Integer) filters.get("notificationId");
            Integer matchId = (Integer) filters.get("matchId");

            if ("byId".equals(queryType)) predicates.add(cb.equal(root.get("notificationID"), notificationId));
            if ("byMatch".equals(queryType)) predicates.add(cb.equal(root.get("billiardMatch").get("billiardMatchID"), matchId));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public NotificationResponse saveNotification(NotificationRequest notificationRequest) {
        if (!billiardMatchRepo.existsById(notificationRequest.getBilliardMatchID()))
            throw new AppException(ErrorCode.MATCH_NOT_FOUND);
        Notification noti = notificationMapper.toNotiRequest(notificationRequest);
        noti.setCreateAt(LocalDateTime.now());
        notificationRepo.save(noti);
        return notificationMapper.toNotiResponse(noti);
    }

    @Override
    public List<NotificationResponse> getNotificationsByMatch(int billiardMatchID) {
        List<Notification> list = notificationRepo.findAllByBilliardMatch_BilliardMatchID(billiardMatchID);
        if (list.isEmpty()) throw new AppException(ErrorCode.EMPTY_LIST);
        return notificationMapper.toNotiResponseList(list);
    }

    @Override
    public boolean deleteNotificationByMatchID(int billiardMatchID) {
        List<Notification> list = notificationRepo.findAllByBilliardMatch_BilliardMatchID(billiardMatchID);
        if (list.isEmpty()) throw new AppException(ErrorCode.EMPTY_LIST);
        notificationRepo.deleteAll(list);
        return true;
    }

    @Override
    public void sendToWebSocket(String destination, Object message) {
        messagingTemplate.convertAndSend(destination, message);
    }


}
