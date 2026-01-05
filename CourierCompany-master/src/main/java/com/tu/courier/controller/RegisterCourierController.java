package com.tu.courier.controller;

import com.tu.courier.dao.UserDAO;
import com.tu.courier.entity.Role;
import com.tu.courier.entity.User;
import com.tu.courier.util.HibernateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterCourierController {

    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    public void onRegisterClick() {
        String name = nameField.getText().strip();
        String username = usernameField.getText().strip();
        String password = passwordField.getText().strip();

        // 1. Валидация
        if (name.isBlank() || username.isBlank() || password.isBlank()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Попълнете всички полета!");
            return;
        }

        // 2. Създаване на обекта User
        User newCourier = new User();
        newCourier.setFullName(name);
        newCourier.setUsername(username);
        newCourier.setPassword(password);
        newCourier.setRole(Role.COURIER); // ВАЖНО: Задаваме роля КУРИЕР

        // 3. Запис в базата
        UserDAO userDAO = new UserDAO(HibernateUtil.getSessionFactory());

        // Проверка дали вече не съществува такъв потребител
        if (userDAO.findByUsername(username) != null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Това потребителско име е заето!");
            return;
        }

        try {
            userDAO.saveUser(newCourier);

            // Успех - затваряме прозореца
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("Успешно записан!");

            // Малко забавяне или затваряне веднага (тук го затваряме веднага)
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.close();
            System.out.println("Нов куриер създаден: " + username);

        } catch (Exception e) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Регистрацията не бе успешна!");
            e.printStackTrace();
        }
    }
}