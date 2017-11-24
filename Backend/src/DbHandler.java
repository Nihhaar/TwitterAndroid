

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class DbHandler {
	// connection strings
	private static String connString = "jdbc:postgresql://localhost:5432/postgres";
	private static String userName = "postgres";
	private static String passWord = "dbms";
	
	
	public static JSONObject authenticate(String id, String password,HttpServletRequest request){		
		JSONObject obj = new JSONObject();
		try{
			// Create the connection
			Connection conn = DriverManager.getConnection(connString, userName, passWord);
			String query = "select count(*) from password where id=? and password=?;";
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, id);
			preparedStmt.setString(2, password);
			ResultSet result =  preparedStmt.executeQuery();
			result.next();
			boolean ans = (result.getInt(1) > 0); 
			preparedStmt.close();
			conn.close();
			if(ans==true){
				request.getSession(true).setAttribute("id", id);
				obj.put("status",true);				
				obj.put("data", id);			
			}
			else{						
					obj.put("status",false);
					obj.put("message", "Authentication Failed");					
			}			
		} 
		catch(Exception e){
			e.printStackTrace();
		}
		return obj;
	}
	
	public static JSONObject createpost(String id, String postText, boolean hasImg)
	{
		JSONObject obj = new JSONObject();
		try{   
			Connection conn = DriverManager.getConnection(connString, userName, passWord);
			PreparedStatement pStmt = conn.prepareStatement("insert into post(uid, text, timestamp, hasImg) values(?,?,CURRENT_TIMESTAMP,?);");
			pStmt.setString(1, id);
			pStmt.setString(2, postText);
			pStmt.setBoolean(3, hasImg);
			if(pStmt.executeUpdate()>0)
			{
				obj.put("status", true);
				obj.put("data","Created Post");				
			}
			else
			{
				obj.put("status",false);
				obj.put("message", "Unable to create");
			}	
			}catch (Exception sqle)
			{
				sqle.printStackTrace();
			}
		return obj;
	}
	
	
	public static JSONObject writecomment(String id, String PostId, String comment)
	{
		JSONObject obj = new JSONObject();
		try{   
			Connection conn = DriverManager.getConnection(connString, userName, passWord);
			PreparedStatement pStmt = conn.prepareStatement("insert into comment(postid,uid,timestamp,text) values(?,?,CURRENT_TIMESTAMP,?);");
			pStmt.setInt(1, Integer.parseInt(PostId));
			pStmt.setString(2, id);
			pStmt.setString(3,comment);
			if(pStmt.executeUpdate()>0)
			{
				obj.put("status", true);
				obj.put("data","Created Post Successfully");			
			}
			else
			{
				obj.put("status",false);
				obj.put("message", "Could not Post");
			}	
			}catch (Exception sqle)
			{
				sqle.printStackTrace();
			}
		return obj;
	}
	
