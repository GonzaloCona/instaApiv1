package instaApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;


import instaApi.User;
import instaApi.dbConexion;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ApiInstagramSearch {

	final static String searchURL = "https://api.instagram.com/v1/users/";
	//final static String apiKey="/media/recent/?access_token=6394138.1677ed0.d1802576372c463bb3444075643c70d2";//api del mes de mayo
	
	final static String apiKey="/media/recent/?access_token=6394138.1677ed0.d1802576372c463bb3444075643c70d2";
                                                         // 6394138.1677ed0.d1802576372c463bb3444075643c70d2
	
	
	// CODIGO INTAGRAM = 3
	public static void main(String[] args) throws Exception {
		 final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		    service.scheduleWithFixedDelay(new Runnable()
		      {
		        @Override
		        public void run()
		        {
		          System.out.println(" ************************************************* ");	
		          System.out.println(" ***********  Inicio Motor Instagram  ************ ");
		          System.out.println(new Date());
		          System.out.println(" ************************************************* ");
		          //llamo al flujo central
		          try {
					orquestador();
				} catch (NumberFormatException | UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(" ** problema ocurrido en el orquestador **");
				}
		        }
		      }, 0, 2, TimeUnit.MINUTES);

		
		
	}
	
	public static void orquestador() throws NumberFormatException, UnsupportedEncodingException {
		        System.out.println(" ************************************************* ");
		        System.out.println(" Lista de Usuarios ");
				int existeIn=0;
				//int count=0;
				List<User> listUser=new ArrayList<User>();
				//List<User> listUserIns=new ArrayList<User>();
				ResultSet rs,rs3;
				String sql;
				boolean existe;
				try{
					Connection c= dbConexion.getConnection();
					
					PreparedStatement st,st2;
					
					//saco usuarios de la ]BD
					st=c.prepareStatement("Select id_usuario,nom1,apell1,keyMovil from usuario");
					rs=st.executeQuery();
					while(rs.next()){
						User u=new User(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4));
						listUser.add(u);
						System.out.println("usuario: "+u.getNombre());
					}
					
					System.out.println(" ************************************************* ");
					
					//saco id red social de instagram usuario
					for(int i=0;i<listUser.size();i++){
						st=c.prepareStatement("Select id_usuario_red from rs_usuario where n_usuario_red<>'sin_info' and id_red_social=3 and id_usuario="+listUser.get(i).getId());
						rs=st.executeQuery();
						existeIn=0;
						while(rs.next()){
							
							String url = buildSearchString(rs.getString(1));
							//BUSCO!
							String result = search(url);
							System.out.println("");
							//TRABAJO SOBRE EL JSON
							try {			
								JSONObject resultj = new JSONObject(result);
								JSONArray datosResult = new JSONArray(resultj.getString("data"));
								 for (int j = 0; j < datosResult.length(); j++)
							     {
									 JSONObject objetoDato = new JSONObject(datosResult.getJSONObject(j).getString("comments"));
									 if(objetoDato.getInt("count")>0){
										 JSONArray Arraycomentarios = new JSONArray(objetoDato.getString("data"));
										 System.out.println(Arraycomentarios);
										 
										 //rescata palabras malas de 4fimi
										 sql = ("select palabra from palabras_fimi where tipo_palabra=1");
										 st2=c.prepareStatement(sql);
										 rs3=st2.executeQuery();
										 
										 while(rs3.next()){
											 for (int k = 0; k < Arraycomentarios.length(); k++)
										     {
												 existe=Arraycomentarios.getJSONObject(k).getString("text").contains(rs3.getString(1));
												 System.out.println("existe la palabra " + rs3.getString(1)+" en el comentario?: "+existe);
												 System.out.println("");
												 if(existe){
													 JSONObject user =new JSONObject(Arraycomentarios.getJSONObject(k).getString("from"));
													 existeIn=inserta(Arraycomentarios.getJSONObject(k),user.getString("username") ,Integer.parseInt(rs.getString(1)));
													 existe=false;
												 }		
										     }
											
										 }
										 	 
									}
									
									 
							     }
										
							
							} catch (JSONException e) {
								System.out.println(e.getMessage());
							}
							
							
						}
						//si se realizó algún insert al usuario
						if(existeIn==1){
							System.out.println("inserto, llama push" );
							String result=push(listUser.get(i).getKeyMovil());
							System.out.println("iresultado push:"+result );
						}			
					}
				}catch( SQLException ex){
					ex.getMessage();		
				}
	}
	
		
	public static int inserta(JSONObject jsonObj,String quien, int id) throws UnsupportedEncodingException{
		int i=0;
		Connection c= dbConexion.getConnection();
		PreparedStatement st;
		String sql,comentario;
	
		
		try{
			comentario=jsonObj.getString("text").replace("'", "");
			System.out.println("v2 comentario entrada"+comentario);
			byte[] byteText = comentario.getBytes(Charset.forName("UTF-8"));
			String comentario_aux= new String(byteText , "UTF-8");
			comentario=comentario_aux;
			System.out.println("v2 comentario salida"+comentario);
			
			sql = ("Insert into historial_usuario(id_usuario_red,id_red_social,comentario,tipo_comentario,fecha,id_onombre_quien_comenta,is_falso_positivo) values("+id+",3,'"+comentario+"','negativo',SYSDATE(),'"+quien+"',1 )");
			st=c.prepareStatement(sql);
			st.executeUpdate();
			System.out.println("");
			System.out.println("Comentario Insertado");
			System.out.print("Fecha de Insersion: ");
			System.out.print(new Date());
			System.out.println("");
			i=1;
		} catch (JSONException e) {
			System.out.println(e.getMessage());
		}catch( SQLException ex){
			System.out.println("");
			System.out.println("Insercion Duplicada: comentario ya existente");
			System.out.print("Fecha de Insersion Duplicada: ");
			System.out.print(new Date());
			System.out.println("");
		}
		return i;
	}
	public static void CalPush(String id){
		
		Sender sender = new Sender("AIzaSyAe_zNh_S3HkeeTV37Cd1NCoR8tTYqYT34");
		Message message = new Message.Builder()
			.addData("title","1")
			.addData("message", "Nuevo Mensaje")
			.addData("body","Nuevo mensaje desde Instagram")
		    .build();
		
		try {
			
			Result result = sender.send(message, id, 1);
			System.out.println("Message Result: "+result.toString());
			JSONObject jo =new JSONObject(message);
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
	}
	public static String push(String key){
		//String url = "http://192.168.30.206:7010/fimi_v0/webapi/u/SPush;id="+key+";cod=1;contenido=desde Instagram";
		String url = "http://localhost:8080/fimi_v0/webapi/u/SPush;id="+key+";cod=1;contenido=desde%20Instagram";
		String result = search(url);
		
		return result;
	}
	
	
	
	//SEARCH
	
	
	public static String search(String pUrl) {
		try {
				URL url = new URL(pUrl);
				HttpURLConnection connection2 = (HttpURLConnection) url.openConnection();
				connection2.setRequestMethod("GET");
				int responseCode = connection2.getResponseCode();	
				BufferedReader in = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
	
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Excepcion capturada");
		}
		return null;
	}
	
	private static String buildSearchString(String searchString) {
		String toSearch = searchURL + searchString + apiKey;
		System.out.println("Construction Search URL: " + toSearch);
		
		return toSearch;
	}


}
