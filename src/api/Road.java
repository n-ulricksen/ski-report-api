package api;

import javax.ws.rs.core.MediaType;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.*;

import javax.ws.rs.*;

import db.DBCommon;

@Path("/roads")
public class Road {
	private static final String collection = "roads";
	
	private static DBCommon getConn() {
		DBCommon db = new DBCommon();
		return db;
	}
	
	private static void closeConn(DBCommon conn) {
		conn.closeConnection();
	}
	
	private static MongoCollection<Document> getCollectionFromConn(DBCommon conn) {
		MongoCollection<Document> coll = conn.getCollection(collection);
		return coll;
	}
	
	// GET all roads data
	// <IP>:8080/SkiReport/roads
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRoads() {
		DBCommon db = getConn();
		MongoCollection<Document> coll = getCollectionFromConn(db);
		MongoCursor<Document> cursor = coll.find().sort(new BasicDBObject("lastUpdated", -1)).iterator();
		String jsonData = "{ roads: [";
		
		try {
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				doc.remove("_id");
				jsonData += doc.toJson() + ", ";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		jsonData += "] }";
		
		closeConn(db);
		
		return jsonData;
	}
	
	// GET all roads report data by highway #
	// <IP>:8080/SkiReport/roads/{highway}/all
	@GET
	@Path("/{highway}/all")
	@Produces(MediaType.APPLICATION_JSON)
	public String getWeatherByResort(@PathParam("highway") String highway) {
		String jsonData = "";
		
		DBCommon db = getConn();
		
		MongoCollection<Document> coll = getCollectionFromConn(db);
		MongoCursor<Document> cursor = coll.find(eq("highway", highway)).sort(new BasicDBObject("lastUpdated", -1)).iterator();

		if (!cursor.hasNext()) {
			return "{error: \"highway not found\"}";
		}
		
		jsonData += "{ roads: [";
		
		try {
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				doc.remove("_id");
				jsonData += doc.toJson() + ", ";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		jsonData += "] }";
		
		closeConn(db);
		
		return jsonData;
	}
	
	// GET recent roads report data by highway
	// <IP>:8080/SkiReport/roads/{highway}
	@GET
	@Path("/{highway}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRecentWeatherByResort(@PathParam("highway") String highway) {
		String jsonData = "{ roads: ";
		
		DBCommon db = getConn();
		MongoCollection<Document> coll = getCollectionFromConn(db);
		
		Document doc = coll.find(eq("highway", highway)).sort(new BasicDBObject("lastUpdated", -1)).first();
		
		if (doc == null) {
			return "{error: \"highway not found\"}";
		}
		
		doc.remove("_id");
		
		jsonData += doc.toJson();
		
		jsonData += " }";
		
		closeConn(db);
		
		return jsonData;
	}
}