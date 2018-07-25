package main;



	
	import com.mchange.v2.c3p0.ComboPooledDataSource;
	import org.junit.Ignore;
	import org.junit.Test;
import org.nd4j.jdbc.mysql.MysqlLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
	import org.nd4j.linalg.factory.Nd4j;
	 
	import java.sql.Blob;
	 
	import static org.junit.Assert.assertEquals;
	 
	public class MysqlLoaderTest {
	 
	 
	    //simple litmus test, unfortunately relies on an external database
	    @Test
	    @Ignore
	    public void testMysqlLoader() throws Exception {
	        ComboPooledDataSource ds = new ComboPooledDataSource();
	        ds.setJdbcUrl("jdbc:mysql://localhost:3306/dmoz2013_s");
	        MysqlLoader loader = new MysqlLoader(ds, "jdbc:mysql://localhost:3306/nd4j?user=root&password=newpass", "mcs_info_after_bug_fix", "mc_value");
	        //loader.delete("1");
	        //INDArray load = loader.load(loader.loadForID("1"));
	        //if (load != null) {
	       //     loader.delete("1");
	        //}
	        loader.save(Nd4j.create(new float[]{1, 2, 3}), "1");
	        Blob b = loader.loadForID("1");
	        INDArray loaded = loader.load(b);
	        assertEquals((Nd4j.create(new float[]{1, 2, 3})), loaded);
	        
	        loader.insertStatement();
	    }
	 
	}

