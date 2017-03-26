package Main;

import java.io.IOException;
import java.sql.SQLException;

import ETC_noUse.word2vec;

public class main {

	public static void main(String[] args) throws IOException, SQLException {
		// TODO Auto-generated method stub

		new word2vec().LoadModel();
    	new ODPCentroid.MainCentroid();
	}
}
