package Main;

import java.io.IOException;
import java.sql.SQLException;


import ETC_noUse.word2vec;


import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.nd4j.jdbc.mysql.MysqlLoader;
import org.nd4j.jdbc.loader.impl.BaseLoader;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
 
import java.sql.Blob;
 
import static org.junit.Assert.assertEquals;

public class main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		System.out.println("Loading w2v model");
		new word2vec().LoadModel();
		System.out.println("Finished loading w2v model");
		System.out.println("starting MainCentroid class");
		try {
			new ODPCentroid.MainCentroid();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    	
	}
}
