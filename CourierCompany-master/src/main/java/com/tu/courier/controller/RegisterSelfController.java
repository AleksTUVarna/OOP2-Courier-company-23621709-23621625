package com.tu.courier.controller;

import com.tu.courier.CourierApp;
import com.tu.courier.dao.UserDAO;
import com.tu.courier.entity.Role;
import com.tu.courier.entity.User;
import com.tu.courier.util.HibernateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterSelfController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passField;
    @FXML private PasswordField confirmPassField;
    @FXML private Label errorLabel;

    @FXML
    public void onRegister() {
        String name = fullNameField.getText().strip();
        String username = usernameField.getText().strip();
        String email = emailField.getText().strip();
        String phone = phoneField.getText().strip();
        String pass = passField.getText().strip();
        String confirmPass = confirmPassField.getText().strip();

        // 1. Проверка за празни полета
        if (name.isBlank() || username.isBlank() || email.isBlank() || phone.isBlank() || pass.isBlank()) {
            errorLabel.setText("Моля, попълнете всички полета.");
            return;
        }

        // 2. Проверка за парола (мин 8 знака)
        if (pass.length() < 8) {
            errorLabel.setText("Паролата трябва да е поне 8 символа.");
            return;
        }

        // 3. Проверка дали паролите съвпадат
        if (!pass.equals(confirmPass)) {
            errorLabel.setText("Паролите не съвпадат.");
            return;
        }

        UserDAO userDAO = new UserDAO(HibernateUtil.getSessionFactory());

        // 4. Проверка дали потребителят вече съществува
        if (userDAO.findByUsername(username) != null) {
            errorLabel.setText("Потребителското име е заето.");
            return;
        }

        // 5. Проверка за уникален телефон (Валидация)
        if (userDAO.isPhoneTaken(phone)) {
            errorLabel.setText("Вече съществува профил с този телефонен номер.");
            return;
        }

        // ВСИЧКО Е ТОЧНО -> ЗАПИСВАМЕ
        User newUser = new User();
        newUser.setFullName(name);
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setPassword(pass);
        newUser.setRole(Role.CLIENT); // Саморегистриралите се са винаги КЛИЕНТИ

        try {
            userDAO.saveUser(newUser);

            // Успех! Връщаме се към Login
            errorLabel.setStyle("-fx-text-fill: green;");
            errorLabel.setText("Успешна регистрация! Върнете се към вход.");
            // Може автоматично да го прехвърлиш след 1-2 сек, но засега го оставяме така

        } catch (Exception e) {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Грешка при запис в базата.");
            e.printStackTrace();
        }
    }

    @FXML
    public void onBack() {
        try {
            // Връщаме Login екрана
            new CourierApp().start((Stage) fullNameField.getScene().getWindow());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}