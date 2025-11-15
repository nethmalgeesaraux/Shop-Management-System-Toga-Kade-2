package Controller;

import Models.Db.DatabaseConnection;
import Models.Dto.Customer;
import javafx.event.ActionEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerController {

    public boolean saveCustomer(Customer customer) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "INSERT INTO Customer (CustID, CustTitle, CustName, DOB, salary, CustAddress, City, Province, PostalCode) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, customer.getCustID());
        stmt.setString(2, customer.getCustTitle());
        stmt.setString(3, customer.getCustName());
        stmt.setDate(4, Date.valueOf(customer.getDob()));
        stmt.setDouble(5, customer.getSalary());
        stmt.setString(6, customer.getCustAddress());
        stmt.setString(7, customer.getCity());
        stmt.setString(8, customer.getProvince());
        stmt.setString(9, customer.getPostalCode());

        return stmt.executeUpdate() > 0;
    }

    public boolean updateCustomer(Customer customer) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "UPDATE Customer SET CustTitle=?, CustName=?, DOB=?, salary=?, CustAddress=?, " +
                "City=?, Province=?, PostalCode=? WHERE CustID=?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, customer.getCustTitle());
        stmt.setString(2, customer.getCustName());
        stmt.setDate(3, Date.valueOf(customer.getDob()));
        stmt.setDouble(4, customer.getSalary());
        stmt.setString(5, customer.getCustAddress());
        stmt.setString(6, customer.getCity());
        stmt.setString(7, customer.getProvince());
        stmt.setString(8, customer.getPostalCode());
        stmt.setString(9, customer.getCustID());

        return stmt.executeUpdate() > 0;
    }

    public boolean deleteCustomer(String custID) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "DELETE FROM Customer WHERE CustID=?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, custID);

        return stmt.executeUpdate() > 0;
    }

    public Customer searchCustomer(String custID) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT * FROM Customer WHERE CustID=?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, custID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new Customer(
                    rs.getString("CustID"),
                    rs.getString("CustTitle"),
                    rs.getString("CustName"),
                    rs.getDate("DOB").toLocalDate(),
                    rs.getDouble("salary"),
                    rs.getString("CustAddress"),
                    rs.getString("City"),
                    rs.getString("Province"),
                    rs.getString("PostalCode")
            );
        }
        return null;
    }

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT * FROM Customer ORDER BY CustID";

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            customers.add(new Customer(
                    rs.getString("CustID"),
                    rs.getString("CustTitle"),
                    rs.getString("CustName"),
                    rs.getDate("DOB").toLocalDate(),
                    rs.getDouble("salary"),
                    rs.getString("CustAddress"),
                    rs.getString("City"),
                    rs.getString("Province"),
                    rs.getString("PostalCode")
            ));
        }
        return customers;
    }

    public String getCustomerName(String custID) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT CustName FROM Customer WHERE CustID=?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, custID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getString("CustName");
        }
        return null;
    }


}