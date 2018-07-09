package FTP.Servidor;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


public class FTPServidor extends Thread {   // transferencia de archivos

    Socket ClientSoc;
    DataInputStream din;
    DataOutputStream dout;

    public FTPServidor(Socket soc, DataInputStream din, DataOutputStream dout) {
        try {
            ClientSoc = soc;
            this.din = din;
            this.dout = dout;
            start();

        } catch (Exception ex) {
        }
    }

    public void SendFile() throws Exception {
        String filename = din.readUTF();
        File f = new File(filename);
        if (!f.exists()) {
            dout.writeUTF("Archivo no encontrado");
            return;
        } else {
            dout.writeUTF("LISTO");
            FileInputStream fin = new FileInputStream(f);
            int ch;
            do {
                ch = fin.read();
                dout.writeUTF(String.valueOf(ch));
            } while (ch != -1);
            fin.close();
            dout.writeUTF("Archivo recibido con exito...");
        }
    }

    public void ReceiveFile() throws Exception { // recibe archivos
        String filename = din.readUTF();

        File f = new File(filename);
        String option;

        if (f.exists()) {
            dout.writeUTF("El archivo ya existe");
            option = din.readUTF();
        } else {
            dout.writeUTF("Enviar archivo");
            option = "Y";
        }

        if (option.compareTo("Y") == 0) {
            FileOutputStream fout = new FileOutputStream(f);
            int ch;
            String temp;
            int aux = 1;
            do {
                temp = din.readUTF();
                ch = Integer.parseInt(temp);
                if (ch != -1) {
                    fout.write(ch);
                    String ack = "ACK: " + (aux + 1) + "  |  Seq: " + aux + " | |[datos de informacion] : " + ch;
                    aux++;
                    System.out.println(ack);
                    dout.writeUTF(ack);
                }

            } while (ch != -1);
            fout.close();
            dout.writeUTF("Archivo enviado con éxito...");
            Servidor.servidor.imprimirConsola("\tArchivo recibido");

        } else {
            return;
        }

    }

    public void run() {
        while (true) {
            try {
                //System.out.println("en espera de los comandos...");
                String Command = din.readUTF();
                if (Command.compareTo("GET") == 0) {
                    Servidor.servidor.imprimirConsola("\tGET Comando recibido...");
                    SendFile();
                    continue;
                } else if (Command.compareTo("LIST") == 0) {
                    Servidor.servidor.imprimirConsola("\tLIST Comando recibido...");
                    ListFiles();
                    continue;
                } else if (Command.compareTo("SEND") == 0) {
                    Servidor.servidor.imprimirConsola("\tSEND Comando recibido...");
                    ReceiveFile();
                    continue;
                } else if (Command.compareTo("DISCONNECT") == 0) {
                    Servidor.servidor.imprimirConsola("\tDISCONNECT Comando recibido...");
                    dout.close();
                    din.close();
                    ClientSoc.close();
                    //System.exit(1);
                }
            } catch (Exception ex) {
            }
        }
    }

    private void ListFiles() throws IOException {
        File dir = new File(".");
        File[] hijos = dir.listFiles();
        for (File hijo : hijos) {
            if (hijo.isFile()) {
                dout.writeUTF(hijo.getName());
                dout.flush();
            }
        }
        dout.writeUTF("All files have been listed.");
        dout.flush();
    }
}