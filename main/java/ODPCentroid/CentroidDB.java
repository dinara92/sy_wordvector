package ODPCentroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import org.nd4j.jdbc.loader.impl.BaseLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import com.mysql.jdbc.Blob;


public class CentroidDB{
	



	String driver = "com.mysql.jdbc.Driver";
	String url = "jdbc:mysql://localhost:3306/dmoz2013_s";
	String user = "root"; // @jve:decl-index=0:
	String password = "newpass";

	Connection conn = null;
	Statement stmt = null;

	public boolean connectDB() {

		try { // jdbc µå¶óÀÌ¹ö ·Îµù
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);

		} catch (ClassNotFoundException e) {
			System.err.println("LoginDB1 Class : " + e.getMessage());
			return false;
		} catch (SQLException e) {
			System.err.println("LoginDB2 Class : " + e.getMessage());
			return false;
		}
		return true;
	}

	public void closeDB() throws SQLException {
		if (conn != null) {
			conn.close();
		}

	}

	
	
	
	
	
	//new function, to get all categories info
	public ArrayList<CategoryInfo> getCategoryInfo() throws SQLException{
		 
		ArrayList<CategoryInfo> allCate_speciflevel = new ArrayList<CategoryInfo>();
 
		if(conn == null)
		 connectDB();
		String query = "select * from dmoz_original" + ";";
		stmt = conn.createStatement();

		// execute the query, and get a java resultset
		ResultSet rs = stmt.executeQuery(query);

		// iterate through the java resultset
		while (rs.next()) {
			int id = rs.getInt("ID");
			CategoryInfo tmp = new CategoryInfo(id, rs.getString("NAME"), rs.getString("FULLNAME"),
					rs.getInt("PARENTID"), rs.getInt("LEVEL"), rs.getInt("URLNUM"), rs.getInt("ISLEAF"),
					rs.getInt("CHILDNUM"), rs.getInt("INVERSELEVEL"), rs.getInt("SUBTREEURLNUM"), rs.getInt("SUBTREENUM"));
			allCate_speciflevel.add(tmp);
		}
		stmt.close();
		 
		return allCate_speciflevel;
		
	}
	public ArrayList<CategoryInfo> getCategoryInfo_Level(int level) throws SQLException{
		 
		ArrayList<CategoryInfo> allCate_speciflevel = new ArrayList<CategoryInfo>();
 
		if(conn == null)
		 connectDB();
		String query = "select * from dmoz_original where LEVEL = " + level + 
				" and fullname not like " + "\"%" + "Top/World" + "%\""  + " and fullname not like " + "\"%" + "Top/Regional" + "%\"" + ";";
		stmt = conn.createStatement();

		// execute the query, and get a java resultset
		ResultSet rs = stmt.executeQuery(query);

		// iterate through the java resultset
		while (rs.next()) {
			int id = rs.getInt("ID");
			CategoryInfo tmp = new CategoryInfo(id, rs.getString("NAME"), rs.getString("FULLNAME"),
					rs.getInt("PARENTID"), rs.getInt("LEVEL"), rs.getInt("URLNUM"), rs.getInt("ISLEAF"),
					rs.getInt("CHILDNUM"), rs.getInt("INVERSELEVEL"), rs.getInt("SUBTREEURLNUM"), rs.getInt("SUBTREENUM"));
			allCate_speciflevel.add(tmp);
		}
		stmt.close();
		 
		return allCate_speciflevel;
		
	}
	//new function, to get all categories info as a HashMap
	public HashMap<Integer, CategoryInfo> getCategoryInfoLevel(int level) throws SQLException{
		 
		HashMap<Integer, CategoryInfo> allCate_speciflevel = new HashMap<Integer, CategoryInfo>();
 
		if(conn == null)
		 connectDB();
		String query = "select * from dmoz_original where level = " + level + 
				" and fullname not like " + "\"%" + "Top/World" + "%\""  + " and fullname not like " + "\"%" + "Top/Regional" + "%\"" + ";";
		stmt = conn.createStatement();

		// execute the query, and get a java resultset
		ResultSet rs = stmt.executeQuery(query);

		// iterate through the java resultset
		while (rs.next()) {
			int id = rs.getInt("ID");
			CategoryInfo tmp = new CategoryInfo(id, rs.getString("NAME"), rs.getString("FULLNAME"),
					rs.getInt("PARENTID"), rs.getInt("LEVEL"), rs.getInt("URLNUM"), rs.getInt("ISLEAF"),
					rs.getInt("CHILDNUM"), rs.getInt("INVERSELEVEL"), rs.getInt("SUBTREEURLNUM"), rs.getInt("SUBTREENUM"));
			allCate_speciflevel.put(id, tmp);
		}
		stmt.close();
		 
		return allCate_speciflevel;
		
	}
	
	//new function, to get all categories info as a HashMap
	public INDArray getChildMCInfo(int categoryid) throws SQLException{
		  
		if(conn == null)
		 connectDB();
		String query = "select * from mcs_info_after_bug_fix where id = " + categoryid + ";";
		stmt = conn.createStatement();

		// execute the query, and get a java resultset
		ResultSet rs = stmt.executeQuery(query);
		INDArray mc_array = null;
		// iterate through the java resultset
		while (rs.next()) {
			int id = rs.getInt("id");
			String mc = rs.getString("mc_value");
			//mc_array = here need to parse string
		}
		stmt.close();
		 
		return mc_array;
		
	}
	
	public void storeChildMCInfo (Integer parent_catid, INDArray parentMC) throws SQLException, IOException{
			
		//String parentMC_string = parentMC.toString();
		//System.out.println(parentMC_string);

		//float[] parentMC_float = parentMC;
			//Nd4j.create(parentMC_float);
		
		/*if(conn == null)
			 connectDB();	
		PreparedStatement statement_save;			        		
		String query = "INSERT INTO mcs_info_after_bug_fix (categoryid, mc_value)"
        		        + " values (?, ?);";
		statement_save = (PreparedStatement) conn.prepareStatement(query);
		statement_save.setInt(1, parent_catid);
		statement_save.setString(2, parentMC_string);
		statement_save.setObject(2, parentMC);
		statement_save.executeUpdate();*/
		
		

		}

	public HashMap<Integer, CategoryInfo> getHeruisticCategoryInfo(ArrayList<String> HueristicCategory) throws IOException, SQLException {
		HashMap<Integer, CategoryInfo> HueristicInfo = new HashMap<Integer, CategoryInfo>();
		
		for (int i = 0; i < HueristicCategory.size(); i++) {
			String fullpath = HueristicCategory.get(i);
			String query = "select * from dmoz_original where FULLNAME = " + fullpath + ");";
			stmt = conn.createStatement();

			// execute the query, and get a java resultset
			ResultSet rs = stmt.executeQuery(query);

			// iterate through the java resultset
			while (rs.next()) {
				int id = rs.getInt("ID");
				CategoryInfo tmp = new CategoryInfo(id, rs.getString("NAME"), rs.getString("FULLNAME"),
						rs.getInt("PARENTID"), rs.getInt("LEVEL"), rs.getInt("URLNUM"), rs.getInt("ISLEAF"),
						rs.getInt("CHILDNUM"), rs.getInt("INVERSELEVEL"), rs.getInt("SUBTREEURLNUM"), rs.getInt("SUBTREENUM"));


				HueristicInfo.put(id, tmp);
			}
			stmt.close();
		}
		return HueristicInfo;
	}

	public ArrayList<Document> LoadDocumentInfo(int categoryId) throws SQLException {
		ArrayList<Document> result = new ArrayList<Document>();
		// TODO Auto-generated method stub
		String query = "select * from dmoz_docs where CATEGORYID = " + categoryId + ";";
		stmt = conn.createStatement();

		// execute the query, and get a java resultset
		ResultSet rs = stmt.executeQuery(query);

		// iterate through the java resultset
		while (rs.next()) {
			 
			int id = rs.getInt("URLID");
			categoryId = rs.getInt("CATEGORYID");
			String url = rs.getString("URL");
			String title = rs.getString("TITLE");
			String description = rs.getString("DESCRIPTION");

			Document doc = new Document(id, categoryId, url, title, description);
			result.add(doc);
		}
		stmt.close();
		return result;
	} 
	
	public HashMap<String, Integer> LoadTermInfo() throws SQLException {
		if(conn == null)
			connectDB();
		
		System.out.println(stmt + "  " + conn);
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		// TODO Auto-generated method stub
		String query = "select * from dictionary;";
		stmt = conn.createStatement();

		// execute the query, and get a java resultset
		ResultSet rs = stmt.executeQuery(query);

		// iterate through the java resultset
		while (rs.next()) {
			String term = rs.getString("term");
			int df = rs.getInt("df");
			result.put(term, df);
		}
		stmt.close();
		closeDB();
		conn = null;
		return result;
	} 
	
	public void storeKNNResult(HashMap<Integer, CategoryInfo> keywordsInfo) throws SQLException {
		// TODO Auto-generated method stub
		ArrayList<Integer> categoryIds = new ArrayList<Integer>(keywordsInfo.keySet());
		for(int i = 0; i < categoryIds.size(); i++){
			storeKNNResult(keywordsInfo.get(categoryIds.get(i)));
		} 
	}

	public void storeKNNResult(CategoryInfo curCate) throws SQLException {
		// TODO Auto-generated method stub
		String query = " insert into test_input_dl4j_level5 (CategoryId, KeywordList, Fullpath)"
		        + " values (?, ?, ?)";
		      PreparedStatement preparedStmt = conn.prepareStatement(query);
		      preparedStmt.setInt(1, curCate.categoryid);
		      preparedStmt.setString (2, getKeywords(curCate.KnnWordsList));
		      preparedStmt.setString (3, curCate.fullpath); 
		      preparedStmt.execute();
	} 

	private String getKeywords(Collection<String> knnWordsList) {
		// TODO Auto-generated method stub
		String result = "";
		
	    Iterator<String> itr = knnWordsList.iterator();
	    while(itr.hasNext()){
	    	result += itr.next() + " ";
	    }
	
		return result;
	}


	

}
