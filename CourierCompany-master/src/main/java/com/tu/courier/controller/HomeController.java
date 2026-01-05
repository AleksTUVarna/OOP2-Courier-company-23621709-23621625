package com.tu.courier.controller;

import com.tu.courier.CourierApp;
import com.tu.courier.entity.Role;
import com.tu.courier.entity.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;

public class HomeController {

    @FXML
    private Text welcomeText;

    // Бутоните от FXML файла
    @FXML private Button btnRegisterCourier;
    @FXML private Button btnCreateShipment;
    @FXML private Button btnMyShipments;
    @FXML private Button btnCourierShipments;
    @FXML private Button btnRegisterClient;

    // Запазваме потребителя, ако ни трябва по-късно
    private User loggedUser;

    public void initData(User user) {
        this.loggedUser = user;
        welcomeText.setText("Здравей, " + user.getFullName());

        // ЛОГИКА ЗА РОЛИТЕ
        if (user.getRole() == Role.ADMIN) {
            // Админът вижда само бутона за добавяне на куриер
            btnRegisterCourier.setVisible(true);
            btnRegisterCourier.setManaged(true); // managed=true означава, че заема място на екрана
        }
        else if (user.getRole() == Role.CLIENT) {
            // Клиентът вижда бутони за пратки
            btnCreateShipment.setVisible(true);
            btnCreateShipment.setManaged(true);

            btnMyShipments.setVisible(true);
            btnMyShipments.setManaged(true);
        }
        else if (user.getRole() == Role.COURIER) {
            // Куриерът вижда своите задачи
            btnCourierShipments.setVisible(true);
            btnCourierShipments.setManaged(true);

            // И бутона за регистрация на клиент
            btnRegisterClient.setVisible(true);
            btnRegisterClient.setManaged(true);
        }
    }

    @FXML
    public void onLogout(ActionEvent event) throws IOException {
        // Затваряме текущия прозорец
        // За MenuItems е по-трудно да се вземе сцената директно, затова ползваме btnRegisterCourier като котва (anchor),
        // или който и да е елемент от сцената, за да намерим прозореца.
        Stage stage = (Stage) welcomeText.getScene().getWindow();
        stage.close();

        // Отваряме наново Login
        new CourierApp().start(new Stage());
    }

    // --- ACTIONS (Тук ще добавяме логиката по-късно) ---

    @FXML
    public void onRegisterCourier() {
        try {
            // Зареждаме прозореца за регистрация
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/tu/courier/register_courier.fxml"));
            javafx.scene.Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Добавяне на служител");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show(); // show(), а не showAndWait(), за да може да се ползва и главното меню

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onCreateShipment() {
        try {
            // Зареждаме НОВИЯ FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tu/courier/create_shipment_full.fxml"));
            javafx.scene.Parent root = loader.load();

            CreateShipmentController controller = loader.getController();
            // Подаваме текущия потребител (setup метод вместо setSender)
            controller.setup(this.loggedUser);

            Stage stage = new Stage();
            stage.setTitle("Нова Пратка");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void onRegisterClient() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/tu/courier/register_client.fxml"));
            javafx.scene.Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Регистрация на нов Клиент");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onCourierShipments() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/tu/courier/courier_shipments.fxml"));
            javafx.scene.Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Списък с пратки");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}