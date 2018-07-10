package FTP.Cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static java.lang.System.exit;

public class Cliente extends JFrame{
    
    GridBagLayout gbl;
    GridBagConstraints gbc;

    JTextArea jtxtArea, jTextAreaReno;
    JScrollPane jspTxtArea, jspTextAreaReno;
    JComboBox jcbxComandos, jcbxLista ;
    JTextField jTxtArchivosLocal;
    JButton jbtnSeleccionar, jbtnEnviar;

    FTPCliente ftpCliente;
    boolean connectado = false, ftpConnectado = false;
    Socket conexion;
    DataInputStream din;
    DataOutputStream dout;

    Object lock;

    static Cliente cliente;

    public static String usuario, clave;
    public static InetAddress ipServidor;
    public static int puertoServidor;

    public InetAddress ultimo_ipServidor;
    public int ultimo_puertoServidor;

    public Cliente() throws HeadlessException {
        super("Cliente");
        gbl = new GridBagLayout();
        this.setLayout(gbl);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(730, 500);
        this.setLocationRelativeTo(null);
        boolean error_formato, autenticado = false;
        lock = new Object();
        try {
            do {
                error_formato = false;
                //Login login = new Login();
                //*
                final Login[] login = new Login[1];
                if (ultimo_ipServidor == null && ultimo_puertoServidor <= 0) {
                    login[0] = new Login();
                } else if (ultimo_ipServidor == null) {
                    login[0] = new Login(ultimo_puertoServidor);
                } else if (ultimo_puertoServidor <= 0) {
                    login[0] = new Login(ultimo_ipServidor);
                } else {
                    login[0] = new Login(ultimo_ipServidor, ultimo_puertoServidor);
                }
                Runnable loginVentana = new Runnable() {
                    @Override
                    public void run() {
                        synchronized (lock) {
                            while (login[0].isVisible()) {
                                try {
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            System.out.println("Fin de Lock");
                        }
                    }
                };
                Thread hiloLogin = new Thread(loginVentana);
                hiloLogin.start();//*/
                login[0].addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        synchronized (lock) {
                            login[0].setVisible(false);
                            lock.notify();
                        }
                    }
                });
              
                hiloLogin.join();
                if (ipServidor == null || puertoServidor == -1) {
                    if (ipServidor == null) JOptionPane.showMessageDialog(null, "Campo de IP llenado incorrectamente.", "Campo invalido.", JOptionPane.ERROR_MESSAGE);
                    if (puertoServidor == -1) JOptionPane.showMessageDialog(null, "Campo de Puerto llenado incorrectamente.", "Campo invalido.", JOptionPane.ERROR_MESSAGE);
                } else {
                    System.out.println("usuario = " + usuario);
                    System.out.println("clave = " + clave);
                    autenticado = autenticar();
                    if (autenticado) {
                        JOptionPane.showMessageDialog(null, "Usuario autenticado exitosamente.", "Usuario autenticado", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Credenciales invalidos.", "Credenciales invalidos.", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } while (error_formato || !autenticado);
        } catch (IOException e) {
            e.printStackTrace();
            exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final int COL_1 = 0, COL_2 = 1, COL_3 = 2;
        int fila = 0;

        jtxtArea = new JTextArea(10,60);
        jspTxtArea = new JScrollPane(jtxtArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        gbc.gridx = COL_1;
        gbc.gridy = fila++;
        gbc.gridwidth = 3;
        this.add(jspTxtArea, gbc);

        jTextAreaReno = new JTextArea(10,60);
        jspTextAreaReno = new JScrollPane(jTextAreaReno, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        gbc.gridx = COL_1;
        gbc.gridy = fila++;
        gbc.gridwidth = 3;
        this.add(jspTextAreaReno, gbc);

        jTxtArchivosLocal = new JTextField(60);
        gbc.gridx = COL_1;
        gbc.gridwidth = 3;
        gbc.gridy = fila++;
        this.add(jTxtArchivosLocal, gbc);

        jbtnSeleccionar = new JButton("Seleccionar Archivo");
        jbtnSeleccionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(Cliente.this.jTxtArchivosLocal.getText());
                jfc.setDialogTitle("Seleccionar Archivo Local");
                int opt = jfc.showDialog(null, "Abrir");
                if (opt == JFileChooser.APPROVE_OPTION) {
                    Cliente.this.jTxtArchivosLocal.setText(jfc.getSelectedFile().getAbsolutePath());
                }
            }
        });
        gbc.gridx = COL_3;
        gbc.gridy = fila++;
        this.add(jbtnSeleccionar, gbc);

        String[] comandos = {"Seleccionar", "Enviar", "Listar", "Salir"};
        jcbxComandos = new JComboBox(comandos);
        gbc.gridx = COL_1;
        gbc.gridwidth = 1;
        this.add(jcbxComandos,gbc);

        jbtnEnviar = new JButton("Enviar comando");
        jbtnEnviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    connectar();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    return;
                }
                String comando = (String) Cliente.this.jcbxComandos.getSelectedItem();
                try {
                    if (comando.equals("Enviar")) {
                        dout.writeUTF("SEND");
                        ftpCliente.SendFile(Cliente.this.jTxtArchivosLocal.getText());
                    } else if (comando.equals("Listar")) {
                        dout.writeUTF("LIST");
                        ftpCliente.ListFiles();
                    } else if (comando.equals("Salir")) {
                        dout.writeUTF("DISCONNECT");
                        System.exit(1);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        gbc.gridy = fila;
        gbc.gridx = COL_1;
        this.add(jbtnEnviar, gbc);

        String[] lista = {"" };
        jcbxLista = new JComboBox(lista);
        gbc.gridx = COL_2;
        this.add(jcbxLista, gbc);

        this.setVisible(true);

    }

    private boolean autenticar() throws IOException {
        if ((ultimo_ipServidor != null && !ultimo_ipServidor.equals(ipServidor)) || ultimo_puertoServidor != puertoServidor) {
            try {
                conexion = new Socket(ipServidor, puertoServidor);
                ultimo_ipServidor = ipServidor;
                ultimo_puertoServidor = puertoServidor;
                din = new DataInputStream(conexion.getInputStream());
                dout = new DataOutputStream(conexion.getOutputStream());
                connectado = true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "No se encuentra servidor", "Error de Conexion", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        dout.writeUTF("USER " + usuario);
        dout.writeUTF("PASS " + clave);
        dout.flush();
        return din.readUTF().equals("AUTENTICACION EXITOSA");
    }

    private void connectar() throws IOException {
        //Socket soc = new Socket(dominio, puerto);
        if (ftpConnectado) return;
        ftpCliente = new FTPCliente(conexion);
        ftpConnectado = true;
        //t.displayMenu();
    }

    public static void main(String[] args) {
        cliente = new Cliente();
    }
}
