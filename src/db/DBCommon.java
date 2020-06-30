package db;

import com.mongodb.client.*;

import org.bson.*;

public class DBCommon {
	private MongoClient conn;
	private static String userId;
	private static String pwd;
	private static String dbName;


	public DBCommon() {
		setDBCredentials();
		
		String mongoURI = "mongodb+srv://" + userId + ":" + pwd + 
			    "@skireport-okbvn.mongodb.net/ski-report?retryWrites=true&w=majority";
		
		if (conn == null) {
			conn = MongoClients.create(mongoURI);
		}
	}
	
	private static void setDBCredentials() {
		if (userId == null || pwd == null || dbName == null) {
			userId = System.getenv("DB_USER");
			pwd    = System.getenv("DB_PWD");
			dbName = System.getenv("DB_NAME");
		}
	}
	
	public MongoCollection<Document> getCollection(String collectionName) {
		MongoDatabase db = conn.getDatabase(dbName);
		
		MongoCollection<Document> collection = db.getCollection(collectionName);
		
		return collection;
	}
	
	public void insertDocument(Document newDoc, String collectionName){
		// get collection
		MongoCollection<Document> collection = getCollection(collectionName);		
		
		// insert the document into collection
		collection.insertOne(newDoc);
		System.out.println("inserted...");
				
		// close collection
		closeConnection();
	}
	
	public void closeConnection() {
		conn.close();
	}
}