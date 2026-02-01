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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AdminDashboardController {

    private static final Logger logger =
            LogManager.getLogger(AdminDashboardController.class);

    @FXML private Text userLabel;
    @FXML private Label headerLabel;
    @FXML private StackPane contentArea;

    private User loggedUser;

    // 🔹 Извиква се след логин
    public void initData(User user) {
        this.loggedUser = user;
        userLabel.setText(user.getUsername());

        logger.info("Admin logged in: username={}, role={}",
                user.getUsername(), user.getRole());
    }

    // 🔹 Общ метод за смяна на изгледи
    private void loadView(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/tu/courier/" + fxmlFile)
            );
            Parent view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            headerLabel.setText(title);

            logger.info("Admin opened view: {}", title);

        } catch (Exception e) {
            logger.error("Error loading view: {}", fxmlFile, e);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(
                    new Label("Грешка при зареждане на: " + fxmlFile)
            );
        }
    }

    @FXML
    public void onShipmentsClick() {
        loadView("manage_shipments.fxml", "Управление на Пратки");
    }

    @FXML
    public void onCouriersClick() {
        loadView("manage_couriers.fxml", "Управление на Куриери");
    }

    @FXML
    public void onClientsClick() {
        loadView("manage_clients.fxml", "Управление на Клиенти");
    }

    @FXML
    public void onOfficesClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/tu/courier/manage_offices.fxml")
            );
            Parent view = loader.load();

            ManageOfficesController controller = loader.getController();
            controller.setup(this.loggedUser.getRole());

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            headerLabel.setText("Управление на Офиси");

            logger.info("Admin opened Offices management");

        } catch (IOException e) {
            logger.error("Error opening Offices management", e);
        }
    }

    @FXML
    public void onReportsClick() {
        loadView("reports.fxml", "Справки");
    }

    @FXML
    public void onTrackClick() {
        loadView("track_shipment.fxml", "Проследяване на Пратка");
    }

    @FXML
    public void onNotificationsClick() {
        loadView("notifications.fxml", "Известия");
    }

    @FXML
    public void onSettingsClick() {
        headerLabel.setText("Настройки");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(new Label("Профил и настройки..."));

        logger.info("Admin opened Settings");
    }

    @FXML
    public void onCreateShipmentByAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/tu/courier/create_shipment_full.fxml")
            );
            Parent root = loader.load();

            CreateShipmentController controller = loader.getController();
            controller.setup(this.loggedUser);

            Stage stage = new Stage();
            stage.setTitle("Админ: Създаване на Пратка");
            stage.setScene(new Scene(root));
            stage.show();

            logger.info("Admin opened Create Shipment window");

        } catch (IOException e) {
            logger.error("Error opening Create Shipment window", e);
        }
    }

    @FXML
    public void onLogoutClick() {
        try {
            logger.info("Admin logged out: {}", loggedUser.getUsername());

            new CourierApp().start(
                    (Stage) contentArea.getScene().getWindow()
            );
        } catch (Exception e) {
            logger.error("Error during logout", e);
        }
    }
}
