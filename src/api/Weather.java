package api;

import javax.ws.rs.core.MediaType;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.*;

import javax.ws.rs.*;

import db.DBCommon;

@Path("/weather")
public class Weather {
	private static final String collection = "weather";
	
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
	
	// GET all weather data
	// <IP>:8080/SkiReport/weather
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getWeather() {
		DBCommon db = getConn();
		MongoCollection<Document> coll = getCollectionFromConn(db);
		MongoCursor<Document> cursor = coll.find().iterator();
		String jsonData = "{ weather: [";
		
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
	
	// GET all weather report data by resort
	// <IP>:8080/SkiReport/weather/{resort}/all
	@GET
	@Path("/{resort}/all")
	@Produces(MediaType.APPLICATION_JSON)
	public String getWeatherByResort(@PathParam("resort") String resort) {		
		String resortString = getResortString(resort);
		String jsonData = "";
		
		DBCommon db = getConn();
		
		MongoCollection<Document> coll = getCollectionFromConn(db);
		MongoCursor<Document> cursor = coll.find(eq("resort", resortString)).iterator();
		
		jsonData += "{ weather: [";
		
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
	
	// GET recent weather report data by zip code
	// <IP>:8080/SkiReport/weather/{zipCode}
	@GET
	@Path("/{zipCode}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRecentWeatherByZipCode(@PathParam("zipCode") String zipCode) {		
		String jsonData = "{ weather: ";
		
		DBCommon db = getConn();
		MongoCollection<Document> coll = getCollectionFromConn(db);
		
		Document doc = coll.find(eq("zipCode", zipCode)).sort(new BasicDBObject("lastUpdated", -1)).first();
		
		System.out.println("looking for resort: " + zipCode);
		
		if (doc == null) {
			return "{error: \"resort not found\"}";
		}
		
		doc.remove("_id");
		
		jsonData += doc.toJson();
		
		jsonData += " }";
		
		closeConn(db);
		
		return jsonData;
	}
	
	private static String getResortString(String input) {
		String resort = "";
		
		for (int i = 0; i < input.length(); i++) {
			char ch = input.charAt(i);
			
			if (i == 0) {
				ch -= 32;	// shift to uppercase
			} else if (ch == '_') {
				i++;			// skip '_'
				ch = input.charAt(i);
				ch -= 32;		// add the next char Upper case
				resort += " ";	// add a space before the next capital letter
			}
			
			resort += ch;
		}
		
		System.out.println(resort);
		
		return resort;
	}
}