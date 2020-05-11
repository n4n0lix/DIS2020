package de.dis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.dis.FormUtil;

public class Estate {
    private int id = -1;
    private int fkMaklerId;
    private String city;
    private int postalCode;
    private String street;
    private int streetNumber;
    private double squareArea;
    protected Boolean isFlag = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(int postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public int getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(int streetNumber) {
        this.streetNumber = streetNumber;
    }
    public double getSquareArea() {
        return squareArea;
    }
    public void setSquareArea(double squareArea) {
        this.squareArea = squareArea;
    }

    public int getFkMaklerId() {
        return fkMaklerId;
    }

    public void setEstateAgentId(int fkMaklerId) {
        this.fkMaklerId = fkMaklerId;
    }

    public static Estate load(int id) {
        try {
            Connection con = DBConnectionManager.getInstance().getConnection();

            String selectSQL =
                    "SELECT * FROM estates WHERE id = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Estate estate = new Estate();
                estate.setId(id);
                estate.setCity(rs.getString("city"));
                estate.setPostalCode(rs.getInt("postal_code"));
                estate.setStreet(rs.getString("street"));
                estate.setStreetNumber(rs.getInt("street_number"));
                estate.setSquareArea(rs.getDouble("square_area"));
                estate.setEstateAgentId(rs.getInt("fk_estate_agent_id"));

                rs.close();
                pstmt.close();
                return estate;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void save() {
        Connection con = DBConnectionManager.getInstance().getConnection();

        try {
            if (getId() == -1) {
                String insertSQL = "INSERT INTO estates(city, postal_code, street, street_number, square_area, fk_estate_agent_id) VALUES (?, ?, ?, ?, ?, ?)";

                PreparedStatement pstmt = con.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);

                pstmt.setString(1, getCity());
                pstmt.setInt(2, getPostalCode());
                pstmt.setString(3, getStreet());
                pstmt.setInt(4, getStreetNumber());
                pstmt.setDouble(5, getSquareArea());
                pstmt.setInt(6, getFkMaklerId());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    setId(rs.getInt(1));

                    isFlag = true;
                }

                rs.close();
                pstmt.close();

            } else {
                String updateSQL = "UPDATE estates SET city = ?, postal_code = ?, street = ?, street_number = ?, square_area = ?, fk_estate_agent_id = ? where id = ?";
                PreparedStatement pstmt = con.prepareStatement(updateSQL);

                pstmt.setString(1, getCity());
                pstmt.setInt(2, getPostalCode());
                pstmt.setString(3, getStreet());
                pstmt.setInt(4, getStreetNumber());
                pstmt.setDouble(5, getSquareArea());
                pstmt.setInt(6, getFkMaklerId());
                pstmt.setInt(7, getId());
                pstmt.executeUpdate();

                isFlag = false;

                pstmt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM estates WHERE id = ?";

        try {
            Connection conn = DBConnectionManager.getInstance().getConnection();

            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, id);

            int affected = preparedStatement.executeUpdate();

            return (affected == 1) ? true : false;

        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    @Override
    public String toString() {
        return "ID : " + getId() + "\n" +
                "City : " + getCity() + "\n" +
                "Postal Code : " + getPostalCode() + "\n" +
                "Street : " + getStreet() + "\n" +
                "Street Number : " + getStreetNumber() + "\n" +
                "Square Number : " + getSquareArea() + "\n" +
                "Estate Agent ID : " + getFkMaklerId();
    }

    public void update(int estateAgentId) {
        setEstateAgentId(estateAgentId);
        setCity(FormUtil.readString("City"));
        setPostalCode(FormUtil.readInt("Postal Code"));
        setStreet(FormUtil.readString("Street"));
        setStreetNumber(FormUtil.readInt("Street Number"));
        setSquareArea(FormUtil.readDouble("Square Area"));
    }


}
