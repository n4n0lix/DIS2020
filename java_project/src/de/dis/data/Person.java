package de.dis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.dis.FormUtil;

public class Person {
    private int id = -1;
    private String firstName;
    private String lastName;
    private String address;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static Person load(int id) {
        try {
            Connection con = DBConnectionManager.getInstance().getConnection();

            String selectSQL = "SELECT * FROM persons WHERE id = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {

                Person pers = new Person();
                pers.setId(id);
                pers.setFirstName(rs.getString("first_name"));
                pers.setLastName(rs.getString("last_name"));
                pers.setAddress(rs.getString("address"));

                rs.close();
                pstmt.close();
                return pers;
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
                String sql = "INSERT INTO persons(first_name, last_name, address) VALUES (?, ?, ?)";

                PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                pstmt.setString(1, getFirstName());
                pstmt.setString(2, getLastName());
                pstmt.setString(3, getAddress());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();

                if (rs.next()) {
                    setId(rs.getInt(1));
                }

                rs.close();
                pstmt.close();
            } else {
                String updateSQL = "UPDATE persons SET first_name = ?, last_name = ?, address= ? WHERE id = ?";
                PreparedStatement pstmt = con.prepareStatement(updateSQL);

                pstmt.setString(1, getFirstName());
                pstmt.setString(2, getAddress());
                pstmt.setString(3, getAddress());

                pstmt.setInt(4, getId());

                pstmt.executeUpdate();
                pstmt.close();

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean delete (int id) {
        String sql = "DELETE FROM persons WHERE id = ?";

        try {
            Connection con = DBConnectionManager.getInstance().getConnection();

            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, id);

            int affected = pstmt.executeUpdate();

            return (affected == 1) ? true : false;

        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    @Override
    public String toString() {
        return "First name: " + getFirstName() + "/n" +
                "Last name: " + getLastName() + "/n" +
                "Address: " + getAddress();
    }


    public void update() {
        setFirstName(FormUtil.readString("First Name"));
        setLastName(FormUtil.readString("Last Name"));
        setAddress(FormUtil.readString("Address"));
    }


}
