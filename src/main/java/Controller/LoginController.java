package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameField;

    @FXML
    void OnAction(ActionEvent event) {
        final String correctUsername = "admin";
        final String correctPassword = "admin123";

        String enteredUsername = usernameField.getText();
        String enteredPassword = passwordField.getText();

        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            showAlert("Input Error", "Please enter both username and password");
            return;
        }

        if (enteredUsername.equals(correctUsername) && enteredPassword.equals(correctPassword)) {
            try {
                Stage stage = new Stage();
                stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/View/dashboard.fxml"))));
                Stage currentStage = (Stage) usernameField.getScene().getWindow();
                currentStage.close();
                stage.show();
                showAlert("Success", "Login successful!");
            } catch (IOException e) {
                showAlert("Error", "Failed to load dashboard: " + e.getMessage());
            }
        } else {
            showAlert("Error", "Invalid username or password.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}