package com.tu.courier.controller;

import com.tu.courier.dao.UserDAO;
import com.tu.courier.entity.User;
import com.tu.courier.util.HibernateUtil;
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

public class ManageClientsController {

    @FXML private TableView<User> clientsTable;
    @FXML private TableColumn<User, Long> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private UserDAO userDAO;
    private ObservableList<User> masterData = FXCollections.observableArrayList();

    public void initialize() {
        userDAO = new UserDAO(HibernateUtil.getSessionFactory());

        // Настройка на колоните (трябва да съвпадат с имената в User.java)
        colId.setCellValueFactory(new PropertyValueFactory<>("clientNo"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        loadData();
        setupSearch();
    }

    private void loadData() {
        // Използваме метода за клиенти, който направихме преди
        masterData.setAll(userDAO.getAllClients());
        clientsTable.setItems(masterData);
    }

    private void setupSearch() {
        FilteredList<User> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();

                // Търсене във всички полета
                if (user.getFullName().toLowerCase().contains(lowerCaseFilter)) return true;
                if (user.getUsername().toLowerCase().contains(lowerCaseFilter)) return true;
                if (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerCaseFilter)) return true;
                if (user.getPhone() != null && user.getPhone().contains(lowerCaseFilter)) return true;

                return false;
            });
        });

        clientsTable.setItems(filteredData);
    }

    @FXML
    public void onAddClient() {
        openDialog(null);
    }

    @FXML
    public void onEditClient() {
        User selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Изберете клиент за редакция!");
            return;
        }
        openDialog(selected);
    }

    @FXML
    public void onDeleteClient() {
        User selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Изберете клиент за изтриване!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Изтриване");
        alert.setHeaderText("Сигурни ли сте?");
        alert.setContentText("Ще изтриете клиент: " + selected.getFullName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userDAO.deleteUser(selected);
                loadData();
                statusLabel.setText("");
            } catch (Exception e) {
                statusLabel.setText("Грешка! Клиентът има активни пратки.");
            }
        }
    }

    private void openDialog(User clientToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tu/courier/client_form.fxml"));
            Parent root = loader.load();

            ClientFormController controller = loader.getController();
            if (clientToEdit != null) {
                controller.setEditMode(clientToEdit);
            }

            Stage stage = new Stage();
            stage.setTitle(clientToEdit == null ? "Нов Клиент" : "Редакция на Клиент");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(clientsTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}