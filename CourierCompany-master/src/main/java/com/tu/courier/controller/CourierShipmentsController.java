package com.tu.courier.controller;

import com.tu.courier.dao.ShipmentDAO;
import com.tu.courier.entity.Shipment;
import com.tu.courier.entity.ShipmentStatus;
import com.tu.courier.service.NotificationService;
import com.tu.courier.util.HibernateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.List;

public class CourierShipmentsController {

    @FXML private TableView<Shipment> shipmentsTable;
    @FXML private TableColumn<Shipment, Long> colId;
    @FXML private TableColumn<Shipment, String> colSender;
    @FXML private TableColumn<Shipment, String> colReceiver;
    @FXML private TableColumn<Shipment, String> colAddress;
    @FXML private TableColumn<Shipment, BigDecimal> colPrice;
    @FXML private TableColumn<Shipment, String> colStatus;

    @FXML private Label statusLabel;

    private ShipmentDAO shipmentDAO;
    private NotificationService notificationService;

    @FXML
    public void initialize() {
        shipmentDAO = new ShipmentDAO(HibernateUtil.getSessionFactory());
        notificationService = new NotificationService();

        // Настройка на колоните (какво да показват)
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // За подател и получател показваме имената, не целия обект
        colSender.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSenderDisplayName()));

        colReceiver.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReceiverDisplayName()));

        loadData();
    }

    @FXML
    public void onRefresh() {
        loadData();
    }

    private void loadData() {
        List<Shipment> list = shipmentDAO.getActiveShipments();
        ObservableList<Shipment> observableList = FXCollections.observableArrayList(list);
        shipmentsTable.setItems(observableList);
    }

    @FXML
    public void onMarkDelivered() {
        // Взимаме избрания ред от таблицата
        Shipment selected = shipmentsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("❌ Първо изберете пратка от таблицата!");
            return;
        }

        // Ако вече е доставена - просто информираме (за всеки случай)
        if (selected.getStatus() == ShipmentStatus.DELIVERED) {
            statusLabel.setStyle("-fx-text-fill: orange;");
            statusLabel.setText("⚠️ Пратка #" + selected.getId() + " вече е със статус DELIVERED.");
            return;
        }

        // 1) Променяме статуса
        selected.setStatus(ShipmentStatus.DELIVERED);

        try {
            // 2) Записваме в базата
            shipmentDAO.updateShipment(selected);

            // 3) Създаваме известия (ако sender/receiver са регистрирани User-ове)
            // (ако са guest, sender/receiver обектите вероятно са null)
            String trackingId = selected.getTrackingId();
            String messageForSender = "✅ Your shipment was delivered. Tracking ID: " + trackingId;
            String messageForReceiver = "✅ A shipment to you was delivered. Tracking ID: " + trackingId;

            // Подател
            if (selected.getSender() != null) {
                notificationService.notifyUser(selected.getSender(), selected, messageForSender);
            }

            // Получател
            if (selected.getReceiver() != null) {
                notificationService.notifyUser(selected.getReceiver(), selected, messageForReceiver);
            }

            // По желание: глобално известие (примерно за админ лог)
            notificationService.notifyGlobal(selected, "Shipment delivered. Tracking ID: " + trackingId);

            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("✅ Пратка #" + selected.getId() + " е доставена успешно!");

            // 4) Обновяваме таблицата (пратката ще изчезне, защото вече е DELIVERED)
            loadData();

        } catch (Exception e) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("❌ Грешка при доставяне/известия: " + e.getMessage());
            // По желание: e.printStackTrace(); (за debug)
        }
    }
}
