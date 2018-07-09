package FTP.Servidor;

import FTP.ObjetosDeDatos.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Servidor extends JFrame {

    static Servidor servidor;
    static final int PUERTO = 1532;

    ServerSocket ftpSocket;

    ArrayList<Usuario> usuarios = new ArrayList<>();

    GridBagLayout gbl;
    GridBagConstraints gbc;

    JButton jbIniciarServidor, jbBorrar;
    JTextArea jtaConsola;
    JScrollPane jspConsola;

    public Servidor() throws HeadlessException {
        super("Servidor FTP");
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        this.setLayout(gbl);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(500, 300);

        usuarios.add(new Usuario("admin", "admin"));
        usuarios.add(new Usuario("abc", "123"));

        final int COL_1 = 0, COL_2 = 1;
        int fila = 0;

        Runnable procesoEscuchar = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Socket cliente = null;
                    try {
                        cliente = ftpSocket.accept();
                        Servidor.this.imprimirConsola("\tCliente Connectado: " + cliente.getInetAddress() + ":" + cliente.getPort());
                        DataInputStream din = new DataInputStream(cliente.getInputStream());
                        DataOutputStream dout = new DataOutputStream(cliente.getOutputStream());
                        String usuario = null, clave = null;
                        while (usuario == null || clave == null) {
                            String entrada = din.readUTF();
                            if (entrada.startsWith("USER ")) usuario = entrada.length() >= 6 ? entrada.substring(5) : "";
                            else if (entrada.startsWith("PASS ")) clave = entrada.length() >= 6 ? entrada.substring(5) : "";
                            if (usuario != null && clave != null) {
                                if (!authenticarUsuario(usuario, clave)) {
                                    usuario = null;
                                    clave = null;
                                    dout.writeUTF("FALLA DE AUTENTICACION");
                                } else {
                                    dout.writeUTF("AUTENTICACION EXITOSA");
                                }
                                dout.flush();
                            }
                        }
                        new FTPServidor(cliente, din, dout);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread hiloEscuchar = new Thread(procesoEscuchar);

        jbIniciarServidor = new JButton("Inicia Servidor");
        jbIniciarServidor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ftpSocket = new ServerSocket(PUERTO);
                    Servidor.this.imprimirConsola("Servidor escuchando en " + PUERTO);
                    Servidor.this.imprimirConsola("Esperando conexiones...");
                    Servidor.this.jbIniciarServidor.setEnabled(false);
                    hiloEscuchar.start();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        gbc.gridy = fila;
        gbc.gridx = COL_1;
        this.add(jbIniciarServidor, gbc);

        jbBorrar = new JButton("Borrar");
        jbBorrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Servidor.this.jtaConsola.setText("");
            }
        });
        gbc.gridx = COL_2;
        this.add(jbBorrar, gbc);
        fila++;

        jtaConsola = new JTextArea(10, 30);
        jtaConsola.setEditable(false);
        jspConsola = new JScrollPane(jtaConsola, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        gbc.gridx = COL_1;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        this.add(jspConsola, gbc);

        this.setVisible(true);
    }

    public void imprimirConsola(String s) {
        jtaConsola.setText(jtaConsola.getText() + s + "\n");
    }

    private boolean authenticarUsuario(String usuario, String clave) {
        for (Usuario u: usuarios)
            if (u.authenticar(usuario, clave))
                return true;
        return false;
    }

    public static void main(String[] args) {
        servidor = new Servidor();
    }
}