public static JSONArray userFollow(String id){
		
		JSONArray jsonObj = new JSONArray();
		try{
			// Create the connection
			Connection conn = DriverManager.getConnection(connString, userName, passWord);
			String query = "select uid2 as uid, name from follows, \"user\" where \"user\".uid "
					+ "= uid2 and uid1 = ?";
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, id);
			ResultSet result =  preparedStmt.executeQuery();
			
			jsonObj = ResultSetConverter(result);	
			preparedStmt.close();
			conn.close();
			 
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return jsonObj;
	}
	
	
	
	
	public static JSONObject deauth(HttpServletRequest request) throws JSONException
	{
		JSONObject obj = new JSONObject();
		if (request.getSession(false) == null) {
			obj.put("status", false);
			obj.put("message", "Invalid Session");
			return obj;
		}else 
		{
			request.getSession(false).invalidate();
			obj.put("status", true);
			obj.put("data", "sucessfully logged out");
			return obj;
		}
	}
	
	public static JSONArray seeMyPosts(String id, int offset, int limit){
		JSONArray json = new JSONArray();
		try (
		    Connection conn = DriverManager.getConnection(
		    		connString, userName, passWord);
		    PreparedStatement postSt = conn.prepareStatement("select postid,timestamp,uid,text,hasimg from post where post.uid = ? order by timestamp desc offset ? limit ?");
		)
		{
			postSt.setString(1, id);
			postSt.setInt(2, offset);
			postSt.setInt(3, limit);
			ResultSet rs = postSt.executeQuery();
			conn.close();
			json = ResultSetConverter(rs);			
			return json;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
		
	}
	
	public static JSONArray seeUserPosts(String id, int offset, int limit){
		JSONArray json = new JSONArray();
		try (
		    Connection conn = DriverManager.getConnection(
		    		connString, userName, passWord);
		    PreparedStatement postSt = conn.prepareStatement("select postid,timestamp,uid,text,hasimg from post where post.uid = ? order by timestamp desc offset ? limit ?");
		)
		{
			postSt.setString(1, id);
			postSt.setInt(2, offset);
			postSt.setInt(3, limit);
			ResultSet rs = postSt.executeQuery();
			conn.close();
			json = ResultSetConverter(rs);			
			return json;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
		
	}
	
	public static JSONArray seePosts(String id, int offset, int limit){
		JSONArray json = new JSONArray();
		try (
		    Connection conn = DriverManager.getConnection(
		    		connString, userName, passWord);
			PreparedStatement postSt = conn.prepareStatement("select postid,timestamp,uid,text,hasimg from post where post.uid in (select uid2 from follows where uid1 = ? UNION select uid from \"user\" where uid=? ) order by timestamp asc offset ? limit ?");
		)
		{	
			postSt.setString(1, id);
			postSt.setString(2,id);
			postSt.setInt(3, offset);
			postSt.setInt(4, limit);
			ResultSet rs = postSt.executeQuery();
			json = ResultSetConverter(rs);
			return json;
		} catch (SQLException | JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	private static JSONArray ResultSetConverter(ResultSet rs) throws SQLException, JSONException {
		
		// TODO Auto-generated method stub
		JSONArray json = new JSONArray();
	    ResultSetMetaData rsmd = rs.getMetaData();
	    while(rs.next()) {
	        int numColumns = rsmd.getColumnCount();
	        JSONObject obj = new JSONObject();
	        int postid=-1;
	        for (int i=1; i<numColumns+1; i++) {
	          String column_name = rsmd.getColumnName(i);

	          if(rsmd.getColumnType(i)==java.sql.Types.ARRAY){
	           obj.put(column_name, rs.getArray(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT){
	           obj.put(column_name, rs.getInt(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN){
	           obj.put(column_name, rs.getBoolean(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.BLOB){
	           obj.put(column_name, rs.getBlob(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE){
	           obj.put(column_name, rs.getDouble(column_name)); 
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT){
	           obj.put(column_name, rs.getFloat(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER){
	           obj.put(column_name, rs.getInt(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR){
	           obj.put(column_name, rs.getNString(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
	           obj.put(column_name, rs.getString(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT){
	           obj.put(column_name, rs.getInt(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT){
	           obj.put(column_name, rs.getInt(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.DATE){
	           obj.put(column_name, rs.getDate(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP){
	           obj.put(column_name, rs.getTimestamp(column_name));   
	          }
	          else{
	           obj.put(column_name, rs.getObject(column_name));
	          }
	          
	          if(column_name.equals((String)"postid"))
	          {
	        	  postid = rs.getInt(column_name);
	        	  
	          }
	          
	        }
	        json.put(obj);
	        if(postid!=-1)
	        {
	       	     JSONArray comObj = getComments(postid);
	       	     obj.put("Comment", comObj);
	        }
	       
	      }
	    return json;
	}
	
	public static JSONArray getComments(int postid){
		JSONArray json = new JSONArray();
		try (
			    Connection conn = DriverManager.getConnection(
			    		connString, userName, passWord);
			    PreparedStatement commSt = conn.prepareStatement("select timestamp,comment.uid, name, text from comment,\"user\" as us where postid = ? and us.uid=comment.uid order by timestamp asc")
			    		
			)
		{
				commSt.setInt(1, postid);
				ResultSet rs = commSt.executeQuery();
				json = ResultSetConverter(rs);
				return json;
		} catch (SQLException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
		
	}
	
	public static JSONObject follow(String uid1,String uid2) throws JSONException
	{
		JSONObject obj = new JSONObject();
		try (
			    Connection conn = DriverManager.getConnection(
			    		connString, userName, passWord);
			    PreparedStatement commSt = conn.prepareStatement("insert into follows values(?,?)");
			    		
			)
		{
			commSt.setString(1, uid1);
			commSt.setString(2, uid2);
			if(commSt.executeUpdate()>0)
			{
				obj.put("status", true);
				obj.put("data", "user followed " + uid2);
			}
			else
			{
				obj.put("status", false);
				obj.put("message", "could not follow");
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			obj.put("status", false);
			obj.put("message", "Already followed");
		}
		return obj;
	}
	
	public static JSONObject unfollow(String uid1,String uid2) throws JSONException
	{
		JSONObject obj = new JSONObject();
		try (
			    Connection conn = DriverManager.getConnection(
			    		connString, userName, passWord);
				PreparedStatement check = conn.prepareStatement("select * from follows where uid1=? and uid2=?"); 
			    		
			)
		{
			check.setString(1, uid1);
			check.setString(2, uid2);
			ResultSet result =  check.executeQuery();
			if(result.next())
			{
				PreparedStatement commSt = conn.prepareStatement("delete from follows where uid1=? and uid2=?");
				commSt.setString(1, uid1);
				commSt.setString(2, uid2);
				if(commSt.executeUpdate()>0)
				{
					obj.put("status", true);
					obj.put("data", "unfollowed "+uid2);
				}
				else
				{
					obj.put("status", false);
					obj.put("message", "could not unfollow");
					
				}
			}
			else
			{
				obj.put("status", false);
				obj.put("message", "user not followed");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}
	
	public static JSONArray getSuggestion(String search)
	{
		JSONArray jsonToSend = new JSONArray();
		if(search.length()<3)
			return jsonToSend;
		try (
			    Connection conn = DriverManager.getConnection(
			    		connString, userName, passWord);
				PreparedStatement commSt = conn.prepareStatement("select name,uid,email from \"user\" where name like ? or uid like ? or email like ? limit 10");
			)
		{
			
			
			search = "%" + search + "%";
			commSt.setString(1, search);
			commSt.setString(2, search);
			commSt.setString(3, search);
			ResultSet rset = commSt.executeQuery();
			jsonToSend.put(ResultSetConverter(rset));			
			return jsonToSend;
		} 
				
		catch (SQLException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonToSend;
	}
	
	public static JSONObject isValidUser(String uid){
		JSONArray jsonToSend = new JSONArray();
		try (
			    Connection conn = DriverManager.getConnection(
			    		connString, userName, passWord);
				PreparedStatement commSt = conn.prepareStatement("select uid,name,email from \"user\" where uid = ?");
			)
		{
			commSt.setString(1, uid);
			ResultSet rset = commSt.executeQuery();
			jsonToSend.put(ResultSetConverter(rset));
		} 
				
		catch (SQLException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(jsonToSend.length() != 0)
			return jsonToSend.getJSONObject(0);
		else
			return null;
	}
	
	public static boolean isFollowUser(String id1, String id2){
		try (
			    Connection conn = DriverManager.getConnection(
			    		connString, userName, passWord);
				PreparedStatement commSt = conn.prepareStatement("select uid1 from follows where uid1 = ? and uid2 = ?");
			)
		{
			commSt.setString(1, id1);
			commSt.setString(2, id2);
			ResultSet rset = commSt.executeQuery();
			while(rset.next()){
				return true;
			}
		} 
				
		catch (SQLException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public static JSONObject insertImage(String filename, InputStream is, long length){
		JSONObject obj = new JSONObject();
		try{   
			Connection conn = DriverManager.getConnection(connString, userName, passWord);
			PreparedStatement pStmt = conn.prepareStatement("insert into image(postid, name, img) values(?,?,?);");
			PreparedStatement pStmt2 = conn.prepareStatement("select last_value from postid;");
			
			pStmt2.executeQuery();
			ResultSet rset = pStmt2.getResultSet();
			int postid = 0;
			while(rset.next()){
				postid = rset.getInt("last_value");
			}
			pStmt.setInt(1, postid);
			pStmt.setString(2, filename);
			
			if(length != 0 && is != null){
				pStmt.setBinaryStream(3, is, length);
				is.close();
			}
			else{
				pStmt.setBinaryStream(3, null);
			}
			
			if(pStmt.executeUpdate()>0)
			{
				obj.put("status", true);
				obj.put("data","Uploaded Image");				
			}
			else
			{
				obj.put("status",false);
				obj.put("message", "Unable to upload image");
			}	
			}catch (Exception sqle)
			{
				sqle.printStackTrace();
			}
		return obj;
	}
	
	public static void sendImage(int postid, OutputStream out) throws IOException{
		try (
			    Connection conn = DriverManager.getConnection(
			    		connString, userName, passWord);
				PreparedStatement commSt = conn.prepareStatement("select img from image where postid = ?");
			)
		{
			
			commSt.setInt(1, postid);
			ResultSet rset = commSt.executeQuery();
			while(rset.next()){
				byte[] imgBytes = rset.getBytes("img");
				out.write(imgBytes);
				out.flush();
			}
		} 
				
		catch (SQLException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
}
