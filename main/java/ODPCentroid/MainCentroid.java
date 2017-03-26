package ODPCentroid;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;

import ETC_noUse.word2vec;
import Main.StaticValues;

public class MainCentroid {

	static CentroidDB ODP_DB = null;
	static WeightLookupTable<VocabWord> words = null;
	ArrayList<String> HeuristicCategory_path = null;
	HashMap<String, Integer> termDF = null;

	public MainCentroid() throws SQLException, IOException {
		init();
		start();
	}

	public void init() throws SQLException {
		if (ODP_DB == null) {
			ODP_DB = new CentroidDB();
			ODP_DB.connectDB();
			System.out.println("Connected to db..");
		}
		System.out.println("starting to load dictionary from db");
		termDF = ODP_DB.LoadTermInfo();
	}

	public void start() throws SQLException, IOException {
		// 12.20 i몇번째부터인지 보기.
		/*for (int i = 14; i > 0; i--) {
			HashMap<Integer, CategoryInfo> Heuri_Info_MCC = getHuri_specificLevel_MCC(i); //get Heuristics categories only from ODPcategoris2008.txt
			System.out.println(i + " " + Heuri_Info_MCC.size());
			get_store_NearestWords(Heuri_Info_MCC);
		}*/
		System.out.println("starting to build category map");
		//HashMap<Integer, CategoryInfo> allCategInfoMap = getAllCategoriesNew();
		List<CategoryInfo> allCategInfoMap = getAllCategoriesNewList();
		System.out.println("All taxonomy size is " + allCategInfoMap.size());
		//get_store_NearestWords(allCategInfoMap);
		if (words == null)
			words = StaticValues.vec.getLookupTable();
		makeMCMap(allCategInfoMap);

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
			//////curCate.KnnWordsList = KnnWordsList;
			//////ODP_DB.storeKNNResult(curCate); 
			
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
		
		for (int i = 0; i < category.documentInfo.size(); i++) {
			Document doc = category.documentInfo.get(i);
			//System.out.println("Doc title and desc: " + doc.title + doc.description);
			
			INDArray tmp = getCentroid(doc.title + " " + doc.description, 1);

			/*if (tmp == null){
				System.out.println("tmp is null!");
			}*/
			if (result == null) {
				result = tmp;
			}

			else {
				if(tmp!=null){
				result = result.add(tmp);
				//System.out.println("Result not new");
				}
				else{
					continue;
				}
			}
		}
		if(result == null)
			return null;
		
		
		result = result.div(category.documentInfo.size());
		//if (weight != 1)
			//result = result.mul(weight);
		// 실제 centroid를 계산한다.
		return result;
	}

