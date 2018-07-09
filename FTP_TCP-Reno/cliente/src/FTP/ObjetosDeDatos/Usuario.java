package FTP.ObjetosDeDatos;

public class Usuario {

    private String usuario, clave;

    public Usuario(String usuario, String clave) {
        this.usuario = usuario;
        this.clave = clave;
    }

    public boolean authenticar(String usuario, String clave) {
        return this.usuario.equals(usuario) && this.clave.equals(clave);
    }

}
