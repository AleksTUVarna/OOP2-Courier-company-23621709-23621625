package com.tu.courier.dao;

import com.tu.courier.entity.Shipment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class ShipmentDAO {
    private final SessionFactory sessionFactory;

    public ShipmentDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void createShipment(Shipment shipment) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.persist(shipment);
            transaction.commit();
            System.out.println("✅ Пратката е създадена успешно!");
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Взима всички пратки, които НЕ са доставени (за да ги види куриерът)
    public java.util.List<Shipment> getActiveShipments() {
        Session session = sessionFactory.openSession();
        try {
            String hql = """
                SELECT DISTINCT s
                FROM Shipment s
                LEFT JOIN FETCH s.toOffice
                LEFT JOIN FETCH s.sender
                LEFT JOIN FETCH s.receiver
                WHERE s.status <> 'DELIVERED'
                ORDER BY s.shipmentDate DESC
                """;
            return session.createQuery(hql, Shipment.class).list();
        } finally {
            session.close();
        }
    }


    // Метод за обновяване на статус (когато куриерът я достави)
    public void updateShipment(Shipment shipment) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.merge(shipment); // merge обновява съществуващ запис
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // 1. Взима ВСИЧКИ пратки (за Админа)
    public java.util.List<Shipment> getAllShipments() {
        Session session = sessionFactory.openSession();
        try {
            String hql = """
                SELECT DISTINCT s
                FROM Shipment s
                LEFT JOIN FETCH s.toOffice
                LEFT JOIN FETCH s.sender
                LEFT JOIN FETCH s.receiver
                ORDER BY s.shipmentDate DESC
                """;
            return session.createQuery(hql, Shipment.class).list();
        } finally {
            session.close();
        }
    }


    // 2. Изтриване на пратка (ако е грешна)
    public void deleteShipment(Shipment shipment) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.remove(shipment);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Търсене по Tracking ID
    public Shipment findByTrackingId(String trackingId) {
        Session session = sessionFactory.openSession();
        try {
            // Внимавай: trackingId е името на полето в Java класа
            return session.createQuery("FROM Shipment WHERE trackingId = :tid", Shipment.class)
                    .setParameter("tid", trackingId)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    // Търси пратки, където потребителят е ИЛИ подател, ИЛИ получател
    public java.util.List<Shipment> getShipmentsByUser(com.tu.courier.entity.User user) {
        Session session = sessionFactory.openSession();
        try {
            String hql = """
                SELECT DISTINCT s
                FROM Shipment s
                LEFT JOIN FETCH s.toOffice
                LEFT JOIN FETCH s.sender
                LEFT JOIN FETCH s.receiver
                WHERE s.sender = :user OR s.receiver = :user
                ORDER BY s.shipmentDate DESC
                """;
            return session.createQuery(hql, Shipment.class)
                    .setParameter("user", user)
                    .list();
        } finally {
            session.close();
        }
    }

}