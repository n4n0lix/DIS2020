package de.dis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.dis.FormUtil;

public class PurchaseContract extends Contract {
    private int numberOfInstallment;
    private double interestRate;
    private int houseId;

    public int getNumberOfInstallment() {
        return numberOfInstallment;
    }

    public void setNumberOfInstallment(int numberOfInstallment) {
        this.numberOfInstallment = numberOfInstallment;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public int getHouseId() {
        return houseId;
    }

    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    public static PurchaseContract load(int id) {
        Contract contract = Contract.load(id);

        try {
            Connection con = DBConnectionManager.getInstance().getConnection();

            String selectSQL = "SELECT * FROM purchase_contracts WHERE fk_contract_id = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                PurchaseContract purchase = new PurchaseContract();

                purchase.setId(id);
                purchase.setContractDate(contract.getContractDate());
                purchase.setPlace(contract.getPlace());
                purchase.setPersonId(contract.getPersonId());
                purchase.setNumberOfInstallment(rs.getInt("number_of_installment"));
                purchase.setInterestRate(rs.getDouble("interest_rate"));
                purchase.setHouseId(rs.getInt("fk_house_id"));

                rs.close();
                pstmt.close();
                return purchase;
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
                String insertSQL = "INSERT INTO purchase_contracts(fk_contract_id, number_of_installment, interest_rate, fk_house_id) VALUES (?, ?, ?, ?)";

                PreparedStatement pstmt = con.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);

                pstmt.setInt(1, getId());
                pstmt.setInt(2, getNumberOfInstallment());
                pstmt.setDouble(3, getInterestRate());
                pstmt.setInt(4, getHouseId());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    setId(rs.getInt(1));
                }

                isFlag = false;

                rs.close();
                pstmt.close();
            } else {
                String updateSQL = "UPDATE purchase_contracts SET number_of_installment = ?, interest_rate = ?, fk_house_id = ? WHERE fk_contract_id = ?";
                PreparedStatement pstmt = con.prepareStatement(updateSQL);

                pstmt.setInt(1, getNumberOfInstallment());
                pstmt.setDouble(2, getInterestRate());
                pstmt.setInt(3, getHouseId());
                pstmt.setInt(4, getId());
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
            String selectSQL = "DELETE FROM purchase_contracts WHERE contract_id = ?";

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
        setNumberOfInstallment(FormUtil.readInt("NumberOfInstallments"));
        setInterestRate(FormUtil.readDouble("InterestRate"));
        setHouseId(FormUtil.readInt("HouseId"));
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString() + "/n" +
                "Number of installment: " + getNumberOfInstallment() + "/n" +
                "Interest rate: " + getInterestRate() + "/n" +
                "House ID: " + getHouseId();
    }


    public static void print() {
        try {
            Connection con = DBConnectionManager.getInstance().getConnection();

            String selectSQL = "SELECT * FROM purchase_contracts";
            PreparedStatement preparedStatement = con.prepareStatement(selectSQL);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                PurchaseContract purchase = new PurchaseContract();

                purchase.setId(rs.getInt("fk_contract_id"));

                Contract contract = Contract.load(purchase.getId());
                if (contract == null) throw new SQLException("Please check information");
                purchase.setContractDate(contract.getContractDate());
                purchase.setPlace(contract.getPlace());
                purchase.setPersonId(contract.getPersonId());

                purchase.setNumberOfInstallment(rs.getInt("number_of_installment"));
                purchase.setInterestRate(rs.getDouble("interest_rate"));
                purchase.setHouseId(rs.getInt("fk_house_id"));

                System.out.println(purchase.toString());

                System.out.println();
            }

            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
