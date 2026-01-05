package com.tu.courier.controller;

import com.tu.courier.dao.OfficeDAO;
import com.tu.courier.entity.Office;
import com.tu.courier.util.HibernateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class OfficeFormController {

    @FXML private Text titleText;
    @FXML private TextField nameField;
    @FXML private TextField cityField;
    @FXML private TextField addressField;
    @FXML private Label statusLabel;

    private Office officeToEdit;

    public void setEditMode(Office office) {
        this.officeToEdit = office;
        titleText.setText("Редакция на Офис");
        nameField.setText(office.getName());
        cityField.setText(office.getCity());
        addressField.setText(office.getAddress());
    }

    @FXML
    public void onSave() {
        String name = nameField.getText().strip();
        String city = cityField.getText().strip();
        String address = addressField.getText().strip();

        if (name.isBlank() || city.isBlank() || address.isBlank()) {
            statusLabel.setText("Всички полета са задължителни!");
            return;
        }

        OfficeDAO officeDAO = new OfficeDAO(HibernateUtil.getSessionFactory());

        if (officeToEdit == null) {
            // Нов запис
            Office newOffice = new Office();
            newOffice.setName(name);
            newOffice.setCity(city);
            newOffice.setAddress(address);
            officeDAO.saveOffice(newOffice);
        } else {
            // Редакция
            officeToEdit.setName(name);
            officeToEdit.setCity(city);
            officeToEdit.setAddress(address);
            officeDAO.updateOffice(officeToEdit);
        }

        ((Stage) nameField.getScene().getWindow()).close();
    }
}