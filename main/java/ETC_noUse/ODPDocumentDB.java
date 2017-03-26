package ETC_noUse;
//package ODPCentroid;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//
//public class ODPDocumentDB {
//
//	String driver = "com.mysql.jdbc.Driver";
//	String url = "jdbc:mysql://localhost:3306/dmoz2013";
//	String user = "root"; // @jve:decl-index=0:
//	String password = "thduddl";
//
//
//	Connection conn = null;
//	Statement stmt = null;
//
//	public boolean connectDB() {
//
//		try { // jdbc µå¶óÀÌ¹ö ·Îµù
//			Class.forName(driver);
//			conn = DriverManager.getConnection(url, user, password);
//
//		} catch (ClassNotFoundException e) {
//			System.err.println("LoginDB Class : " + e.getMessage());
//			return false;
//		} catch (SQLException e) {
//			System.err.println("LoginDB Class : " + e.getMessage());
//			return false;
//		}
//		return true;
//	}
//
//	public void closeDB() throws SQLException {
//		if (conn != null) {
//			conn.close();
//		}
//
//	}
//	
//	
//}
