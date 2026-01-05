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

public class RegisterClientController {

    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    public void onRegisterClick() {
        String name = nameField.getText().strip();
        String username = usernameField.getText().strip();
        String password = passwordField.getText().strip();

        if (name.isBlank() || username.isBlank() || password.isBlank()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Попълнете всички полета!");
            return;
        }

        User newClient = new User();
        newClient.setFullName(name);
        newClient.setUsername(username);
        newClient.setPassword(password);
        newClient.setRole(Role.CLIENT); // <--- ВАЖНО: ТУК Е РАЗЛИКАТА (CLIENT)

        UserDAO userDAO = new UserDAO(HibernateUtil.getSessionFactory());

        if (userDAO.findByUsername(username) != null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Заето потребителско име!");
            return;
        }

        try {
            userDAO.saveUser(newClient);
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("Клиентът е регистриран!");

            // Затваряме прозореца
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Регистрацията не бе успешна!");
            e.printStackTrace();
        }
    }
}