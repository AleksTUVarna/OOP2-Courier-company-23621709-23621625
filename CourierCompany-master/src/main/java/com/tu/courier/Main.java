package com.tu.courier;

import com.tu.courier.dao.UserDAO;
import com.tu.courier.entity.Role;
import com.tu.courier.entity.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Main {
    public static void main(String[] args) {
        // 1. Инициализация (Правим го само веднъж)
        SessionFactory sessionFactory = new Configuration()
                .configure()
                .buildSessionFactory();

        // 2. Създаваме нашия DAO инструмент
        UserDAO userDAO = new UserDAO(sessionFactory);

        System.out.println("--- ТЕСТ 1: Търсене на Админ ---");
        User admin = userDAO.findByUsername("admin");
        if (admin != null) {
            System.out.println("✅ Успешен вход за: " + admin.getFullName());
        } else {
            System.out.println("❌ Админът липсва!");
        }

        System.out.println("\n--- ТЕСТ 2: Създаване на нов куриер ---");
        // Проверяваме дали вече го има, за да не гърми при повторен пуск
        if (userDAO.findByUsername("ivan_courier") == null) {
            User courier = new User();
            courier.setUsername("ivan_courier");
            courier.setPassword("pass123");
            courier.setFullName("Ivan Ivanov");
            courier.setRole(Role.COURIER);

            userDAO.saveUser(courier);
        } else {
            System.out.println("ℹ️ Куриерът Ivan вече съществува.");
        }

        sessionFactory.close();
    }
}