package com.tu.courier.controller;

import com.tu.courier.dao.ShipmentDAO;
import com.tu.courier.entity.Shipment;
import com.tu.courier.util.HibernateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class TrackShipmentController {

    @FXML private TextField searchField;
    @FXML private Label errorLabel;
    @FXML private VBox resultBox;

    // Елементи за резултата
    @FXML private Label lblStatus;
    @FXML private Label lblSender;
    @FXML private Label lblReceiver;
    @FXML private Label lblAddress;
    @FXML private Label lblDate;

    @FXML
    public void onSearch() {
        String trackingId = searchField.getText().strip();

        if (trackingId.isBlank()) {
            errorLabel.setText("Моля въведете номер!");
            resultBox.setVisible(false);
            return;
        }

        ShipmentDAO dao = new ShipmentDAO(HibernateUtil.getSessionFactory());
        Shipment shipment = dao.findByTrackingId(trackingId);

        if (shipment == null) {
            errorLabel.setText("Не е намерена пратка с номер: " + trackingId);
            resultBox.setVisible(false);
        } else {
            // УСПЕХ! Показваме данните
            errorLabel.setText("");
            resultBox.setVisible(true);

            lblStatus.setText(shipment.getStatus().toString());

            // Задаване на цветове според статуса
            switch (shipment.getStatus()) {
                case DELIVERED -> lblStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 18px;");
                case CREATED -> lblStatus.setStyle("-fx-text-fill: blue; -fx-font-weight: bold; -fx-font-size: 18px;");
                case SENT -> lblStatus.setStyle("-fx-text-fill: orange; -fx-font-weight: bold; -fx-font-size: 18px;");
            }

            // Попълване на данните (ползваме имената, записани в пратката)
            lblSender.setText(shipment.getSenderName() != null ? shipment.getSenderName() : "Анонимен");
            lblReceiver.setText(shipment.getReceiverName() != null ? shipment.getReceiverName() : "Анонимен");
            lblAddress.setText(shipment.getDeliveryAddress());

            if (shipment.getShipmentDate() != null) {
                lblDate.setText(shipment.getShipmentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            } else {
                lblDate.setText("-");
            }
        }
    }
}