package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import Models.Dto.Customer;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CustomerFormController implements Initializable {

    @FXML private TextField txtCustID, txtCustName, txtSalary, txtCustAddress, txtCity, txtProvince, txtPostalCode;
    @FXML private ComboBox<String> cmbTitle;
    @FXML private DatePicker dpDob;
    @FXML private TableView<Customer> tblCustomer;
    @FXML private TableColumn<Customer, String> colCustID, colTitle, colName, colAddress, colCity, colProvince, colPostalCode;
    @FXML private TableColumn<Customer, LocalDate> colDob;
    @FXML private TableColumn<Customer, Double> colSalary;

    private CustomerController customerController;
    private ObservableList<Customer> customerList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        customerController = new CustomerController();
        customerList = FXCollections.observableArrayList();

        // Initialize combo box
        cmbTitle.setItems(FXCollections.observableArrayList("Mr", "Mrs", "Miss", "Ms"));

        // Initialize  columns
        colCustID.setCellValueFactory(new PropertyValueFactory<>("custID"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("custTitle"));
        colName.setCellValueFactory(new PropertyValueFactory<>("custName"));
        colDob.setCellValueFactory(new PropertyValueFactory<>("dob"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("custAddress"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        colProvince.setCellValueFactory(new PropertyValueFactory<>("province"));
        colPostalCode.setCellValueFactory(new PropertyValueFactory<>("postalCode"));

        tblCustomer.setItems(customerList);
        loadAllCustomers();

        // Add selection listener
        tblCustomer.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        setCustomerData(newValue);
                    }
                });
    }

    @FXML
    private void saveCustomer() {
        try {
            if (validateFields()) {
                Customer customer = new Customer(
                        txtCustID.getText().toUpperCase(),
                        cmbTitle.getValue(),
                        txtCustName.getText(),
                        dpDob.getValue(),
                        Double.parseDouble(txtSalary.getText()),
                        txtCustAddress.getText(),
                        txtCity.getText(),
                        txtProvince.getText(),
                        txtPostalCode.getText()
                );

                if (customerController.saveCustomer(customer)) {
                    showAlert("Success", "Customer saved successfully!", Alert.AlertType.INFORMATION);
                    clearFields();
                    loadAllCustomers();
                } else {
                    showAlert("Error", "Failed to save customer!", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error saving customer: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void updateCustomer() {
        try {
            if (validateFields()) {
                Customer customer = new Customer(
                        txtCustID.getText().toUpperCase(),
                        cmbTitle.getValue(),
                        txtCustName.getText(),
                        dpDob.getValue(),
                        Double.parseDouble(txtSalary.getText()),
                        txtCustAddress.getText(),
                        txtCity.getText(),
                        txtProvince.getText(),
                        txtPostalCode.getText()
                );

                if (customerController.updateCustomer(customer)) {
                    showAlert("Success", "Customer updated successfully!", Alert.AlertType.INFORMATION);
                    clearFields();
                    loadAllCustomers();
                } else {
                    showAlert("Error", "Failed to update customer!", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error updating customer: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void deleteCustomer() {
        try {
            if (txtCustID.getText().isEmpty()) {
                showAlert("Validation Error", "Please enter Customer ID to delete", Alert.AlertType.WARNING);
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Delete");
            confirmation.setContentText("Are you sure you want to delete customer " + txtCustID.getText() + "?");

            if (confirmation.showAndWait().get() == ButtonType.OK) {
                if (customerController.deleteCustomer(txtCustID.getText())) {
                    showAlert("Success", "Customer deleted successfully!", Alert.AlertType.INFORMATION);
                    clearFields();
                    loadAllCustomers();
                } else {
                    showAlert("Error", "Customer not found or cannot be deleted!", Alert.AlertType.WARNING);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Error deleting customer: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void searchCustomer() {
        try {
            if (txtCustID.getText().isEmpty()) {
                showAlert("Validation Error", "Please enter Customer ID to search", Alert.AlertType.WARNING);
                return;
            }

            Customer customer = customerController.searchCustomer(txtCustID.getText().toUpperCase());
            if (customer != null) {
                setCustomerData(customer);
            } else {
                showAlert("Not Found", "Customer not found!", Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            showAlert("Error", "Error searching customer: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clearFields() {
        txtCustID.clear();
        cmbTitle.getSelectionModel().clearSelection();
        txtCustName.clear();
        dpDob.setValue(null);
        txtSalary.clear();
        txtCustAddress.clear();
        txtCity.clear();
        txtProvince.clear();
        txtPostalCode.clear();
        tblCustomer.getSelectionModel().clearSelection();
    }

    @FXML
    private void backToDashboard() {
        ((Stage) txtCustID.getScene().getWindow()).close();
    }

    private void setCustomerData(Customer customer) {
        txtCustID.setText(customer.getCustID());
        cmbTitle.setValue(customer.getCustTitle());
        txtCustName.setText(customer.getCustName());
        dpDob.setValue(customer.getDob());
        txtSalary.setText(String.valueOf(customer.getSalary()));
        txtCustAddress.setText(customer.getCustAddress());
        txtCity.setText(customer.getCity());
        txtProvince.setText(customer.getProvince());
        txtPostalCode.setText(customer.getPostalCode());
    }

    private void loadAllCustomers() {
        try {
            customerList.clear();
            customerList.addAll(customerController.getAllCustomers());
        } catch (SQLException e) {
            showAlert("Error", "Error loading customers: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateFields() {
        if (txtCustID.getText().isEmpty() || cmbTitle.getValue() == null ||
                txtCustName.getText().isEmpty() || dpDob.getValue() == null ||
                txtSalary.getText().isEmpty() || txtCustAddress.getText().isEmpty() ||
                txtCity.getText().isEmpty() || txtProvince.getText().isEmpty()) {

            showAlert("Validation Error", "Please fill all required fields", Alert.AlertType.WARNING);
            return false;
        }

        // Validate Customer ID format
        if (!txtCustID.getText().matches("C\\d{3}")) {
            showAlert("Validation Error", "Customer ID must be in format C001, C002, etc.", Alert.AlertType.WARNING);
            return false;
        }

        try {
            double salary = Double.parseDouble(txtSalary.getText());
            if (salary <= 0) {
                showAlert("Validation Error", "Salary must be greater than 0", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid salary", Alert.AlertType.WARNING);
            return false;
        }

        if (dpDob.getValue().isAfter(LocalDate.now())) {
            showAlert("Validation Error", "Date of birth cannot be in the future", Alert.AlertType.WARNING);
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