	private void makeMCMap(List<CategoryInfo> allCategoriesMap) {
		
	///	ArrayList<Integer> categoryIds = new ArrayList<Integer>(allCategoriesMap.keySet());
		HashMap<Integer, INDArray> centroidMap = new HashMap<Integer, INDArray>();
		int curID;
		CategoryInfo curCate = null;
		INDArray centroid;
		//for (int i = 0; i < categoryIds.size(); i++) { 
		///for(Integer categ : allCategoriesMap.keySet()) {
			//curID = categoryIds.get(i);
			//curID = categ;
			///curCate = allCategoriesMap.get(categ);
			///ArrayList<CategoryInfo> childs = curCate.childCategory;

			///for(CategoryInfo child : childs) {
				//System.out.println("1.Category " + categ + " child: " + child.categoryid);
				//System.out.println("WRONG: 2.Category " + curCate.categoryid + " child: " + child.categoryid);
				//System.out.println("3.Category " + curCate.parentid + " child: " + child.categoryid);
				///centroid = getCategoryCentroid(child);
				///centroidMap.put(child.categoryid, centroid); 
			///}
			
			//INDArray centroid = getCategoryCentroid_MCC(curCate);
			///centroid = getCategoryCentroid(curCate);
			///centroidMap.put(categ, centroid); 
		///}
		//System.out.println("Centroid map is ready of size " + centroidMap.size());
		Map<Integer, INDArray> mergeCentroids = new HashMap<Integer, INDArray>();
		curCate = allCategoriesMap.get(0);
		getCategoryMC(curCate, 0,  mergeCentroids);
		System.out.println("MC map is ready of size " + mergeCentroids.size());

	}
	private void getCategoryMC(CategoryInfo category, int rootId, Map<Integer, INDArray> mergeCentroidMap) { //this function is to make one whole mc
		// TODO Auto-generated method stub

		//INDArray result = null;
		ArrayList<CategoryInfo> childList = category.childCategory;
		//for(CategoryInfo cat : childList){
			//System.out.println("WRONG: Category " + category.categoryid + " vot tebe child: " + cat.categoryid);
			//System.out.println("Category " + category.parentid + " vot tebe child: " + cat.categoryid);
		//}
		
		///INDArray currentCentroid = centroidMap.get(rootId);

		if ( category.isleaf == 1 || childList == null || childList.size() == 0) {
			///if (currentCentroid == null) {
			///	System.out.println("current centroid from category " + rootId + " is nulls");
			///	mergeCentroidMap.put(rootId, null);
			///	return;
			///}
			mergeCentroidMap.put(rootId, getCategoryCentroid(category));
			return;
		} else {
			/*** add centroid of a category and children's merge centroids ***/
			try {
				for (CategoryInfo child : childList) {
					getCategoryMC(child, child.categoryid,  mergeCentroidMap);
				}

			} catch (StackOverflowError e) {
				System.out.println("category depth is " +  category.level + " and leaf is " + category.isleaf);
			}

			//already computed after recursion returns
			INDArray currentCentroid = getCategoryCentroid(category);
			INDArray mergeCentroid = null;
			assert(currentCentroid != null);
			if (currentCentroid != null) {
				/*adjust weights : add 0.8 - bigger weight to parent centroid */
				/*for (int i =0; i < currentCentroid.size(); i++) {
					currentCentroid.getCentroid().set(i, currentCentroid.getCentroid().get(i) * 0.8);
				}*/
				mergeCentroid = currentCentroid.mul(0.8);
				//mergeCentroid.normalize();
			}
			//double norm;
			
			for (CategoryInfo child : childList) {
				INDArray childCentroid = mergeCentroidMap.get(child);
				if (childCentroid == null) {
					System.out.println("empty child centroid for category "+ child + ", skipping..");
					continue;
				}
				//List<Double> childCentroidList = mergeCentroidMap.get(child).getCentroid();
				//norm = mergeCentroidMap.get(child).setCentroid_lengthNorm();
				
				/*** merge-centroid calculation by term addition ***/
				// if(merge_centroid!=null){

				if (mergeCentroid != null) {
					//System.out.println("merge centroid size is " + mergeCentroid.getCentroid().size());
					//System.out.println("child centroid list size is " + childCentroidList.size());
					//for (int i = 0; i < childCentroidList.size(); i++) {
					mergeCentroid = mergeCentroid.mul(0.8).add(childCentroid.mul(0.2)) /*/norm) */;
					//}
				} else {
					System.out.println("empty merge centroid for child category " + child + ", breaking..");
				}
				
				// put Normalized merge - centroids
				//double numOfCentroids = (currentCentroid == null) ? childList.size() : (childList.size() +1);
				double numOfCentroids = childList.size() + 1;
				//for (int i = 0; i < mergeCentroid.getCentroid().size(); i++) {
				mergeCentroid.div(numOfCentroids);
				//}
				//mergeCentroid.normalize();
			}
			mergeCentroidMap.put(rootId, mergeCentroid);
			//System.out.println("MC map size " + mergeCentroidMap.size());
		}
		System.out.println("MC map size " + mergeCentroidMap.size());
	}
	
	private INDArray getCentroid(String sentence, int weight) {
		// TODO Auto-generated method stub

		INDArray result = null;
		sentence = deleteUselessLetter(sentence); 
		String[] wordLists = sentence.split(" ");

		double mok_sum = 0;
		for (int i = 0; i < wordLists.length; i++) {
			if (termDF.containsKey(wordLists[i])) {
				INDArray tmp = words.vector(wordLists[i]).div(Math.log(termDF.get(wordLists[i])));

				mok_sum += ((1.0) / Math.log(termDF.get(wordLists[i])));
				if (result == null)
					result = tmp;

				else
					result.add(tmp);
			} else {
				// 바이그램 단위로도 실험해보기
			}
			// 벡터가 LookUpTable에 없는 경우도 실험해보기
		}

		if(result != null)
			result.div(mok_sum);
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
			assert(thisCategory.categoryid == i);
			//System.out.println(i + ". Catid " + thisCategory.categoryid);
			CategoryInfo parentCategory = allCategoriesList.get(thisCategory.parentid);
			//System.out.println(i + ". Parent Catid " + thisCategory.parentid);

			// ChildCategory 추가 - 카테고리 정보와 documentInfo도 추가
			thisCategory.documentInfo = setDocumentInfo(thisCategory.categoryid);
			parentCategory.childCategory.add(thisCategory);

			//allCategories.putIfAbsent(thisCategory.parentid, parentCategory);
		}
		System.out.println("Size of allCategories in list is " + allCategoriesList.size());
		return allCategoriesList;
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
