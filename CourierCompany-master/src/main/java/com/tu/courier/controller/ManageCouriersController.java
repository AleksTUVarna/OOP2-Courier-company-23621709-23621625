package com.tu.courier.controller;

import com.tu.courier.dao.UserDAO;
import com.tu.courier.entity.User;
import com.tu.courier.util.HibernateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class ManageCouriersController {

    @FXML private TableView<User> couriersTable;
    @FXML private TableColumn<User, Long> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colCity;
    @FXML private TableColumn<User, String> colOffice;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private UserDAO userDAO;
    private ObservableList<User> masterData = FXCollections.observableArrayList();

    public void initialize() {
        userDAO = new UserDAO(HibernateUtil.getSessionFactory());

        // Настройка на колоните
        colId.setCellValueFactory(new PropertyValueFactory<>("courierNo"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));

        // За град и офис трябва да бръкнем в свързания обект Office
        colCity.setCellValueFactory(cell -> {
            if (cell.getValue().getOffice() != null)
                return new SimpleStringProperty(cell.getValue().getOffice().getCity());
            return new SimpleStringProperty("-");
        });

        colOffice.setCellValueFactory(cell -> {
            if (cell.getValue().getOffice() != null)
                return new SimpleStringProperty(cell.getValue().getOffice().getAddress());
            return new SimpleStringProperty("Без офис");
        });

        loadData();
        setupSearch();
    }

    private void loadData() {
        masterData.setAll(userDAO.getAllCouriers());
        couriersTable.setItems(masterData);
    }

    private void setupSearch() {
        // Опаковаме данните във филтриран списък
        FilteredList<User> filteredData = new FilteredList<>(masterData, p -> true);

        // Слушаме какво пише потребителят
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();

                // Търсим по Име, Username или Град
                if (user.getFullName().toLowerCase().contains(lowerCaseFilter)) return true;
                if (user.getUsername().toLowerCase().contains(lowerCaseFilter)) return true;
                if (user.getOffice() != null && user.getOffice().getCity().toLowerCase().contains(lowerCaseFilter)) return true;

                return false;
            });
        });

        couriersTable.setItems(filteredData);
    }

    @FXML
    public void onAddCourier() {
        openDialog("register_courier_full.fxml", "Нов Куриер", null);
    }

    @FXML
    public void onEditCourier() {
        User selected = couriersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Изберете куриер за редакция!");
            return;
        }
        openDialog("register_courier_full.fxml", "Редакция на Куриер", selected);
    }

    @FXML
    public void onDeleteCourier() {
        User selected = couriersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Изберете куриер за изтриване!");
            return;
        }

        // Потвърждение
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Изтриване");
        alert.setHeaderText("Сигурни ли сте?");
        alert.setContentText("Ще изтриете: " + selected.getFullName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userDAO.deleteUser(selected);
                loadData(); // Презареждаме таблицата
                statusLabel.setText("");
            } catch (Exception e) {
                statusLabel.setText("Грешка! Вероятно куриерът има свързани пратки.");
            }
        }
    }

    // Помощен метод за отваряне на прозореца (Add/Edit ползват един и същ FXML)
    private void openDialog(String fxml, String title, User userToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tu/courier/" + fxml));
            Parent root = loader.load();

            // Взимаме контролера на формата
            CourierFormController controller = loader.getController();

            // Ако редактираме, подаваме данните
            if (userToEdit != null) {
                controller.setEditMode(userToEdit);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.WINDOW_MODAL); // Блокира задния прозорец
            stage.initOwner(couriersTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Като затворим прозореца, обновяваме таблицата
            loadData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}