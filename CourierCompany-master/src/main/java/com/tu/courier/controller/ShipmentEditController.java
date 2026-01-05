package com.tu.courier.controller;

import com.tu.courier.dao.ShipmentDAO;
import com.tu.courier.entity.Shipment;
import com.tu.courier.entity.ShipmentStatus;
import com.tu.courier.util.HibernateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class ShipmentEditController {

    @FXML private TextField addressField;
    @FXML private TextField priceField;
    @FXML private ComboBox<ShipmentStatus> statusBox;
    @FXML private Label msgLabel;

    private Shipment shipment;

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;

        // Попълваме полетата с текущите данни
        addressField.setText(shipment.getDeliveryAddress());
        priceField.setText(shipment.getPrice().toString());

        statusBox.getItems().setAll(ShipmentStatus.values());
        statusBox.setValue(shipment.getStatus());
    }

    @FXML
    public void onSave() {
        try {
            // Взимаме новите данни
            String newAddress = addressField.getText().strip();
            BigDecimal newPrice = new BigDecimal(priceField.getText().strip());
            ShipmentStatus newStatus = statusBox.getValue();

            if (newAddress.isBlank()) {
                msgLabel.setText("Адресът е задължителен!");
                return;
            }

            // Обновяваме обекта
            shipment.setDeliveryAddress(newAddress);
            shipment.setPrice(newPrice);
            shipment.setStatus(newStatus);

            // Запис в базата
            ShipmentDAO dao = new ShipmentDAO(HibernateUtil.getSessionFactory());
            dao.updateShipment(shipment);

            ((Stage) addressField.getScene().getWindow()).close();

        } catch (NumberFormatException e) {
            msgLabel.setText("Цената трябва да е число!");
        } catch (Exception e) {
            e.printStackTrace();
            msgLabel.setText("Грешка при запис!");
        }
    }
}