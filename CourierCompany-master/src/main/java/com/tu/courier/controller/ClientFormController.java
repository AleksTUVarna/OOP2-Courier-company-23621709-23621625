package com.tu.courier.controller;

import com.tu.courier.dao.UserDAO;
import com.tu.courier.entity.Role;
import com.tu.courier.entity.User;
import com.tu.courier.util.HibernateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ClientFormController {

    @FXML private Text titleText;
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private User userToEdit;

    public void setEditMode(User user) {
        this.userToEdit = user;
        titleText.setText("Редактиране на Клиент");

        nameField.setText(user.getFullName());
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        passwordField.setPromptText("Въведи само ако искаш нова парола");
    }

    @FXML
    public void onSave() {
        String name = nameField.getText().strip();
        String username = usernameField.getText().strip();
        String email = emailField.getText().strip();
        String phone = phoneField.getText().strip();
        String password = passwordField.getText().strip();


        if (name.isBlank() || username.isBlank()) {
            statusLabel.setText("Името и User-a са задължителни!");
            return;
        }

        UserDAO userDAO = new UserDAO(HibernateUtil.getSessionFactory());

        try {
            if (userToEdit == null) {
                // --- НОВ КЛИЕНТ ---
                if (password.isBlank()) {
                    statusLabel.setText("Паролата е задължителна за нов!");
                    return;
                }
                if (userDAO.findByUsername(username) != null) {
                    statusLabel.setText("Този username е зает!");
                    return;
                }

                // Валидация за телефон
                if (!phone.isBlank() && userDAO.isPhoneTaken(phone)) {
                    statusLabel.setText("Телефонът вече е регистриран!");
                    return;
                }

                User newUser = new User();
                newUser.setFullName(name);
                newUser.setUsername(username);
                newUser.setEmail(email);
                newUser.setPhone(phone);
                newUser.setPassword(password);
                newUser.setRole(Role.CLIENT); // ВАЖНО: Роля CLIENT

                userDAO.saveUser(newUser);

            } else {
                // --- РЕДАКЦИЯ ---
                // Проверка ако се сменя телефона, дали не е зает от друг
                if (!phone.equals(userToEdit.getPhone()) && userDAO.isPhoneTaken(phone)) {
                    statusLabel.setText("Новият телефон вече е зает!");
                    return;
                }

                userToEdit.setFullName(name);
                userToEdit.setUsername(username);
                userToEdit.setEmail(email);
                userToEdit.setPhone(phone);

                if (!password.isEmpty()) {
                    userToEdit.setPassword(password);
                }

                userDAO.updateUser(userToEdit);
            }

            // Затваряме
            ((Stage) nameField.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Грешка при запис!");
        }
    }
}