package com.tu.courier.service;

import com.tu.courier.dao.NotificationDAO;
import com.tu.courier.entity.Notification;
import com.tu.courier.entity.Shipment;
import com.tu.courier.entity.User;
import com.tu.courier.util.HibernateUtil;

public class NotificationService {

    private final NotificationDAO notificationDAO =
            new NotificationDAO(HibernateUtil.getSessionFactory());

    public void notifyUser(User user, Shipment shipment, String message) {
        notificationDAO.save(new Notification(user, shipment, message));
    }

    public void notifyGlobal(Shipment shipment, String message) {
        notificationDAO.save(new Notification(null, shipment, message));
    }
}
