package ODPCentroid;

import java.util.ArrayList;
import java.util.Collection;

public class CategoryInfo {

	int categoryid;
	String categoryName;
	String fullpath; 
	int parentid;
	int level;
	int urlnum ;
	int isleaf;
	int childnum;
	int inversedLevel;
	int subtreeurlnum;
	int subtreenum;
	
	
	Collection<String> KnnWordsList = null;
	ArrayList<Document> documentInfo = new ArrayList<Document>();
	
	public ArrayList<CategoryInfo> childCategory = new ArrayList<CategoryInfo>();
	
	

	public CategoryInfo(int id, String name, String fullname, int parentid, int level, int urlnum, int isleaf, int childnum, int inversedLevel, int subtreeurlnum, int subtreenum) {
		// TODO Auto-generated constructor stub 
			// TODO Auto-generated constructor stub
		this.categoryid = id;
		this.categoryName = name;
		this.fullpath = fullname;
		this.parentid = parentid;
		this.level = level;
		this.urlnum = urlnum;
		this.isleaf = isleaf;
		this.childnum = childnum;
		this.inversedLevel = inversedLevel;
		this.subtreeurlnum = subtreeurlnum;
		this.subtreenum = subtreenum;
	}



 
	
}
