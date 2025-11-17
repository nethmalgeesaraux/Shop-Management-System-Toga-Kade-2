package Controller;

import Models.Db.DatabaseConnection;
import Models.Dto.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemController {

    public boolean saveItem(Item item) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "INSERT INTO Item (itemCode, description, packSize, unitPrice, qtyOnHand) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, item.getItemCode());
        stmt.setString(2, item.getDescription());
        stmt.setString(3, item.getPackSize());
        stmt.setDouble(4, item.getUnitPrice());
        stmt.setInt(5, item.getQtyOnHand());

        return stmt.executeUpdate() > 0;
    }

    public boolean updateItem(Item item) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "UPDATE Item SET Description=?, PackSize=?, UnitPrice=?, QtyOnHand=? WHERE ItemCode=?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, item.getDescription());
        stmt.setString(2, item.getPackSize());
        stmt.setDouble(3, item.getUnitPrice());
        stmt.setInt(4, item.getQtyOnHand());
        stmt.setString(5, item.getItemCode());

        return stmt.executeUpdate() > 0;
    }

    public boolean deleteItem(String itemCode) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "DELETE FROM Item WHERE ItemCode=?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, itemCode);

        return stmt.executeUpdate() > 0;
    }

    public Item searchItem(String itemCode) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT * FROM Item WHERE ItemCode=?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, itemCode);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new Item(
                    rs.getString("itemCode"),
                    rs.getString("description"),
                    rs.getString("packSize"),
                    rs.getDouble("unitPrice"),
                    rs.getInt("qtyOnHand")
            );
        }
        return null;
    }

    public List<Item> getAllItems() throws SQLException {
        List<Item> items = new ArrayList<>();
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT * FROM Item ORDER BY ItemCode";

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            items.add(new Item(
                    rs.getString("ItemCode"),
                    rs.getString("Description"),
                    rs.getString("PackSize"),
                    rs.getDouble("UnitPrice"),
                    rs.getInt("QtyOnHand")
            ));
        }
        return items;
    }

    public boolean updateItemQuantity(String itemCode, int quantity) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "UPDATE Item SET QtyOnHand = QtyOnHand - ? WHERE ItemCode = ? AND QtyOnHand >= ?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, quantity);
        stmt.setString(2, itemCode);
        stmt.setInt(3, quantity);

        return stmt.executeUpdate() > 0;
    }

    public String getItemDescription(String itemCode) throws SQLException {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT Description FROM Item WHERE ItemCode=?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, itemCode);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getString("Description");
        }
        return null;
    }
}