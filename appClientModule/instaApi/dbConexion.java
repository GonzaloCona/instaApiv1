package instaApi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbConexion {
private static Connection con=null;
	
	public static Connection getConnection(){
		try{
			if(con==null){
				String driver="com.mysql.jdbc.Driver";
				String url="jdbc:mysql://localhost/4fimi?autoReconnect=true";
				//String pwd="1234";
				String pwd="QazWsxEdc123";
				String usr="root";
				Class.forName(driver);
				con=DriverManager.getConnection(url,usr,pwd);
				//System.out.println("Connectionesfull");
			}
		}catch(ClassNotFoundException | SQLException ex){
			ex.printStackTrace();
		}
		return con;
	}
}
