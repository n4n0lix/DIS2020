package de.dis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.dis.FormUtil;

public class Apartment extends Estate {

    private int floor;
    private double rent;
    private int room;
    private boolean balcony;
    private boolean kitchen;

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public double getRent() {
        return rent;
    }

    public void setRent(double rent) {
        this.rent = rent;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public boolean isBalcony() {
        return balcony;
    }

    public void setBalcony(boolean balcony) {
        this.balcony = balcony;
    }

    public boolean isKitchen() {
        return kitchen;
    }

    public void setKitchen(boolean kitchen) {
        this.kitchen = kitchen;
    }

    public static Apartment load(int id) {
        Estate estate = Estate.load(id);

        try {
            Connection con = DBConnectionManager.getInstance().getConnection();

            String selectSQL = "SELECT * FROM apartments WHERE fk_estate_id = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Apartment apartment = new Apartment();
                apartment.setId(id);
                apartment.setCity(estate.getCity());
                apartment.setPostalCode(estate.getPostalCode());
                apartment.setStreet(estate.getStreet());
                apartment.setStreetNumber(estate.getStreetNumber());
                apartment.setSquareArea(estate.getSquareArea());

                apartment.setFloor(rs.getInt("number_of_floors"));
                apartment.setRent(rs.getDouble("rent"));
                apartment.setRoom(rs.getInt("room"));
                apartment.setBalcony(rs.getBoolean("balcony"));
                apartment.setKitchen(rs.getBoolean("kitchen"));

                rs.close();
                pstmt.close();
                return apartment;
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
                String insertSQL = "INSERT INTO apartments(fk_estate_id, floor, rent, room, balcony, kitchen) VALUES (?, ?, ?, ?, ?, ?)";

                PreparedStatement pstmt = con.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);

                pstmt.setInt(1, getFkMaklerId());
                pstmt.setInt(2, getFloor());
                pstmt.setDouble(3, getRent());
                pstmt.setInt(4, getRoom());
                pstmt.setBoolean(5, isBalcony());
                pstmt.setBoolean(6, isKitchen());

                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    setId(rs.getInt(1));
                }
                rs.close();
                pstmt.close();
            } else {
                String updateSQL = "UPDATE apartments SET floor = ?, rent = ?, room = ?, balcony = ?, kitchen = ?  WHERE fk_estate_id = ?";
                PreparedStatement pstmt = con.prepareStatement(updateSQL);

                pstmt.setInt(1, getFkMaklerId());
                pstmt.setInt(2, getFloor());
                pstmt.setDouble(3, getRent());
                pstmt.setInt(4, getRoom());
                pstmt.setBoolean(5, isBalcony());
                pstmt.setBoolean(6, isKitchen());

                pstmt.executeUpdate();
                pstmt.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM apartments WHERE fk_estate_id = ?";

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

        setFloor(FormUtil.readInt("Number of floors"));
        setRent(FormUtil.readDouble("Rent"));
        setRoom(FormUtil.readInt("Room"));
        setBalcony(FormUtil.readBoolean("Balcony"));
        setKitchen(FormUtil.readBoolean("Kitchen"));
    }

    @Override
    public String toString() {
        return super.toString() + "\n" +
                "Number of floors: " + getFloor() + "\n" +
                "Rent: " + getRent() + "\n" +
                "Room: " + getRoom() + "\n" +
                "Balcony: " + isBalcony() + "\n" +
                "Kitchen: " + isKitchen();
    }


}
