package de.dis.data;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.dis.FormUtil;

public class Contract {
    private int id = -1;
    private Date contractDate;
    private String place;
    private int personId;
    protected Boolean isFlag = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getContractDate() {
        return contractDate;
    }

    public void setContractDate(Date contractDate) {
        this.contractDate = contractDate;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public static Contract load(int id) {
        try {
            Connection con = DBConnectionManager.getInstance().getConnection();

            String selectSQL = "SELECT * FROM contracts WHERE id = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {

                Contract contract = new Contract();
                contract.setId(id);
                contract.setContractDate(rs.getDate("contract_date"));
                contract.setPlace(rs.getString("place"));
                contract.setPersonId(rs.getInt("fk_person_id"));

                rs.close();
                pstmt.close();
                return contract;
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
                String sql = "INSERT INTO contracts(contract_date, place, fk_person_id) VALUES (?, ?, ?)";

                PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                pstmt.setDate(1, getContractDate());
                pstmt.setString(2, getPlace());
                pstmt.setInt(3, getPersonId());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();

                if (rs.next()) {
                    setId(rs.getInt(1));
                }

                isFlag = true;

                rs.close();
                pstmt.close();
            } else {
                String updateSQL = "UPDATE contracts SET contract_date = ?, place = ?, fk_person_id = ? WHERE id = ?";
                PreparedStatement pstmt = con.prepareStatement(updateSQL);

                pstmt.setDate(1, getContractDate());
                pstmt.setString(2, getPlace());
                pstmt.setInt(3, getPersonId());

                isFlag = false;

                pstmt.executeUpdate();
                pstmt.close();

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean delete (int id) {
        String sql = "DELETE FROM contracts WHERE id = ?";

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
        return "Contract date: " + getContractDate() + "/n" +
                "Place: " + getPlace() + "/n" +
                "Person ID: " + getPersonId();
    }


    public void update() {
        setContractDate(FormUtil.readDate("Date"));
        setPlace(FormUtil.readString("Place"));
        setPersonId(FormUtil.readInt("Person id"));
    }
}
