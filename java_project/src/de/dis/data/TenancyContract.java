package de.dis.data;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;

import de.dis.FormUtil;

public class TenancyContract extends Contract {

    private Date startDate;
    private Time duration;
    private double additionalCost;
    private int apartmentId;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Time getDuration() {
        return duration;
    }

    public void setDuration(Time duration) {
        this.duration = duration;
    }

    public double getAdditionalCost() {
        return additionalCost;
    }

    public void setAdditionalCost(double additionalCost) {
        this.additionalCost = additionalCost;
    }

    public int getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(int apartmentId) {
        this.apartmentId = apartmentId;
    }

    public static TenancyContract load(int id) {
        Contract contract = Contract.load(id);

        try {
            Connection con = DBConnectionManager.getInstance().getConnection();

            String selectSQL = "SELECT * FROM tenancy_contracts WHERE fk_contract_id = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                TenancyContract tenancy = new TenancyContract();

                tenancy.setId(id);
                tenancy.setContractDate(contract.getContractDate());
                tenancy.setPlace(contract.getPlace());
                tenancy.setPersonId(contract.getPersonId());

                tenancy.setStartDate(rs.getDate("start_date"));
                tenancy.setDuration(rs.getTime("duration"));
                tenancy.setAdditionalCost(rs.getDouble("additional_cost"));
                tenancy.setApartmentId(rs.getInt("fk_apartment_id"));

                rs.close();
                pstmt.close();
                return tenancy;
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
                String insertSQL = "INSERT INTO tenancy_contracts(fk_contract_id, start_date, duration, additional_cost, fk_apartment_id) VALUES (?, ?, ?, ?, ?)";

                PreparedStatement pstmt = con.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);

                pstmt.setInt(1, getId());
                pstmt.setDate(2, getStartDate());
                pstmt.setTime(3, getDuration());
                pstmt.setDouble(4, getAdditionalCost());
                pstmt.setInt(5, getApartmentId());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    setId(rs.getInt(1));
                }

                isFlag = false;

                rs.close();
                pstmt.close();
            } else {
                String updateSQL = "UPDATE tenancy_contracts SET start_date = ?, duration = ?, additional_cost = ?, fk_apartment_id = ? WHERE fk_contract_id = ?";
                PreparedStatement pstmt = con.prepareStatement(updateSQL);

                pstmt.setInt(1, getId());
                pstmt.setDate(2, getStartDate());
                pstmt.setTime(3, getDuration());
                pstmt.setDouble(4, getAdditionalCost());
                pstmt.setInt(5, getApartmentId());
                pstmt.executeUpdate();

                isFlag = true;

                pstmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static boolean delete(int id) {
        Connection con = DBConnectionManager.getInstance().getConnection();

        try {
            String selectSQL = "DELETE FROM tenancy_contracts WHERE fk_contract_id = ?";

            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, id);
            pstmt.execute();
            pstmt.close();
            return Contract.delete(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    public void update() {
        super.update();
        setStartDate(FormUtil.readDate("StartDate"));
        setDuration(FormUtil.readTime("Duration"));
        setAdditionalCost(FormUtil.readDouble("AdditionalCosts"));
        setApartmentId(FormUtil.readInt("ApartmentId"));
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString() + "/n" +
                "Start Date: " + getStartDate() + "/n" +
                "Duration: " + getDuration() + "/n" +
                "Additional Cost: " + getAdditionalCost() + "/n" +
                "Apartment ID: " + getApartmentId();
    }


    public static void print() {
        try {
            Connection con = DBConnectionManager.getInstance().getConnection();

            String selectSQL = "SELECT * FROM tenancy_contracts";
            PreparedStatement preparedStatement = con.prepareStatement(selectSQL);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                TenancyContract tenancy = new TenancyContract();

                tenancy.setId(rs.getInt("fk_contract_id"));

                Contract contract = Contract.load(tenancy.getId());
                if (contract == null) throw new SQLException("Please check information");
                tenancy.setContractDate(contract.getContractDate());
                tenancy.setPlace(contract.getPlace());
                tenancy.setPersonId(contract.getPersonId());

                tenancy.setStartDate(rs.getDate("start_date"));
                tenancy.setDuration(rs.getTime("duration"));
                tenancy.setAdditionalCost(rs.getDouble("additional_cost"));
                tenancy.setApartmentId(rs.getInt("fk_apartment_id"));

                System.out.println(tenancy.toString());

                System.out.println();
            }

            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

