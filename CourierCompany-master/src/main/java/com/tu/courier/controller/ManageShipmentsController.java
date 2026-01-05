package com.tu.courier.controller;

import com.tu.courier.dao.ShipmentDAO;
import com.tu.courier.entity.Shipment;
import com.tu.courier.entity.ShipmentStatus;
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

public class ManageShipmentsController {

    @FXML private TableView<Shipment> shipmentsTable;
    @FXML private TableColumn<Shipment, Long> colId;
    @FXML private TableColumn<Shipment, String> colSender;
    @FXML private TableColumn<Shipment, String> colReceiver;
    @FXML private TableColumn<Shipment, String> colAddress;
    @FXML private TableColumn<Shipment, Double> colWeight;
    @FXML private TableColumn<Shipment, Double> colPrice;
    @FXML private TableColumn<Shipment, String> colStatus;
    @FXML private TableColumn<Shipment, String> colDate;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter; // String, за да имаме опция "ВСИЧКИ"
    @FXML private Label statusLabel;
    @FXML private TableColumn<Shipment, String> colTrackingId;

    private ShipmentDAO shipmentDAO;
    private ObservableList<Shipment> masterData = FXCollections.observableArrayList();
    private FilteredList<Shipment> filteredData;

    public void initialize() {
        shipmentDAO = new ShipmentDAO(HibernateUtil.getSessionFactory());

        // Стандартни колони
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTrackingId.setCellValueFactory(new PropertyValueFactory<>("trackingId")); // Свързваме кода
        colAddress.setCellValueFactory(cell -> {
            Shipment s = cell.getValue();

            if (s.getToOffice() != null) {
                var o = s.getToOffice();
                String text = o.getCity() + " - " + o.getName(); // или + " (" + o.getAddress() + ")"
                return new SimpleStringProperty("Офис: " + text);
            }

            String addr = s.getDeliveryAddress();
            return new SimpleStringProperty(addr == null ? "" : addr);
        });

        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("shipmentDate"));

        // --- УМНО ПОКАЗВАНЕ НА ИМЕНА (Гост или Клиент) ---

        colSender.setCellValueFactory(cell -> {
            Shipment s = cell.getValue();
            // Ако има регистриран User, взимаме неговото име. Ако не - взимаме текстовото поле.
            String name = (s.getSender() != null) ? s.getSender().getFullName() : s.getSenderName();
            return new SimpleStringProperty(name);
        });

        colReceiver.setCellValueFactory(cell -> {
            Shipment s = cell.getValue();
            String name = (s.getReceiver() != null) ? s.getReceiver().getFullName() : s.getReceiverName();
            return new SimpleStringProperty(name);
        });

        // Настройка на филтри и зареждане
        statusFilter.getItems().add("ВСИЧКИ");
        for (ShipmentStatus status : ShipmentStatus.values()) {
            statusFilter.getItems().add(status.name());
        }
        statusFilter.setValue("ВСИЧКИ");

        loadData();
        setupFilters();
    }

    private void loadData() {
        masterData.setAll(shipmentDAO.getAllShipments());
        if (filteredData != null) updateFilter();
    }


    private void setupFilters() {
        // Създаваме филтриран списък
        filteredData = new FilteredList<>(masterData, p -> true);

        // Слушател за търсачката
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilter());

        // Слушател за статуса
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());

        shipmentsTable.setItems(filteredData);
    }

    // Този метод комбинира логиката на двата филтъра
    private void updateFilter() {
        String searchText = searchField.getText() != null
                ? searchField.getText().strip().toLowerCase()
                : "";
        String statusSel = statusFilter.getValue();

        filteredData.setPredicate(shipment -> {
            boolean matchesStatus = "ВСИЧКИ".equals(statusSel) || shipment.getStatus().name().equals(statusSel);

            boolean matchesSearch = false;
            if (searchText.isEmpty()) {
                matchesSearch = true;
            } else {
                // Търсим по ID, Tracking ID, Име на подател или Име на получател
                if (String.valueOf(shipment.getId()).contains(searchText)) matchesSearch = true;
                else if (shipment.getTrackingId().toLowerCase().contains(searchText)) matchesSearch = true; // <--- НОВО

                    // Проверка на имената (безопасно за null)
                else {
                    String sName = (shipment.getSender() != null) ? shipment.getSender().getFullName() : shipment.getSenderName();
                    String rName = (shipment.getReceiver() != null) ? shipment.getReceiver().getFullName() : shipment.getReceiverName();

                    if (sName != null && sName.toLowerCase().contains(searchText)) matchesSearch = true;
                    if (rName != null && rName.toLowerCase().contains(searchText)) matchesSearch = true;
                }
            }

            return matchesStatus && matchesSearch;
        });
    }

    @FXML
    public void onDeleteShipment() {
        Shipment selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Изберете пратка!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Изтриване");
        alert.setHeaderText("Изтриване на пратка #" + selected.getId());
        alert.setContentText("Сигурни ли сте? Това действие е необратимо!");

        if (alert.showAndWait().get() == ButtonType.OK) {
            shipmentDAO.deleteShipment(selected);
            loadData();
        }
    }

    @FXML
    public void onEditShipment() {
        Shipment selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Изберете пратка за редакция!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tu/courier/shipment_edit_dialog.fxml"));
            Parent root = loader.load();

            ShipmentEditController controller = loader.getController();
            controller.setShipment(selected);

            Stage stage = new Stage();
            stage.setTitle("Редакция на пратка #" + selected.getId());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(shipmentsTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadData(); // Обновяваме след редакция

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}