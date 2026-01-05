package com.tu.courier.controller;

import com.tu.courier.CourierApp;
import com.tu.courier.dao.UserDAO;
import com.tu.courier.entity.User;
import com.tu.courier.util.HibernateUtil;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private VBox loadingOverlay; // Връзка към слоя за зареждане

    @FXML
    protected void onLoginButtonClick() {
        String username = usernameField.getText().strip();
        String password = passwordField.getText().strip();

        // 1. Валидация за празни полета
        if (username.isBlank() || password.isBlank()) {
            errorLabel.setText("Моля, попълнете всички полета.");
            return;
        }

        // 2. Показваме индикатора за зареждане
        loadingOverlay.setVisible(true);
        errorLabel.setText(""); // Чистим стари грешки

        // 3. Създаваме задача (Task), която да върви във фон
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                // Симулираме леко забавяне, за да се види ефекта (можеш да го махнеш после)
                Thread.sleep(800);

                UserDAO userDAO = new UserDAO(HibernateUtil.getSessionFactory());
                return userDAO.findByUsername(username);
            }
        };

        // 4. Какво става, когато задачата приключи успешно?
        loginTask.setOnSucceeded(event -> {
            loadingOverlay.setVisible(false); // Скриваме индикатора
            User user = loginTask.getValue(); // Взимаме резултата

            if (user != null && user.getPassword().equals(password)) {
                openHomeScreen(user);
            } else {
                errorLabel.setText("Грешно потребителско име или парола.");
            }
        });

        // 5. Какво става, ако има грешка (напр. няма връзка с базата)?
        loginTask.setOnFailed(event -> {
            loadingOverlay.setVisible(false);
            errorLabel.setText("Грешка при връзка със сървъра!");
            loginTask.getException().printStackTrace();
        });

        // 6. Стартираме задачата в нова нишка
        new Thread(loginTask).start();
    }

    private void openHomeScreen(User user) {
        try {
            FXMLLoader loader;
            Parent root;

            // --- РАЗПРЕДЕЛЕНИЕ ПО РОЛИ ---
            switch (user.getRole()) {
                case ADMIN:
                    loader = new FXMLLoader(getClass().getResource("/com/tu/courier/admin_dashboard.fxml"));
                    root = loader.load();
                    ((AdminDashboardController) loader.getController()).initData(user);
                    break;

                case COURIER:
                    loader = new FXMLLoader(getClass().getResource("/com/tu/courier/courier_dashboard.fxml"));
                    root = loader.load();
                    ((CourierDashboardController) loader.getController()).initData(user);
                    break;

                case CLIENT:
                    loader = new FXMLLoader(getClass().getResource("/com/tu/courier/client_dashboard.fxml"));
                    root = loader.load();
                    ((ClientDashboardController) loader.getController()).initData(user);
                    break;

                default:
                    throw new IllegalStateException("Unexpected role: " + user.getRole());
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700)); // Всички ползват големия прозорец
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Грешка при зареждане на менюто!");
        }
    }

    @FXML
    public void onRegisterLinkClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tu/courier/register_self.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root)); // Сменяме сцената в същия прозорец
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}