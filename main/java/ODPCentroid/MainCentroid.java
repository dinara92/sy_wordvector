package ODPCentroid;

import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.jdbc.mysql.MysqlLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import ETC_noUse.word2vec;
import Main.StaticValues;

public class MainCentroid {

	static CentroidDB ODP_DB = null;
	static WeightLookupTable<VocabWord> words = null;
	ArrayList<String> HeuristicCategory_path = null;
	HashMap<String, Integer> termDF = null;

	public MainCentroid() throws Exception {
		
		//loadINDArrayFromDBNoParam();
		//saveINDArrayToDBNoParam();
		if (words == null)
			words = StaticValues.vec.getLookupTable();

		//INDArray centroid = loadINDArrayFromDB(31213);
		//Collection<String> KnnWordsList = StaticValues.vec.wordsNearest(centroid, 20);
		//System.out.println(KnnWordsList);
		
		init();
		start();
		//runCentroid();
	}

	public void init() throws Exception {
		if (ODP_DB == null) {
			ODP_DB = new CentroidDB();
			ODP_DB.connectDB();
			System.out.println("Connected to db..");
		}
		System.out.println("starting to load dictionary from db");
		termDF = ODP_DB.LoadTermInfo();
	}
	
	public void runCentroid() throws SQLException, IOException{
		
		HashMap <Integer, INDArray> centroidMap = new HashMap<Integer, INDArray>();
		
		//for (int i = 14; i > 0; i--) {
		HashMap<Integer, CategoryInfo> Heuri_Info_MCC = getHuri_specificLevel_MCC(5);
		System.out.println("Level " + 5 + " " + Heuri_Info_MCC.size());
		

		ArrayList<Integer> categoryIds = new ArrayList<Integer>(Heuri_Info_MCC.keySet());
		for (int i = 0; i < categoryIds.size(); i++) {
			int curID = categoryIds.get(i);
			CategoryInfo curCate = Heuri_Info_MCC.get(curID);

			INDArray centroid = getCategoryCentroid(curCate);
			centroidMap.put(curID, centroid);
		}
		
		//}
	}
	public void start() {
		// 12.20 i몇번째부터인지 보기.
		//for (int i = 14; i > 0; i--) {
			//HashMap<Integer, CategoryInfo> Heuri_Info_MCC = getHuri_specificLevel_MCC(5);
			//System.out.println("Level " + 5 + " " + Heuri_Info_MCC.size());
			//get_store_NearestWords(Heuri_Info_MCC);
		//}
		
		//HashMap<Integer, CategoryInfo> Heuri_Info_MCC = getHuri_specificLevel_MCC(5);
		//System.out.println(5 + " " + Heuri_Info_MCC.size());
		///check_Heuri_Info_MCC(Heuri_Info_MCC);
		//System.out.println("starting to build category map");
		//List<CategoryInfo> allCategInfoMap = getAllCategoriesNewList();
		//System.out.println("All taxonomy size is " + allCategInfoMap.size());
		
		//if (words == null)
			//words = StaticValues.vec.getLookupTable();

		//makeMCMap(allCategInfoMap, Heuri_Info_MCC);
		
		
		//----------------------
		for(int i = 7; i > 0; i--){
			System.out.println("-------------------LEVEL------------------- : " + i);
			HashMap<Integer, CategoryInfo> parentInfo = null;
			try {
				parentInfo = getCategoriesNewListByLevel(i, i + 1);
				
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //max level : 14
			INDArray parentMC;
			
	 ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setJdbcUrl("jdbc:mysql://localhost:3306/dmoz2016?user=root&password=newpass");
        MysqlLoader loader = null;
        try {
			ds.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        try {
			loader = new MysqlLoader(ds, "jdbc:mysql://localhost:3306/dmoz2016?user=root&password=newpass", "mcs_info", "mc_value");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        try{
 
			for(Integer parent_catid: parentInfo.keySet()){
				parentMC = getCategoryMCfromDB(parentInfo.get(parent_catid), loader);
				try {
						saveINDArrayToDB(parent_catid, parentMC, loader);
						//System.out.println("This is in db " + parent_catid + " mc: " + parentMC);
					//}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Not this catid " + parent_catid + " with this mc : " + parentMC);
				}	//store parent merge centroid to db
				
			}
        }
        finally{
        	ds.close();
        }
		}

	}

	public void saveINDArrayToDB(Integer parent_catid, INDArray parentMC, MysqlLoader loader) throws Exception{
        //ComboPooledDataSource ds = new ComboPooledDataSource();
        //ds.setJdbcUrl("jdbc:mysql://localhost:3306/dmoz2013_s?user=root&password=newpass");
        //ds.setDriverClass("com.mysql.jdbc.Driver");
        //MysqlLoader loader = new MysqlLoader(ds, "jdbc:mysql://localhost:3306/dmoz2013_s?user=root&password=newpass", "mcs_info", "mc_value");
        //try{
	        //loader.save(Nd4j.create(new float[]{0,2,3,4}), "30");
	        loader.save(parentMC, parent_catid.toString());
	        loader.insertStatement();
        //}
        //finally{
        //	ds.close();
        //}
	}

	public void saveINDArrayToDBNoParam() throws Exception{
        ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setJdbcUrl("jdbc:mysql://localhost:3306/dmoz2013_s?user=root&password=newpass");
        ds.setDriverClass("com.mysql.jdbc.Driver");
        MysqlLoader loader = new MysqlLoader(ds, "jdbc:mysql://localhost:3306/dmoz2013_s?user=root&password=newpass", "mcs_info_after_bug_fix", "mc_value");
        loader.save(Nd4j.create(new double[]{-0.00, -0.32, 0.04, 0.33, -0.11, -0.19, 0.07, 0.02, -0.16, 0.16, -0.06, -0.00, -0.03, 0.17, 0.14, -0.08, 0.13, -0.01, 0.03, -0.22, -0.01, 0.04, 0.06, 0.24, -0.19, -0.10, 0.03, 0.17, -0.22, 0.01, 0.10, -0.03, 0.15, -0.30, 0.11, -0.05, -0.03, -0.04, 0.16, 0.20, 0.23, -0.22, -0.08, 0.18, -0.02, 0.00, -0.07, -0.22, 0.16, 0.25, -0.23, -0.14, -0.11, -0.16, 0.01, -0.10, -0.09, 0.05, -0.02, -0.07, -0.30, -0.14, 0.08, 0.07, -0.11, -0.18, 0.19, -0.04, -0.12, 0.33, -0.06, 0.12, 0.11, 0.04, -0.38, -0.14, 0.11, 0.21, -0.21, 0.00, -0.17, -0.35, 0.13, -0.04, -0.09, 0.24, -0.12, 0.26, -0.01, -0.02, -0.24, -0.30, 0.17, 0.09, -0.03, 0.10, 0.26, -0.05, 0.02, -0.07, 0.07, 0.14, -0.06, -0.01, -0.05, 0.18, -0.02, 0.05, -0.07, -0.00, 0.05, -0.12, 0.25, 0.01, 0.04, 0.23, -0.20, -0.33, 0.00, 0.15, -0.22, -0.07, -0.08, 0.08, 0.22, -0.20, 0.03, -0.00, -0.12, 0.31, 0.13, 0.25, -0.03, 0.12, -0.18, -0.08, -0.07, 0.06, -0.07, 0.28, -0.20, -0.28, 0.03, -0.08, -0.19, 0.34, 0.05, 0.12, 0.06, 0.01, 0.12, 0.03, -0.03, 0.08, -0.05, -0.07, 0.06, 0.06, -0.23, -0.02, 0.01, 0.29, 0.13, -0.07, -0.04, -0.18, 0.09, 0.13, -0.07, 0.22, -0.05, 0.06, 0.01, -0.26, 0.04, -0.19, 0.02, -0.11, -0.29, 0.03, 0.03, -0.19, 0.18, -0.11, 0.04, -0.01, -0.00, -0.11, -0.02, 0.06, 0.08, -0.09, 0.05, 0.01, 0.06, -0.20, 0.12, 0.00, -0.04, 0.07, -0.01, 0.06, 0.06, -0.04, 0.11, -0.15, 0.18, -0.06, -0.26, 0.31, 0.11, -0.08, -0.16, -0.07, -0.20, 0.13, -0.01, 0.16, 0.22, -0.11, -0.19, 0.08, -0.03, 0.07, 0.00, -0.05, 0.13, 0.12, -0.18, -0.11, -0.19, 0.33, -0.19, -0.06, 0.19, 0.12, 0.29, 0.25, -0.14, -0.06, 0.08, 0.15, 0.01, -0.12, 0.06, -0.03, -0.32, 0.11, -0.29, -0.39, 0.01, 0.26, -0.19, 0.05, 0.03, -0.19, 0.21, -0.04, 0.04, -0.22, 0.19, 0.17, -0.05, -0.12, 0.12, 0.00, 0.23, 0.01, -0.06, 0.21, -0.04, -0.25, -0.11, 0.02, 0.13, 0.06, 0.04, -0.01, -0.18, 0.25, -0.13, -0.10, 0.23, 0.14, 0.02, 0.05, 0.26, 0.18, 0.26, 0.07, 0.08, 0.28, -0.26, 0.08, -0.03, 0.09, -0.23, 0.26, 0.01, 0.22}), "30");
        loader.insertStatement();
	}
	public INDArray loadINDArrayFromDB(Integer id, MysqlLoader loader) throws Exception{
		//ComboPooledDataSource ds = new ComboPooledDataSource();
	    //ds.setJdbcUrl("jdbc:mysql://localhost:3306/dmoz2013_s?user=root&password=newpass");
	    //MysqlLoader loader = new MysqlLoader(ds, "jdbc:mysql://localhost:3306/dmoz2013_s?user=root&password=newpass", "mcs_info", "mc_value");
	    INDArray loaded;
	    //try{
			Blob b = loader.loadForID(id.toString());
	        loaded = loader.load(b);
	        //System.out.println("Loaded " + loaded);
	    //}
	    //finally{
	    //	ds.close();
	    //}
        return loaded;
	}
	
	public void loadINDArrayFromDBNoParam() throws Exception{
		ComboPooledDataSource ds = new ComboPooledDataSource();
	    ds.setJdbcUrl("jdbc:mysql://localhost:3306/dmoz2013_s?user=root&password=newpass");
	    MysqlLoader loader = new MysqlLoader(ds, "jdbc:mysql://localhost:3306/dmoz2013_s?user=root&password=newpass", "mcs_info_after_bug_fix", "mc_value");
		Blob b = loader.loadForID("30");
        INDArray loaded = loader.load(b);

        if(Nd4j.create(new float[]{1, 6, 5}).equals(loaded))
        	System.out.println("Equal" + loaded);
        
        System.out.println(loaded);
	}
		
	public void heuriInfoMCC_All() throws SQLException, IOException{
		
		HashMap<Integer, CategoryInfo> Heuri_Info_MCC = null;
		HashMap<Integer, CategoryInfo> Heuri_Info_MCC_All = new HashMap<Integer, CategoryInfo>();

		//int num_of_categs = 0;
		for (int i = 14; i > 0; i--) {
			Heuri_Info_MCC = getHuri_specificLevel_MCC_All(i, Heuri_Info_MCC_All);
			System.out.println("level " + i + " , size: " + Heuri_Info_MCC.size());
			//num_of_categs+=Heuri_Info_MCC.size();
		}
		//System.out.println("Number of categs " + num_of_categs);
		//System.out.println("All size " + Heuri_Info_MCC.size());

	}
	
	public void check_Heuri_Info_MCC(HashMap<Integer, CategoryInfo> Heuri_Info_MCC){
		for(Integer catid : Heuri_Info_MCC.keySet()){
			System.out.println("Catid " + catid + ", inside: " + Heuri_Info_MCC.get(catid).categoryid + " " + Heuri_Info_MCC.get(catid).fullpath);
			System.out.println("\tChildList: ");
			for(CategoryInfo child : Heuri_Info_MCC.get(catid).childCategory)
				System.out.println("child " + child.categoryid);
		}
	}
	
	private void get_store_NearestWords(HashMap<Integer, CategoryInfo> heuri_Info_MCC) throws SQLException {
		// TODO Auto-generated method stub
		if (words == null)
			words = StaticValues.vec.getLookupTable();

		ArrayList<Integer> categoryIds = new ArrayList<Integer>(heuri_Info_MCC.keySet());
		for (int i = 0; i < categoryIds.size(); i++) {
			int curID = categoryIds.get(i);
			CategoryInfo curCate = heuri_Info_MCC.get(curID);

			//INDArray centroid = getCategoryCentroid_MCC(curCate);
			//INDArray centroid = getCategoryCentroid(curCate, 1);
			INDArray centroid = getCategoryCentroid(curCate);

			//getCategoryMC - then use this map to get mc's
			if(centroid == null) continue;
			Collection<String> KnnWordsList = StaticValues.vec.wordsNearest(centroid, 1000);
			// Knn기반 단어 정보 저장.
			curCate.KnnWordsList = KnnWordsList;
			ODP_DB.storeKNNResult(curCate); 
			
		}
		
	}

	private void get_store_NearestWords_MC(Integer mc_catid, HashMap<Integer, CategoryInfo> heuri_Info_MCC, INDArray mc) throws SQLException {

		if(heuri_Info_MCC.containsKey(mc_catid)){
			CategoryInfo curCate = heuri_Info_MCC.get(mc_catid);
			//getCategoryMC - then use this map to get mc's
			//if(centroid == null) continue;
			Collection<String> KnnWordsList = StaticValues.vec.wordsNearest(mc, 1000);
			// Knn기반 단어 정보 저장.
			curCate.KnnWordsList = KnnWordsList;
			ODP_DB.storeKNNResult(curCate); 
			//System.out.println("MC itself: " + mc);
			System.out.println("\tNow in db" + mc_catid);
		}
	}
	
	private void get_store_NearestWords_MC_afterMap(HashMap<Integer, CategoryInfo> heuri_Info_MCC, Map<Integer, INDArray> mergeCentroids) throws SQLException {

		for(Integer catid : heuri_Info_MCC.keySet()){
			CategoryInfo curCate = heuri_Info_MCC.get(catid);
			Collection<String> KnnWordsList = StaticValues.vec.wordsNearest(mergeCentroids.get(catid), 1000);
			// Knn기반 단어 정보 저장.
			curCate.KnnWordsList = KnnWordsList;
			ODP_DB.storeKNNResult(curCate); 
			System.out.println("\tNow in db" + catid);
		}
	}
	
	private INDArray getCategoryCentroid_MCC(CategoryInfo category) { //this function makes centroid + child centroids
		// TODO Auto-generated method stub

		INDArray result = null;

		ArrayList<CategoryInfo> childs = category.childCategory;
		
		// child vector들의 centroid값을 다 더한다 // Add the centroid values of the child vectors
		for (int i = 0; i < childs.size(); i++) {

			//INDArray tmp = getCategoryCentroid(childs.get(i), 1);
			INDArray tmp = getCategoryCentroid(childs.get(i));

			if(tmp == null) continue;
			
			if (result == null)
				result = tmp;
			else
				result.add(tmp);
		}

		INDArray tmp = getCategoryCentroid(category);
		//INDArray tmp = getCategoryCentroid(category, 1);

		// centroid vector값을 child의 갯수로 나눈다.  // Divide the centroid vector value by the number of children

		if(result != null){
			result.div(childs.size());
			result.div(2);
			if(tmp != null)
				result.add(tmp);
			result.div(1.5);
		}
		 
		else{
			if(tmp != null)
				result = tmp;
		}
		return result;
	}
	
	
	// 각 카테고리의 centroid
	private INDArray getCategoryCentroid(CategoryInfo category) { //this function is to make one whole centroid
	//private INDArray getCategoryCentroid(CategoryInfo category, double weight) { 

		// TODO Auto-generated method stub

		INDArray result = null;
		ArrayList<CategoryInfo> childList = category.childCategory;
		//for(CategoryInfo cat : childList){
			//System.out.println("WRONG: Category " + category.categoryid + " vot tebe child: " + cat.categoryid);
			//System.out.println("Category " + category.parentid + " vot tebe child: " + cat.categoryid);
		//}
		
		//System.out.println("Category docs number " + category.documentInfo.size());
		for (int i = 0; i < category.documentInfo.size(); i++) {
			Document doc = category.documentInfo.get(i);
			//System.out.println("for categ " + category.categoryid + "Doc title and desc: " + doc.title + doc.description);
			
			INDArray tmp = getCentroid(doc.title + " " + doc.description, 1);
			//System.out.println("\ttmp doc " + i + " for " + category.categoryid + " is: " + tmp);
			//if (tmp == null){
				//System.out.println("tmp is null!");
			//}
			if (result == null) {
				result = tmp;
			}

			else {
				if(tmp!=null){
					if(!tmp.toString().contains("∞") && !tmp.toString().contains("NaN")){
						result = result.add(tmp);
					}
				}
				else{
					continue;
				}
			}
		}
		if(result == null){
			result = Nd4j.zeros(300);
			//System.out.println("getCategoryCentroid for category " + category.categoryid + " init 300 zeros..");
			//return null;
			return result;
		}
		
		result = result.div(category.documentInfo.size());

		return result;
	}
	
	
	private INDArray getCategoryCentroidNew(CategoryInfo category) {

		INDArray result = null;
		ArrayList<CategoryInfo> childList = category.childCategory;

		//System.out.println("Category docs number " + category.documentInfo.size());
		for (int i = 0; i < category.documentInfo.size(); i++) {
			Document doc = category.documentInfo.get(i);
			//System.out.println("for categ " + category.categoryid + "Doc title and desc: " + doc.title + doc.description + doc.urlid);
			
			INDArray tmp = getSentenceVec(doc.title + " " + doc.description);
			//System.out.println("\ttmp doc " + i + " for " + category.categoryid + " is: " + tmp);
			if (tmp == null){
				System.out.println("tmp is null!");
			}
			if (result == null) {
				result = tmp;
			}

			else {
				if(tmp!=null){
					//if(!tmp.toString().contains("∞") && !tmp.toString().contains("NaN")){
						result = result.add(tmp);
					//}
				}
				else{
					continue;
				}
			}
		}
		if(result == null){
			result = Nd4j.zeros(300);
			System.out.println("getCategoryCentroid for category " + category.categoryid + " init 300 zeros..");
			return result;
		}
		
		result = result.div(category.documentInfo.size());
		//System.out.println("Centroid " + result);
		return result;
	}
	
	private INDArray getCategoryMCfromDB(CategoryInfo parentCategoryInfo, MysqlLoader loader) { 
		//this function is to make one whole mc
		// TODO Auto-generated method stub

		ArrayList<CategoryInfo> childList = parentCategoryInfo.childCategory;

		if ( parentCategoryInfo.isleaf == 1 || childList == null || childList.size() == 0) {
			INDArray leafCentroid = getCategoryCentroid(parentCategoryInfo);
			
			if (leafCentroid == null) {
				System.out.println("Catid " + parentCategoryInfo.categoryid + " Warning! leaf centroid returned null " + leafCentroid);
			}
			return leafCentroid;
		} 
		else {
			/* add centroid of a category and children's merge centroids */
			INDArray parentCentroid = getCategoryCentroid(parentCategoryInfo);
			//System.out.println("Parent centroid catid: " +  parentCategoryInfo.categoryid  + " : " + parentCentroid);
			INDArray childCentroid = null;
			for (CategoryInfo child : childList) {
				try {
					childCentroid = loadINDArrayFromDB(child.categoryid, loader);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //need to get from mcs DB
				if (childCentroid == null) {
					System.out.println("empty child centroid for category "+ child.parentid + ", skipping..");
					childCentroid = Nd4j.zeros(300);
					//continue;
				}
				
		if(parentCentroid!=null){
			parentCentroid = parentCentroid.add(childCentroid);
		}
		else{
			System.out.println("Catid " + parentCategoryInfo.categoryid + " Warning! parent centroid returned null " + parentCentroid);
			//continue;
			}
		}
		double numOfCentroids = childList.size() + 1;
		parentCentroid.div(numOfCentroids);
		
		return parentCentroid;
			}
		}
	
	private void makeMCMap(List<CategoryInfo> allCategoriesMap, HashMap<Integer, CategoryInfo> Heuri_Info_MCC) {
		
		///	ArrayList<Integer> categoryIds = new ArrayList<Integer>(allCategoriesMap.keySet());
			HashMap<Integer, INDArray> centroidMap = new HashMap<Integer, INDArray>();
			int curID;
			CategoryInfo curCate = null;
			INDArray centroid;

			Map<Integer, INDArray> mergeCentroids = new HashMap<Integer, INDArray>();
			curCate = allCategoriesMap.get(0);
			getCategoryMC(curCate, curCate.categoryid,  mergeCentroids, Heuri_Info_MCC);
			System.out.println("MC map is ready of size " + mergeCentroids.size());
			//get_store_NearestWords_MC_afterMap(Heuri_Info_MCC, mergeCentroids);


		}
	
	private void getCategoryMC(CategoryInfo category, int rootId, Map<Integer, INDArray> mergeCentroidMap, HashMap<Integer, CategoryInfo> Heuri_Info_MCC) { //this function is to make one whole mc
		// TODO Auto-generated method stub

		//INDArray result = null;
		ArrayList<CategoryInfo> childList = category.childCategory;

		//System.out.println(category.categoryName);
		//System.out.println(category.fullpath);
		//System.out.println("childList size is " + childList.size());
		/*for(CategoryInfo cat : childList) {
			System.out.println("WRONG: Category " + category.categoryid + " vot tebe child: " + cat.categoryid);
			System.out.println("Category " + category.parentid + " vot tebe child: " + cat.categoryid);
		}*/
		
		if ( category.isleaf == 1 || childList == null || childList.size() == 0) {
			INDArray leafCentroid = getCategoryCentroid(category);
			
			if (leafCentroid == null) {
				System.out.println("Catid " + category.categoryid + " Warning! leaf centroid returned null " + leafCentroid);
			}
			mergeCentroidMap.put(rootId, leafCentroid);
			return;
		} else {
			/* add centroid of a category and children's merge centroids */
			for (CategoryInfo child : childList) {
				getCategoryMC(child, child.categoryid, mergeCentroidMap, Heuri_Info_MCC);
			}

			/*already computed after recursion returns*/
			INDArray currentCentroid = getCategoryCentroid(category);
			INDArray mergeCentroid = null;
			//assert(currentCentroid != null);
			if (currentCentroid != null) {
				//mergeCentroid = currentCentroid.mul(0.8);
				mergeCentroid = currentCentroid;

			}
			
			if (mergeCentroid == null) {
				mergeCentroid = Nd4j.zeros(300);
				System.out.println("empty merge centroid for  category " + category.categoryid + " init 300 zeros..");
			}
			for (CategoryInfo child : childList) {
				INDArray childCentroid = mergeCentroidMap.get(child.categoryid);
				//System.out.println("MC child is " + child.categoryid);
				if (childCentroid == null) {
					System.out.println("empty child centroid for category "+ child.parentid + ", skipping..");
					continue;
				}
				
				//mergeCentroid = mergeCentroid.mul(0.8).add(childCentroid.mul(0.2)) /*/norm) */;
				mergeCentroid = mergeCentroid.add(childCentroid) /*/norm) */;

				// not sure why divide every time
				double numOfCentroids = childList.size() + 1;
				mergeCentroid.div(numOfCentroids);
				mergeCentroid.div(2);
				//mergeCentroid.div(1.5);

			}
			mergeCentroidMap.put(rootId, mergeCentroid);
			try {
				get_store_NearestWords_MC(rootId, Heuri_Info_MCC, mergeCentroid);
				//System.out.println("MC itself: " + mergeCentroid);

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		//System.out.println("MC map size " + mergeCentroidMap.size());
	}
	
	private INDArray getCentroid(String sentence, int weight) {
		// TODO Auto-generated method stub

		INDArray result = null;
		sentence = deleteUselessLetter(sentence); 
		String[] wordLists = sentence.split(" ");

		double mok_sum = 0;
		for (int i = 0; i < wordLists.length; i++) {
			if (termDF.containsKey(wordLists[i])) {
				if(words.vector(wordLists[i]) !=null){
				INDArray tmp = words.vector(wordLists[i]).div(Math.log(termDF.get(wordLists[i])));
				//System.out.println("Word " + wordLists[i] + " has vector " + tmp);
				mok_sum += ((1.0) / Math.log(termDF.get(wordLists[i])));
				if (result == null)
					result = tmp;
				else
				{
					if(!tmp.toString().contains("∞") && !tmp.toString().contains("NaN")){
						result = result.add(tmp);
					}
				}
			}
			}else {
				// 바이그램 단위로도 실험해보기
			}
			// 벡터가 LookUpTable에 없는 경우도 실험해보기
		}

		if(result != null)
			result.div(mok_sum);
		return result;
	}

	private INDArray getSentenceVec(String sentence) {
		// TODO Auto-generated method stub

		INDArray result = Nd4j.zeros(300);
		String[] wordLists = sentence.split(" ");

		for (int i = 0; i < wordLists.length; i++) {
			if(words.vector(wordLists[i]) == null){
				continue;
			}

				INDArray tmp = words.vector(wordLists[i]);
				result = result.add(tmp);
		}
		
		if(result != null)
			result.div(wordLists.length);

		return result;
	}
	
	public static String deleteUselessLetter(String oneSentence) {
		if (oneSentence == null)
			return null;

		// TODO Auto-generated method stub
		String[] deleteTag = { "&quot", "<url>", "</url>", "<category>", "</category>", "<page>", "<title>", "<body>",
				"<tag>", "</page>", "</title>", "</body>", "</tag>", "<?xml version='1.0' encoding='euc-kr'?>", "&lt;",
				"&gt;", "[()]", "[?]", "[*]", "\\.", "\"", ",", "~", "��", "\\+", "\\*", "!", "/", "$", ":", ";", "=",
				"\\?", "\\@", "\\)", "\\(", "\\“", "\\”", "’s", "_", "--", "\\]", "\\[", "\\~", "\\!", "\\#", "\\$",
				"\\`", "\\{", "\\|", "&", "%" };

		String[] deleteLetter = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "a", "b", "c", "d", "e", "f", "g",
				"h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };

		for (int i = 0; i < deleteTag.length; i++) {
			// System.out.println(deleteTag[i]);
			oneSentence = oneSentence.replaceAll(deleteTag[i], " ");
		}
		oneSentence = oneSentence.replaceAll("\"", " ");
		oneSentence = oneSentence.replaceAll("\n", " ");
		oneSentence = oneSentence.replaceAll("  ", " ");
		oneSentence = oneSentence.toLowerCase();

		ArrayList<String> words = new ArrayList<String>(Arrays.asList(oneSentence.split(" ")));

		for (int i = 0; i < words.size(); i++) {
			String[] tmp = words.get(i).split("-");
			if (tmp.length > 1) {
				for (int j = 0; j < tmp.length; j++)
					words.add(tmp[j]);
				words.remove(i);
				i--;
			}
		}
		for (int i = 0; i < words.size(); i++) {
			for (int j = 0; j < deleteLetter.length; j++) {
				if (words.get(i).equals(deleteLetter[j])) {
					words.remove(i);
					i--;
					break;
				}
			}

			if (i > 0) {
				while (words.get(i).length() > 0 && words.get(i).charAt(0) == '-') {
					words.set(i, words.get(i).substring(1));
				}
				if (words.get(i).length() > 0 && words.get(i).charAt(words.get(i).length() - 1) == '-') {
					words.set(i, words.get(i).substring(0, words.get(i).length() - 1));
				}
			}
		}
		String returnValue = "";
		for (int i = 0; i < words.size(); i++)
			returnValue += words.get(i) + " ";

		return returnValue;
	}

	// 휴리스틱 규칙이 적용된 카테고리들의 정보 (이름, 문서 정보)와, 해당 카테고리의 Child카테고리 정보 (이름, 문서 정보)를
	// 리턴한다.
	private HashMap<Integer, CategoryInfo> getHuri_specificLevel_MCC(int level) throws SQLException, IOException {
		// TODO Auto-generated method stub
		//don't need HeuriCate_speciflevel if need to make all the categories
		HashMap<Integer, CategoryInfo> HeuriCate_speciflevel = getHeuriCate_level(level - 1);
		ArrayList<CategoryInfo> allCate_Chilelevel = ODP_DB.getCategoryInfo_Level(level);

		System.out.println(HeuriCate_speciflevel.size() + " " + allCate_Chilelevel.size());
		// Child category 정보를 부모 카테고리에 연결한다.
		for (int i = 0; i < allCate_Chilelevel.size(); i++) {

			CategoryInfo childCate = allCate_Chilelevel.get(i);
			if (HeuriCate_speciflevel.containsKey(childCate.parentid)) {
				CategoryInfo heuri = HeuriCate_speciflevel.get(childCate.parentid);

				// ChildCategory 추가 - 카테고리 정보와 documentInfo도 추가
				childCate.documentInfo = setDocumentInfo(childCate.categoryid);
				heuri.childCategory.add(childCate);

				HeuriCate_speciflevel.put(childCate.parentid, heuri);

			}
		}

		return HeuriCate_speciflevel;

	}

	private HashMap<Integer, CategoryInfo> getHuri_specificLevel_MCC_All(int level, HashMap<Integer, CategoryInfo> allMap) throws SQLException, IOException {
		// TODO Auto-generated method stub
		//don't need HeuriCate_speciflevel if need to make all the categories
		HashMap<Integer, CategoryInfo> HeuriCate_speciflevel = getHeuriCate_level(level - 1);
		ArrayList<CategoryInfo> allCate_Chilelevel = ODP_DB.getCategoryInfo_Level(level);

		System.out.println(HeuriCate_speciflevel.size() + " " + allCate_Chilelevel.size());
		// Child category 정보를 부모 카테고리에 연결한다.
		for (int i = 0; i < allCate_Chilelevel.size(); i++) {

			CategoryInfo childCate = allCate_Chilelevel.get(i);
			if (HeuriCate_speciflevel.containsKey(childCate.parentid)) {
				CategoryInfo heuri = HeuriCate_speciflevel.get(childCate.parentid);

				// ChildCategory 추가 - 카테고리 정보와 documentInfo도 추가
				childCate.documentInfo = setDocumentInfo(childCate.categoryid);
				heuri.childCategory.add(childCate);

				allMap.put(childCate.parentid, heuri);

			}
		}
		return allMap;

	}
	
	private HashMap<Integer, CategoryInfo> getAllCategories() throws SQLException, IOException {
		// TODO Auto-generated method stub
		//don't need HeuriCate_speciflevel if need to make all the categories
		ArrayList<CategoryInfo> allCate_Chilelevel = ODP_DB.getCategoryInfo();
		HashMap<Integer, CategoryInfo> allCategories = new HashMap<Integer, CategoryInfo>();
		
		for (int i = 0; i < allCate_Chilelevel.size(); i++) {

			CategoryInfo childCate = allCate_Chilelevel.get(i);
			CategoryInfo heuri = allCate_Chilelevel.get(childCate.parentid);

			// ChildCategory 추가 - 카테고리 정보와 documentInfo도 추가
			childCate.documentInfo = setDocumentInfo(childCate.categoryid);
			heuri.childCategory.add(childCate);

			//allCategories.put(childCate.parentid, heuri);

		}

		return allCategories;
	}
	
	private HashMap<Integer, CategoryInfo> getAllCategoriesNew() throws SQLException, IOException {
		// TODO Auto-generated method stub
		//don't need HeuriCate_speciflevel if need to make all the categories
		ArrayList<CategoryInfo> allCategoriesList = ODP_DB.getCategoryInfo();
		HashMap<Integer, CategoryInfo> allCategories = new HashMap<Integer, CategoryInfo>();
		System.out.println("allCategoriesList size is " + allCategoriesList.size());
		
		for (int i = 0; i < allCategoriesList.size(); i++) {
			CategoryInfo thisCategory = allCategoriesList.get(i);
			assert(thisCategory.categoryid == i);
			//System.out.println(i + ". Catid " + thisCategory.categoryid);
			CategoryInfo parentCategory = allCategoriesList.get(thisCategory.parentid);
			//System.out.println(i + ". Parent Catid " + thisCategory.parentid);

			// ChildCategory 추가 - 카테고리 정보와 documentInfo도 추가
			thisCategory.documentInfo = setDocumentInfo(thisCategory.categoryid);
			parentCategory.childCategory.add(thisCategory);

			allCategories.putIfAbsent(thisCategory.parentid, parentCategory);
		}
		System.out.println("Size of allCategories in map is " + allCategories.size());
		return allCategories;
	}
	
	private List<CategoryInfo> getAllCategoriesNewList() throws SQLException, IOException {
		// TODO Auto-generated method stub
		//don't need HeuriCate_speciflevel if need to make all the categories
		ArrayList<CategoryInfo> allCategoriesList = ODP_DB.getCategoryInfo();
		//HashMap<Integer, CategoryInfo> allCategories = new HashMap<Integer, CategoryInfo>();
		System.out.println("allCategoriesList size is " + allCategoriesList.size());
		for (int i = 0; i < allCategoriesList.size(); i++) {
			CategoryInfo thisCategory = allCategoriesList.get(i);
			//System.out.println(i + ". Catid " + thisCategory.categoryid);
			if (thisCategory.parentid <= 0) {
				continue;
			}
			CategoryInfo parentCategory = allCategoriesList.get(thisCategory.parentid - 1);
			//System.out.println(i + ". Parent Catid " + thisCategory.parentid);

			// ChildCategory 추가 - 카테고리 정보와 documentInfo도 추가
			thisCategory.documentInfo = setDocumentInfo(thisCategory.categoryid);
			parentCategory.childCategory.add(thisCategory);

			//allCategories.putIfAbsent(thisCategory.parentid, parentCategory);
		}
		System.out.println("Size of allCategories in list is " + allCategoriesList.size());
		return allCategoriesList;
	}
	
	private HashMap<Integer, CategoryInfo> getCategoriesNewListByLevel(int parent_level, int child_level) throws SQLException, IOException {
		// TODO Auto-generated method stub
		ArrayList<CategoryInfo> childCategoriesList = ODP_DB.getCategoryInfo_Level(child_level); // merge centroid info 		
		HashMap<Integer, CategoryInfo> parentCategoriesList = ODP_DB.getCategoryInfoLevel(parent_level); // centroid info
		
		//HashMap<Integer, CategoryInfo> parentCategoriesListUpdated = ODP_DB.getCategoryInfoLevel(parent_level); // centroid info
 
		System.out.println("childCategoriesList size is " + childCategoriesList.size());
		System.out.println("parentCategoriesList size is " + parentCategoriesList.size());

		if(!childCategoriesList.isEmpty()){
			for(Integer catid : parentCategoriesList.keySet()){
				//System.out.println("parentCategory " + catid);
				parentCategoriesList.get(catid).documentInfo = setDocumentInfo(catid);
				parentCategoriesList.get(catid).childCategory = childCategoriesList;
			}
			
			/*for (Integer catid: childCategoriesList.keySet()) {
				CategoryInfo childCategory = childCategoriesList.get(catid);
				if (childCategory.parentid <= 0) {
					continue;
				}
				System.out.println("Child: " + childCategory.categoryid + " , parent: " + childCategory.parentid);
				CategoryInfo parentCategory = parentCategoriesList.get(childCategory.parentid);
				
				parentCategory.documentInfo = setDocumentInfo(parentCategory.categoryid);
				childCategory.documentInfo = setDocumentInfo(childCategory.categoryid);
				System.out.println("For parent " + childCategory.parentid + " child: " + childCategory.categoryid);
				parentCategory.childCategory.add(childCategory);
				}*/
		}
		else{
			System.out.println("\tChild list is empty!");
			for(Integer catid : parentCategoriesList.keySet()){
				//System.out.println("parentCategory " + catid);
				parentCategoriesList.get(catid).documentInfo = setDocumentInfo(catid);
				
			}
		}
		return parentCategoriesList;
	}
	
	// 휴리스틱 규칙을 따르는 카테고리들 중, 특정 레벨에 속한 카테고리를 가져온다.
	public HashMap<Integer, CategoryInfo> getHeuriCate_level(int level) throws IOException, SQLException {

		if (HeuristicCategory_path == null)
			HeuristicCategory_path = getHeuriCate_path();

		HashMap<Integer, CategoryInfo> result = new HashMap<Integer, CategoryInfo>();

		// 한 레벨에 있는 모든 카테고리정보를 가져온다.
		ArrayList<CategoryInfo> allCate_speciflevel = ODP_DB.getCategoryInfo_Level(level);

		// 휴리스틱 규칙을 따르는 카테고리 정보를 가져온다.
		for (int i = 0; i < allCate_speciflevel.size(); i++) {
			if (HeuristicCategory_path.contains(allCate_speciflevel.get(i).fullpath)) {
				// 카테고리의 정보(이름, path 등..)를 가져온다.
				CategoryInfo tmp = allCate_speciflevel.get(i);
				// 해당 카테고리의 문서 정보 가져온다.
				tmp.documentInfo = setDocumentInfo(tmp.categoryid);
				result.put(allCate_speciflevel.get(i).categoryid, tmp);
			}
		}

		return result;
	}

	private ArrayList<Document> setDocumentInfo(int categoryId) throws SQLException {
		// TODO Auto-generated method stub
		return ODP_DB.LoadDocumentInfo(categoryId);
	}

	public ArrayList<String> getHeuriCate_path() throws IOException {
		// 1. Category Name을 받아온다.
		ArrayList<String> ODPHueristicPath = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader("ODPcategoris2008.txt"));
		String line = "";

		while ((line = br.readLine()) != null) {

			line = "Top/" + line.split("\t")[0];
			ODPHueristicPath.add(line);

		}

		br.close();
		return ODPHueristicPath;
	}

	
	
	// // 1 parsing ) ī�װ� ���� ����
	// String sql_insert_category = "insert into
	// dmoz_original(id,name,fullname,parentid,level,urlnum,subtreeurlnum," +
	// "subtreenum,inverselevel,isleaf,childnum) values(?,?,?,?,?,?,?,?,?,?,?)";
	// PreparedStatement pstmt_insert_category = null;
	//
	// // 1 parsing ) URL ���� ����
	// String sql_insert_url = "insert into
	// dmoz_docs(urlID,categoryID,url,title,description) values(?,?,?,?,?)";
	// PreparedStatement pstmt_insert_url = null;

}
