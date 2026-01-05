package com.tu.courier.controller;

import com.tu.courier.CourierApp;
import com.tu.courier.entity.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private Text userLabel;
    @FXML private Label headerLabel;
    @FXML private StackPane contentArea; // Това е бялото поле вдясно

    private User loggedUser;

    public void initData(User user) {
        this.loggedUser = user;
        userLabel.setText(user.getUsername());
    }

    // --- ЛОГИКА ЗА СМЯНА НА ЕКРАНИТЕ ---

    // Помощен метод, който зарежда FXML в средата
    private void loadView(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tu/courier/" + fxmlFile));
            Parent view = loader.load();

            // Изчистваме старото и слагаме новото
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            // Сменяме заглавието горе
            headerLabel.setText(title);

        } catch (IOException e) {
            e.printStackTrace();
            // Ако файла го няма, показваме грешка в UI
            contentArea.getChildren().clear();
            contentArea.getChildren().add(new Label("Грешка при зареждане на: " + fxmlFile));
        }
    }

    @FXML public void onShipmentsClick() {
        loadView("manage_shipments.fxml", "Управление на Пратки");
    }

    @FXML public void onCouriersClick() {
        loadView("manage_couriers.fxml", "Управление на Куриери");
    }

    @FXML public void onClientsClick() {
        loadView("manage_clients.fxml", "Управление на Клиенти");
    }

    @FXML public void onOfficesClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tu/courier/manage_offices.fxml"));
            Parent view = loader.load();

            ManageOfficesController controller = loader.getController();
            controller.setup(this.loggedUser.getRole()); // Админът ще си види бутоните

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            headerLabel.setText("Управление на Офиси");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML public void onReportsClick() {
        headerLabel.setText("Справки");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(new Label("Графики и статистики..."));
    }

    @FXML public void onTrackClick() {
        loadView("track_shipment.fxml", "Проследяване на Пратка");
    }

    @FXML public void onSettingsClick() {
        headerLabel.setText("Настройки");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(new Label("Профил и настройки..."));
    }

    @FXML public void onLogoutClick() {
        try {
            new CourierApp().start((Stage) contentArea.getScene().getWindow());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onCreateShipmentByAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tu/courier/create_shipment_full.fxml"));
            javafx.scene.Parent root = loader.load();

            CreateShipmentController controller = loader.getController();
            controller.setup(this.loggedUser); // Подаваме Админа

            Stage stage = new Stage();
            stage.setTitle("Админ: Създаване на Пратка");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

}