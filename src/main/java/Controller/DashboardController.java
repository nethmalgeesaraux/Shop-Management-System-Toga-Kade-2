package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    private void navigateToCustomer() {
        loadForm("/View/CustomerForm.fxml", "Customer Management");
    }

    @FXML
    private void navigateToItem() {
        loadForm("/View/ItemFrom.fxml", "Item Management");
    }

    @FXML
    private void navigateToOrder() {
        loadForm("/View/OrderFrom..fxml", "Order Management");
    }

    @FXML
    private void navigateToOrderDetail() {
        loadForm("/View/OrderDetailFrom..fxml", "Order Detail Management");
    }

    @FXML
    private void navigateToReports() {
        showAlert("Info", "Reports feature will be implemented soon!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void exitApplication() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Exit Application");
        confirmation.setHeaderText("Confirm Exit");
        confirmation.setContentText("Are you sure you want to exit Thoga Kade Management System?");

        if (confirmation.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            System.exit(0);
        }
    }

    private void loadForm(String fxmlPath, String title) {
        try {
            System.out.println("Loading FXML from: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setResizable(false);


            try {
                stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/icon.png")));
            } catch (Exception e) {
                System.out.println("Icon not found: " + e.getMessage());
            }

            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            System.err.println("Error details: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Cannot load " + title + ": " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (NullPointerException e) {
            System.err.println("FXML file not found: " + fxmlPath);
            System.err.println("Current class: " + getClass().getName());
            System.err.println("Class location: " + getClass().getResource("."));
            e.printStackTrace();
            showAlert("Error", "Form file not found: " + fxmlPath, Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}