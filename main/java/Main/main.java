package Main;

import java.io.IOException;
import java.sql.SQLException;

import ETC_noUse.word2vec;

public class main {

	public static void main(String[] args) throws IOException, SQLException {
		// TODO Auto-generated method stub

		System.out.println("Loading w2v model");
		new word2vec().LoadModel();
		System.out.println("Finished loading w2v model");
		System.out.println("starting MainCentroid class");
    	new ODPCentroid.MainCentroid();
	}
}
