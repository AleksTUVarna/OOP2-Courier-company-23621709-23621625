package com.tu.courier.dao;

import com.tu.courier.entity.Notification;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class NotificationDAO {

    private final SessionFactory sessionFactory;

    public NotificationDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(Notification notification) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(notification);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void update(Notification notification) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.merge(notification);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /** ADMIN: всички известия */
    public List<Notification> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "SELECT n FROM Notification n ORDER BY n.createdAt DESC",
                    Notification.class
            ).getResultList();
        }
    }

    /** CLIENT/COURIER: само неговите */
    public List<Notification> findForUser(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT n FROM Notification n " +
                                    "WHERE n.user IS NOT NULL AND n.user.id = :uid " +
                                    "ORDER BY n.createdAt DESC",
                            Notification.class
                    ).setParameter("uid", userId)
                    .getResultList();
        }
    }

    /** Ако някой ден решиш да показваш и глобални (user NULL) + лични */
    public List<Notification> findForUserIncludingGlobal(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT n FROM Notification n " +
                                    "WHERE (n.user IS NULL) OR (n.user IS NOT NULL AND n.user.id = :uid) " +
                                    "ORDER BY n.createdAt DESC",
                            Notification.class
                    ).setParameter("uid", userId)
                    .getResultList();
        }
    }
}
