package com.tu.courier.controller;

import com.tu.courier.dao.OfficeDAO;
import com.tu.courier.entity.Office;
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
import com.tu.courier.entity.Role;
import java.io.IOException;
import java.util.Optional;

public class ManageOfficesController {

    @FXML private TableView<Office> officesTable;
    @FXML private TableColumn<Office, Long> colId;
    @FXML private TableColumn<Office, String> colName;
    @FXML private TableColumn<Office, String> colCity;
    @FXML private TableColumn<Office, String> colAddress;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    // --- НОВИ ПОЛЕТА ЗА БУТОНИТЕ ---
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private OfficeDAO officeDAO;
    private ObservableList<Office> masterData = FXCollections.observableArrayList();

    // Запазваме ролята
    private Role currentRole;

    public void initialize() {
        officeDAO = new OfficeDAO(HibernateUtil.getSessionFactory());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        loadData();
        setupSearch();
    }

    public void setup(Role role) {
        this.currentRole = role;

        if (role != Role.ADMIN) {
            // Ако НЕ е админ -> скриваме бутоните
            btnAdd.setVisible(false);
            btnAdd.setManaged(false); // managed=false означава, че бутонът не заема място

            btnEdit.setVisible(false);
            btnEdit.setManaged(false);

            btnDelete.setVisible(false);
            btnDelete.setManaged(false);
        }
    }

    private void loadData() {
        masterData.setAll(officeDAO.getAllOffices());
        officesTable.setItems(masterData);
    }

    private void setupSearch() {
        FilteredList<Office> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(office -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();

                if (office.getName() != null && office.getName().toLowerCase().contains(lowerCaseFilter)) return true;
                if (office.getCity().toLowerCase().contains(lowerCaseFilter)) return true;
                if (office.getAddress().toLowerCase().contains(lowerCaseFilter)) return true;

                return false;
            });
        });

        officesTable.setItems(filteredData);
    }

    @FXML
    public void onAddOffice() {
        openDialog(null);
    }

    @FXML
    public void onEditOffice() {
        Office selected = officesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Изберете офис за редакция!");
            return;
        }
        openDialog(selected);
    }

    @FXML
    public void onDeleteOffice() {
        Office selected = officesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Изберете офис за изтриване!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Изтриване");
        alert.setHeaderText("Сигурни ли сте?");
        alert.setContentText("Ще изтриете офис: " + selected.getName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                officeDAO.deleteOffice(selected);
                loadData();
                statusLabel.setText("");
            } catch (Exception e) {
                statusLabel.setText("Грешка! В този офис има регистрирани служители.");
            }
        }
    }

    private void openDialog(Office officeToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tu/courier/office_form.fxml"));
            Parent root = loader.load();

            OfficeFormController controller = loader.getController();
            if (officeToEdit != null) {
                controller.setEditMode(officeToEdit);
            }

            Stage stage = new Stage();
            stage.setTitle(officeToEdit == null ? "Нов Офис" : "Редакция на Офис");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(officesTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}