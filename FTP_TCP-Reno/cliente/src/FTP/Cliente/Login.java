package FTP.Cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;

import static java.lang.System.exit;

public class Login extends JFrame {

    GridBagLayout gbl;
    GridBagConstraints gbc;

    JLabel jlTitulo, jlIPServidor, jlPuerto, jlUsuario, jlClave;
    JTextField jtfIPServidor, jtfPuerto, jtfUsuario;
    JPasswordField jpfClave;
    JButton jbSalir, jbLogin;

    public Login() throws HeadlessException {
        super("Login Cliente FTP");
        incializar();
    }

    public Login(int ultimo_puertoServidor) {
        super("Login Cliente FTP");
        incializar();
        jtfPuerto.setText(Integer.toString(ultimo_puertoServidor));
    }

    public Login(InetAddress ultimo_ipServidor) {
        super("Login Cliente FTP");
        incializar();
        jtfIPServidor.setText(ipAString(ultimo_ipServidor));
    }

    public Login(InetAddress ultimo_ipServidor, int ultimo_puertoServidor) {
        super("Login Cliente FTP");
        incializar();
        jtfIPServidor.setText(ipAString(ultimo_ipServidor));
        jtfPuerto.setText(Integer.toString(ultimo_puertoServidor));
    }

    private String ipAString(InetAddress ip) {
        String campo_direccion = "";
        byte[] direccion = ip.getAddress();
        boolean primer_octeto = true;
        String seperador, formato_byte;
        if (ip instanceof Inet4Address) {
            seperador = ".";
            formato_byte = "%d";
        } else if (ip instanceof Inet6Address) {
            seperador = ":";
            formato_byte = "%2x";
        } else {
            throw new InvalidParameterException("Version de IP disconocido.");
        }
        for (byte octeto : direccion) {
            if (primer_octeto) primer_octeto = false;
            else campo_direccion += seperador;
            campo_direccion += String.format(formato_byte, octeto);
        }
        return campo_direccion;
    }

    private void incializar() {
        gbl = new GridBagLayout();
        this.setLayout(gbl);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(300, 250);
        this.setLocationRelativeTo(null);

        final int COL_1 = 0, COL_2 = 1;
        int fila = 0;

        jlTitulo = new JLabel("Login FTP");
        gbc.gridx = COL_1;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        this.add(jlTitulo, gbc);
        

        jlIPServidor = new JLabel("IP Servidor:");
        gbc.gridx = COL_1;
        gbc.gridy = fila++;
        gbc.gridwidth = 1;
        this.add(jlIPServidor, gbc);

        jtfIPServidor = new JTextField(10);
        jtfIPServidor.setText("192.168.15.1");
        gbc.gridx = COL_2;
        this.add(jtfIPServidor, gbc);

        jlPuerto = new JLabel("Puerto:");
        gbc.gridx = COL_1;
        gbc.gridy = fila++;
        this.add(jlPuerto, gbc);

        jtfPuerto = new JTextField(10);
        jtfPuerto.setText("1532");
        gbc.gridx = COL_2;
        this.add(jtfPuerto, gbc);

        jlUsuario = new JLabel("Usuario:");
        gbc.gridx = COL_1;
        gbc.gridy = fila++;
        this.add(jlUsuario, gbc);

        jtfUsuario = new JTextField(10);
        gbc.gridx = COL_2;
        this.add(jtfUsuario, gbc);

        jlClave = new JLabel("Clave:");
        gbc.gridx = COL_1;
        gbc.gridy = fila++;
        this.add(jlClave, gbc);

        jpfClave = new JPasswordField(10);
        gbc.gridx = COL_2;
        this.add(jpfClave, gbc);

        jbSalir = new JButton("Salir");
        jbSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit(0);
            }
        });
        gbc.gridy = fila;
        gbc.gridx = COL_1;
        this.add(jbSalir, gbc);

        jbLogin = new JButton("Login");
        jbLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Cliente.usuario = Login.this.jtfUsuario.getText();
                Cliente.clave = new String(Login.this.jpfClave.getPassword());
                try {
                    Cliente.ipServidor = InetAddress.getByName(Login.this.jtfIPServidor.getText());
                } catch (UnknownHostException e1) {
                    Cliente.ipServidor = null;
                }
                try {
                    Cliente.puertoServidor = Integer.parseInt(Login.this.jtfPuerto.getText());
                } catch (NumberFormatException nfe) {
                    Cliente.puertoServidor = -1;
                }
                Login.this.dispose();
            }
        });
        gbc.gridx = COL_2;
        this.add(jbLogin, gbc);

        this.setVisible(true);
    }

}
