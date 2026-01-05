package com.tu.courier.controller;

import com.tu.courier.dao.ShipmentDAO;
import com.tu.courier.entity.Shipment;
import com.tu.courier.entity.ShipmentStatus;
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

    @FXML
    public void initialize() {
        shipmentDAO = new ShipmentDAO(HibernateUtil.getSessionFactory());

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
            statusLabel.setText("❌ Първо изберете пратка от таблицата!");
            return;
        }

        // Променяме статуса
        selected.setStatus(ShipmentStatus.DELIVERED);

        // Записваме в базата
        shipmentDAO.updateShipment(selected);

        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setText("✅ Пратка #" + selected.getId() + " е доставена успешно!");

        // Обновяваме таблицата (пратката ще изчезне, защото вече е DELIVERED)
        loadData();
    }


}