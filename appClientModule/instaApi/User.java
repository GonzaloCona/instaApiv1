package instaApi;

public class User {

	private int id;
	private String nombre;
	private String apellido;
	private String keyMovil;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getApellido() {
		return apellido;
	}
	public void setApellido(String apellido) {
		this.apellido = apellido;
	}
	
	public String getKeyMovil() {
		return keyMovil;
	}
	public void setKeyMovil(String keyMovil) {
		this.keyMovil = keyMovil;
	}
	public User(){
		
	}
	public User(int id,String nombre, String apellido,String keyMovil){
		this.id=id;
		this.nombre=nombre;
		this.apellido=apellido;
		this.keyMovil=keyMovil;
	}
}
