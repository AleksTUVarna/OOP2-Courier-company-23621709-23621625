package com.tu.courier.controller;

import com.tu.courier.CourierApp;
import com.tu.courier.entity.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class CourierDashboardController {

    @FXML private Text userLabel;
    @FXML private Label headerLabel;
    @FXML private StackPane contentArea;

    private User loggedUser;

    public void initData(User user) {
        this.loggedUser = user;
        userLabel.setText(user.getUsername());
    }

    private void loadView(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tu/courier/" + fxmlFile));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            headerLabel.setText(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onActiveTasks() {
        // Зарежда таблицата с доставки, която направихме по-рано
        loadView("courier_shipments.fxml", "Задачи за изпълнение");
    }

    @FXML
    public void onRegisterClient() {
        loadView("register_client.fxml", "Регистрация на нов Клиент");
    }

    @FXML
    public void onTrackClick() {
        loadView("track_shipment.fxml", "Проследяване");
    }

    @FXML
    public void onNotificationsClick() {
        loadView("notifications.fxml", "Известия");
    }


    @FXML
    public void onOfficesClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tu/courier/manage_offices.fxml"));
            Parent view = loader.load();

            ManageOfficesController controller = loader.getController();
            controller.setup(this.loggedUser.getRole()); // <--- ВАЖНО

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            headerLabel.setText("Списък Офиси");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onLogoutClick() {
        try {
            new CourierApp().start((Stage) contentArea.getScene().getWindow());
        } catch (Exception e) { e.printStackTrace(); }
    }
}