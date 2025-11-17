package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import Models.Dto.Item;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ItemFormController implements Initializable {

    @FXML
    private TextField txtItemCode, txtDescription, txtPackSize, txtUnitPrice, txtQtyOnHand;
    @FXML
    private TableView<Item> tblItem;
    @FXML
    private TableColumn<Item, String> colItemCode, colDescription, colPackSize;
    @FXML
    private TableColumn<Item, Double> colUnitPrice;
    @FXML
    private TableColumn<Item, Integer> colQtyOnHand;

    private ItemController itemController;
    private ObservableList<Item> itemList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        itemController = new ItemController();
        itemList = FXCollections.observableArrayList();


        colItemCode.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPackSize.setCellValueFactory(new PropertyValueFactory<>("packSize"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colQtyOnHand.setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));

        tblItem.setItems(itemList);
        loadAllItems();


        tblItem.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        setItemData(newValue);
                    }
                });
    }

    @FXML
    private void saveItem() {
        try {
            if (validateFields()) {
                Item item = new Item(
                        txtItemCode.getText().toUpperCase(),
                        txtDescription.getText(),
                        txtPackSize.getText(),
                        Double.parseDouble(txtUnitPrice.getText()),
                        Integer.parseInt(txtQtyOnHand.getText())
                );

                if (itemController.saveItem(item)) {
                    showAlert("Success", "Item saved successfully!", Alert.AlertType.INFORMATION);
                    clearFields();
                    loadAllItems();
                } else {
                    showAlert("Error", "Failed to save item!", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error saving item: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void updateItem() {
        try {
            if (validateFields()) {
                Item item = new Item(
                        txtItemCode.getText().toUpperCase(),
                        txtDescription.getText(),
                        txtPackSize.getText(),
                        Double.parseDouble(txtUnitPrice.getText()),
                        Integer.parseInt(txtQtyOnHand.getText())
                );

                if (itemController.updateItem(item)) {
                    showAlert("Success", "Item updated successfully!", Alert.AlertType.INFORMATION);
                    clearFields();
                    loadAllItems();
                } else {
                    showAlert("Error", "Failed to update item!", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error updating item: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void deleteItem() {
        try {
            if (txtItemCode.getText().isEmpty()) {
                showAlert("Validation Error", "Please enter Item Code to delete", Alert.AlertType.WARNING);
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Delete");
            confirmation.setContentText("Are you sure you want to delete item " + txtItemCode.getText() + "?");

            if (confirmation.showAndWait().get() == ButtonType.OK) {
                if (itemController.deleteItem(txtItemCode.getText())) {
                    showAlert("Success", "Item deleted successfully!", Alert.AlertType.INFORMATION);
                    clearFields();
                    loadAllItems();
                } else {
                    showAlert("Error", "Item not found or cannot be deleted!", Alert.AlertType.WARNING);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error deleting item: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void searchItem() {
        try {
            if (txtItemCode.getText().isEmpty()) {
                showAlert("Validation Error", "Please enter Item Code to search", Alert.AlertType.WARNING);
                return;
            }

            Item item = itemController.searchItem(txtItemCode.getText().toUpperCase());
            if (item != null) {
                setItemData(item);
            } else {
                showAlert("Not Found", "Item not found!", Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            showAlert("Error", "Error searching item: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clearFields() {
        txtItemCode.clear();
        txtDescription.clear();
        txtPackSize.clear();
        txtUnitPrice.clear();
        txtQtyOnHand.clear();
        tblItem.getSelectionModel().clearSelection();
    }

    @FXML
    private void backToDashboard() {
        ((Stage) txtItemCode.getScene().getWindow()).close();
    }

    private void setItemData(Item item) {
        txtItemCode.setText(item.getItemCode());
        txtDescription.setText(item.getDescription());
        txtPackSize.setText(item.getPackSize());
        txtUnitPrice.setText(String.valueOf(item.getUnitPrice()));
        txtQtyOnHand.setText(String.valueOf(item.getQtyOnHand()));
    }

    private void loadAllItems() {
        try {
            itemList.clear();
            itemList.addAll(itemController.getAllItems());
        } catch (SQLException e) {
            showAlert("Error", "Error loading items: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateFields() {
        if (txtItemCode.getText().isEmpty() || txtDescription.getText().isEmpty() ||
                txtUnitPrice.getText().isEmpty() || txtQtyOnHand.getText().isEmpty()) {

            showAlert("Validation Error", "Please fill all required fields", Alert.AlertType.WARNING);
            return false;
        }


        if (!txtItemCode.getText().matches("P\\d{3}")) {
            showAlert("Validation Error", "Item Code must be in format P001, P002, etc.", Alert.AlertType.WARNING);
            return false;
        }

        try {
            double unitPrice = Double.parseDouble(txtUnitPrice.getText());
            if (unitPrice <= 0) {
                showAlert("Validation Error", "Unit price must be greater than 0", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid unit price", Alert.AlertType.WARNING);
            return false;
        }

        try {
            int qty = Integer.parseInt(txtQtyOnHand.getText());
            if (qty < 0) {
                showAlert("Validation Error", "Quantity cannot be negative", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid quantity", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}