package com.tu.courier.dao;

import com.tu.courier.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class UserDAO {

    private final SessionFactory sessionFactory;

    public UserDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // 1. Метод за запис на нов потребител (Регистрация)
    public void saveUser(User user) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.persist(user); // Записва в базата
            transaction.commit();
            System.out.println("✅ Потребителят " + user.getUsername() + " е записан успешно!");
        } catch (Exception e) {
            if (transaction != null) transaction.rollback(); // Връща назад, ако има грешка
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // 2. Метод за намиране по потребителско име (За Логин)
    public User findByUsername(String username) {
        Session session = sessionFactory.openSession();
        try {
            // HQL заявка (Hibernate Query Language)
            Query<User> query = session.createQuery("FROM User WHERE username = :username", User.class);
            query.setParameter("username", username);

            return query.uniqueResult(); // Връща User или null, ако не е намерен
        } finally {
            session.close();
        }
    }

    // 3. Метод за взимане на всички клиенти (за падащото меню при пратка)
    public java.util.List<User> getAllClients() {
        Session session = sessionFactory.openSession();
        try {
            // Търсим само тези, които са с роля CLIENT
            return session.createQuery("FROM User WHERE role = 'CLIENT'", User.class).list();
        } finally {
            session.close();
        }
    }

    // Проверка дали телефонът вече е зает
    public boolean isPhoneTaken(String phone) {
        Session session = sessionFactory.openSession();
        try {
            Query<Long> query = session.createQuery("SELECT count(u) FROM User u WHERE u.phone = :phone", Long.class);
            query.setParameter("phone", phone);
            return query.uniqueResult() > 0;
        } finally {
            session.close();
        }
    }

    // Взима всички куриери (заедно с офисите им)
    public java.util.List<User> getAllCouriers() {
        Session session = sessionFactory.openSession();
        try {
            return session.createQuery("FROM User WHERE role = 'COURIER'", User.class).list();
        } finally {
            session.close();
        }
    }

    // Обновяване на данни (Edit)
    public void updateUser(User user) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Изтриване
    public void deleteUser(User user) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.remove(user); // Изтрива записа
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e; // Хвърляме грешката нагоре, за да уведомим потребителя (напр. ако има пратки)
        } finally {
            session.close();
        }
    }

    public User findByPhone(String phone) {
        Session session = sessionFactory.openSession();
        try {
            Query<User> query = session.createQuery("FROM User WHERE phone = :phone", User.class);
            query.setParameter("phone", phone);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }
}