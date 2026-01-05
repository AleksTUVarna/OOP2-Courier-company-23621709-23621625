package com.tu.courier.controller;

import com.tu.courier.dao.OfficeDAO;
import com.tu.courier.dao.UserDAO;
import com.tu.courier.entity.Office;
import com.tu.courier.entity.Role;
import com.tu.courier.entity.User;
import com.tu.courier.util.HibernateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.text.Text;

public class CourierFormController {

    @FXML private Text titleText;
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Office> officeComboBox;
    @FXML private Label statusLabel;

    private User userToEdit; // Ако е null -> Нов запис, иначе -> Редакция

    public void initialize() {
        // Зареждане на офисите в менюто
        OfficeDAO officeDAO = new OfficeDAO(HibernateUtil.getSessionFactory());
        officeComboBox.getItems().addAll(officeDAO.getAllOffices());

        // Как да се показват офисите в менюто (Град - Адрес)
        officeComboBox.setConverter(new StringConverter<Office>() {
            @Override
            public String toString(Office office) {
                return office == null ? "" : office.getCity() + " - " + office.getAddress();
            }
            @Override
            public Office fromString(String string) { return null; }
        });
    }

    // Този метод се вика от главния контролер при натискане на "Редактирай"
    public void setEditMode(User user) {
        this.userToEdit = user;
        titleText.setText("Редактиране на Куриер");

        nameField.setText(user.getFullName());
        usernameField.setText(user.getUsername());
        officeComboBox.setValue(user.getOffice());
        passwordField.setPromptText("Въведи само ако искаш нова парола");
    }

    @FXML
    public void onSave() {
        String name = nameField.getText().strip();
        String username = usernameField.getText().strip();
        String password = passwordField.getText().strip();
        Office selectedOffice = officeComboBox.getValue();

        if (name.isBlank() || username.isBlank()) {
            statusLabel.setText("Името и User-a са задължителни!");
            return;
        }

        UserDAO userDAO = new UserDAO(HibernateUtil.getSessionFactory());

        if (userToEdit == null) {
            // --- СЪЗДАВАНЕ НА НОВ ---
            if (password.isBlank()) {
                statusLabel.setText("Паролата е задължителна за нов!");
                return;
            }
            if (userDAO.findByUsername(username) != null) {
                statusLabel.setText("Този username е зает!");
                return;
            }

            User newUser = new User();
            newUser.setFullName(name);
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setRole(Role.COURIER);
            newUser.setOffice(selectedOffice); // Свързваме с офис

            userDAO.saveUser(newUser);

        } else {
            // --- РЕДАКЦИЯ ---
            userToEdit.setFullName(name);
            userToEdit.setUsername(username);
            userToEdit.setOffice(selectedOffice);

            // Сменяме паролата само ако е написал нещо ново
            if (!password.isBlank()) {
                userToEdit.setPassword(password);
            }

            userDAO.updateUser(userToEdit);
        }

        // Затваряме
        ((Stage) nameField.getScene().getWindow()).close();
    }
}