package com.tu.courier.controller;

import com.tu.courier.CourierApp;
import com.tu.courier.entity.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientDashboardController {

    @FXML private Text userLabel;
    @FXML private Label headerLabel;
    @FXML private StackPane contentArea;
    @FXML private BorderPane rootPane;

    private User loggedUser;

    public void initData(User user) {
        this.loggedUser = user;
        userLabel.setText(user.getUsername());
    }

    private void loadView(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tu/courier/" + fxmlFile));
            Parent view = loader.load();

            // Ако зареждаме формата за пратка, трябва да подадем потребителя
            if (loader.getController() instanceof CreateShipmentController) {
                ((CreateShipmentController) loader.getController()).setup(this.loggedUser);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            headerLabel.setText(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onSendShipment() {
        // Отваряме новата форма, която направихме
        loadView("create_shipment_full.fxml", "Изпращане на Пратка");
    }

    @FXML
    public void onMyShipments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tu/courier/my_shipments.fxml"));
            Parent view = loader.load();

            // ВАЖНО: Подаваме потребителя на контролера!
            MyShipmentsController controller = loader.getController();
            controller.setup(this.loggedUser);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            headerLabel.setText("Моите Пратки");

        } catch (IOException e) {
            e.printStackTrace();
        }
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

            // Взимаме контролера и му казваме каква е ролята
            ManageOfficesController controller = loader.getController();
            controller.setup(this.loggedUser.getRole()); // <--- ВАЖНО

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            headerLabel.setText("Списък с Офиси");

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