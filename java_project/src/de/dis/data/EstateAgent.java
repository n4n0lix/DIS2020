package de.dis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Makler-Bean
 *
 * Beispiel-Tabelle:
 * CREATE TABLE makler(id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1, NO CACHE) PRIMARY KEY,
 * name varchar(255),
 * address varchar(255),
 * login varchar(40) UNIQUE,
 * password varchar(40));
 */
public class EstateAgent {
	private int id = -1;
	private String name;
	private String address;
	private String login;
	private String password;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Loads a broker from the database
	 * id ID of the broker to load
	 *
	 * Lädt einen Makler aus der Datenbank
	 * @param id ID des zu ladenden Maklers
	 * @return Makler-Instanz
	 */
	public static EstateAgent load(int id) {
		try {
			// Hole Verbindung			Get connected
			Connection con = DBConnectionManager.getInstance().getConnection();

			// Erzeuge Anfrage			Create request
			String selectSQL = "SELECT * FROM estate_agents WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(selectSQL);
			pstmt.setInt(1, id);

			// Führe Anfrage aus			Execute request
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				EstateAgent ts = new EstateAgent();
				ts.setId(id);
				ts.setName(rs.getString("name"));
				ts.setAddress(rs.getString("address"));
				ts.setLogin(rs.getString("login"));
				ts.setPassword(rs.getString("password"));

				rs.close();
				pstmt.close();
				return ts;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Saves the broker in the database.
	 * If no ID has yet been assigned, the generated id is fetched from DB2
	 * and passed to the model.
	 *
	 * Speichert den Makler in der Datenbank. Ist noch keine ID vergeben
	 * worden, wird die generierte Id von DB2 geholt und dem Model übergeben.
	 */
	public void save() {
		// Hole Verbindung
		Connection con = DBConnectionManager.getInstance().getConnection();

		try {
			// FC<ge neues Element hinzu, wenn das Objekt noch keine ID hat.
			// Add a new element if the object does not already have an ID
			if (getId() == -1) {
				// Achtung, hier wird noch ein Parameter mitgegeben,
				// damit spC$ter generierte IDs zurC<ckgeliefert werden!
				// Attention, here a parameter is given,
				// so that later generated IDs are returned!
				String insertSQL = "INSERT INTO estate_agents(name, address, login, password) VALUES (?, ?, ?, ?)";

				PreparedStatement pstmt = con.prepareStatement(insertSQL,
						Statement.RETURN_GENERATED_KEYS);

				// Setze Anfrageparameter und fC<hre Anfrage aus
				// Set request parameters and execute request
				pstmt.setString(1, getName());
				pstmt.setString(2, getAddress());
				pstmt.setString(3, getLogin());
				pstmt.setString(4, getPassword());
				pstmt.executeUpdate();

				// Hole die Id des engefC<gten Datensatzes
				// Get the id of the tight set
				ResultSet rs = pstmt.getGeneratedKeys();
				if (rs.next()) {
					setId(rs.getInt("id"));
				}

				rs.close();
				pstmt.close();
			} else {
				// Falls schon eine ID vorhanden ist, mache ein Update...
				// If an ID already exists, make an update ...
				String updateSQL = "UPDATE estate_agents SET name = ?, address = ?, login = ?, password = ? WHERE id = ?";
				PreparedStatement pstmt = con.prepareStatement(updateSQL);

				// Setze Anfrage Parameter
				// Set request parameters
				pstmt.setString(1, getName());
				pstmt.setString(2, getAddress());
				pstmt.setString(3, getLogin());
				pstmt.setString(4, getPassword());
				pstmt.setInt(5, getId());
				pstmt.executeUpdate();

				pstmt.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static boolean delete(int id) {
		String sql = "DELETE FROM estate_agents WHERE id = ?";

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

	public static EstateAgent login(String login, String password) {

		String sql = "SELECT * FROM estate_agents WHERE login = ?";

		try {
			Connection con = DBConnectionManager.getInstance().getConnection();

			PreparedStatement preparedStatement = con.prepareStatement(sql);
			preparedStatement.setString(1, login);

			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next()) {
				EstateAgent estateAgent = new EstateAgent();

				estateAgent.setId(rs.getInt("id"));
				estateAgent.setName(rs.getString("name"));
				estateAgent.setAddress(rs.getString("address"));
				estateAgent.setLogin(login);
				estateAgent.setPassword(rs.getString("password"));

				rs.close();
				preparedStatement.close();

				if (password.equals(estateAgent.getPassword())) {
					return estateAgent;
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public String toString() {
		return "ID : " + getId() + "\n" +
				"Name : " + getName() + "\n" +
				"Address : " + getAddress() + "\n" +
				"Login : " + getLogin() + "\n" +
				"Password : " + getPassword() + "\n";
	}

}
