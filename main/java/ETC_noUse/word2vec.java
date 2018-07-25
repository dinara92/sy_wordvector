package ETC_noUse;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;

import Main.StaticValues;



/**
 * Hello world!
 *
 */
public class word2vec 
{ 
	public void LoadModel() throws IOException {
		// TODO Auto-generated constructor stub
		if(StaticValues.vec == null){
			//File gModel = new File("/home/dinara/word2vec/word2vec_gensim_ODP/ODP_word2vec/GoogleNews-vectors-negative300.bin");
			File gModel = new File("//home/dinara/word2vec/word2vec_gensim_ODP/ODP_word2vec/dmoz_pages_all_no_world_only_and_1bil_words_news_10context_300f_0mincount.txt");
			
			//File gModel = new File("/home/dinara/word2vec/word2vec_gensim_ODP/ODP_word2vec/OUTV_dmoz_pages_all_no_world_only_and_1bil_words_news_10context_300f_0mincount.txt");

			StaticValues.vec = (Word2Vec) WordVectorSerializer.readWord2VecModel(gModel, false);
		    //StaticValues.vec = (Word2Vec) WordVectorSerializer.loadGoogleModel(gModel, true);
		}
	}
//    public static void main( String[] args ) throws IOException, SQLException
//    {
//    	
//    	System.exit(0);
//        System.out.println( "Hello1111 World!" );
//        //갑자기 됨 ㄳㄳ
//        
//        
//        
//        
//        
//        
//        
////        
////        //Loading data..
////        SentenceIterator iter = new LineSentenceIterator(new File("test.txt"));
////        
////        
////        int batchSize = 1000;
////        int iterations = 3;
////        int layerSize = 150;
////        
////        //log.info("Build model....");
////        Word2Vec vec = new Word2Vec.Builder()
////                .batchSize(batchSize) //# words per minibatch.
////                .minWordFrequency(5) // 
////                .useAdaGrad(false) //
////                .layerSize(layerSize) // word feature vector size
////                .iterations(iterations) // # iterations to train
////                .learningRate(0.025) // 
////                .minLearningRate(1e-3) // learning rate decays wrt # words. floor learning
////                .negativeSample(10) // sample size 10 words
////                .iterate(iter) //
////                .build();
////        vec.fit();
////        
////        
////        Collection<String> kingList = vec.wordsNearest("sad", 50);
////        Collection<String> kingList2 = vec.wordsNearest("love", 50);
////        Collection<String> kingList3 = vec.wordsNearest("like", 50);
////        Collection<String> kingList4 = vec.wordsNearest("love_you", 50);
////        System.out.println(kingList);
////        System.out.println(kingList2);
////        System.out.println(kingList3);
////        System.out.println(kingList4);
//        
//         
//        
//       // vec.fit(); 
////        WeightLookupTable<VocabWord> words = vec.getLookupTable(); 
////        System.out.println("^.^ " + words.vector("apple"));
////        System.out.println("^.^ " + words.vector("apple").div(2)); 
////        System.out.println(words.vector("apple").div(300)); 
////        System.out.println(words.vector("apple").div(3000)); 
////        System.out.println(words.vector("apple").div(30000)); 
////        INDArray tmp = words.vector("apple").div(300000);
////        System.out.println(tmp.mul(300000));
////        
////       
////        //"king - queen + woman = man".
////
////
////        System.out.println(vec.wordsNearest("apple", 10));
////        System.out.println(vec.wordsNearest(words.vector("apple").div(300), 10));
////        System.out.println(vec.wordsNearest((words.vector("apple").div(300).mul(300)), 10));
////        System.out.println("^_^");
////        System.out.println(vec.wordsNearest("Nike", 5));
////        System.out.println(vec.wordsNearest("card", 100));
////        System.out.println(vec.wordsNearest("peer", 5));
////        System.out.println(vec.wordsNearest("friend", 5));
////        System.out.println(vec.wordsNearest("group", 5));
////        
////        System.out.println(vec.wordsNearest("apple", 5));
////        System.out.println(vec.wordsNearest("Apple", 5));
////        System.out.println(vec.wordsNearest("basketball", 5));
////        System.out.println(vec.wordsNearest("coffee", 5));
////        
////        
////        System.out.println(vec.wordsNearest(Arrays.asList("king", "woman"), Arrays.asList("queen"), 10));
////        System.out.println(vec.wordsNearest(Arrays.asList("fruit"), Arrays.asList("apple"), 10));
////        System.out.println(vec.wordsNearest(Arrays.asList("company"), Arrays.asList("Apple", "Google"), 10));
////        System.out.println(vec.wordsNearest(Arrays.asList("fruit"), Arrays.asList("science", "agriculture"), 10));
////                
////        System.out.println(vec.wordsNearest(Arrays.asList("Espresso"), Arrays.asList("coffee"), 10));
////        System.out.println(vec.wordsNearest(Arrays.asList("espresso"), Arrays.asList("coffee"), 10));
////        
////        System.out.println("sim");
////        
////        System.out.println(vec.similarity("Nike", "Greece"));
////        System.out.println(vec.similarity("peer", "friend"));
////        System.out.println(vec.similarity("group", "groups"));
////        System.out.println(vec.similarity("group", "company"));
////        System.out.println(vec.similarity("group", "organization"));
////        System.out.println(vec.similarity("sport", "sports"));
////        System.out.println(vec.similarity("sport", "activity"));
////
////        System.out.println(vec.similarity("Marathon", "sport"));
////        System.out.println(vec.similarity("Marathon", "cloth"));
////        System.out.println(vec.similarity("Marathon", "Judo"));
////
////        System.out.println(vec.similarity("sport", "sport"));
////        System.out.println(vec.similarity("Sport", "sport"));
////        System.out.println(vec.similarity("Sport", "sports"));
////        System.out.println(vec.similarity("sports", "sport"));
////        
////                
////        
////        
////        System.out.println(vec.similarity("Apple", "jobs"));
////        System.out.println(vec.similarity("apple", "jobs"));
////        System.out.println(vec.similarity("Apple", "company"));
////        System.out.println(vec.similarity("apple", "company"));
////        System.out.println(vec.similarity("apple", "fruit"));
////        System.out.println(vec.similarity("orange", "fruit"));
////        
////        System.out.println(vec.similarity("book", "text"));
////        System.out.println(vec.similarity("book", "reservation"));
//    }
    
}