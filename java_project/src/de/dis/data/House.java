package de.dis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.dis.FormUtil;

public class House extends Estate {
    private int numberOfFloors;
    private double price;
    private boolean garden;

    public int getNumberOfFloors() {
        return numberOfFloors;
    }

    public void setNumberOfFloors(int numberOfFloors) {
        this.numberOfFloors = numberOfFloors;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isGarden() {
        return garden;
    }

    public void setGarden(boolean garden) {
        this.garden = garden;
    }

    public static House load(int id) {
        Estate estate = Estate.load(id);

        try {
            Connection con = DBConnectionManager.getInstance().getConnection();

            String selectSQL = "SELECT * FROM houses WHERE fk_estate_id = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                House house = new House();
                house.setId(id);
                house.setCity(estate.getCity());
                house.setPostalCode(estate.getPostalCode());
                house.setStreet(estate.getStreet());
                house.setStreetNumber(estate.getStreetNumber());
                house.setSquareArea(estate.getSquareArea());
                house.setNumberOfFloors(rs.getInt("number_of_floors"));
                house.setPrice(rs.getDouble("price"));
                house.setGarden(rs.getBoolean("garden"));

                rs.close();
                pstmt.close();
                return house;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void save() {
        super.save();
        Connection con = DBConnectionManager.getInstance().getConnection();
        try {
            if (isFlag) {
                String insertSQL = "INSERT INTO houses(fk_estate_id, number_of_floors, price, garden) VALUES (?, ?, ?, ?)";

                PreparedStatement pstmt = con.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);

                pstmt.setInt(1, getId());
                pstmt.setInt(2, getNumberOfFloors());
                pstmt.setDouble(3, getPrice());
                pstmt.setBoolean(4, isGarden());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();

                isFlag = false;
                rs.close();
                pstmt.close();
            } else {
                String updateSQL = "UPDATE houses SET number_of_floors = ?, price = ?, garden = ? WHERE fk_estate_id = ?";
                PreparedStatement psmst = con.prepareStatement(updateSQL);

                psmst.setInt(1, getNumberOfFloors());
                psmst.setDouble(2, getPrice());
                psmst.setBoolean(3, isGarden());
                psmst.setInt(4, getId());
                psmst.executeUpdate();

                isFlag = true;
                psmst.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM estates WHERE id = ?";

        try {
            Connection conn = DBConnectionManager.getInstance().getConnection();

            PreparedStatement psmst = conn.prepareStatement(sql);
            psmst.setInt(1, id);

            int affected = psmst.executeUpdate();

            return (affected == 1) ? Estate.delete(id) : false;

        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public void update(int maklerId) {
        super.update(maklerId);

        setNumberOfFloors(FormUtil.readInt("Number of floors"));
        setPrice(FormUtil.readDouble("Price"));
        setGarden(FormUtil.readBoolean("Garden"));
    }

    @Override
    public String toString() {
        return super.toString() + "\n" +
                "Number of floors: " + getNumberOfFloors() + "\n" +
                "Price: " + getPrice() + "\n" +
                "Garden: " + isGarden();
    }

}
