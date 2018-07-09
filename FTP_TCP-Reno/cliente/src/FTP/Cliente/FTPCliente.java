package FTP.Cliente;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class FTPCliente {

    Socket ClientSoc;
    DataInputStream din;
    DataOutputStream dout;
    BufferedReader br;

    FTPCliente(Socket soc) {
        try {
            ClientSoc = soc;
            din = new DataInputStream(ClientSoc.getInputStream());
            dout = new DataOutputStream(ClientSoc.getOutputStream());
            br = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception ex) {
        }
    }

    void SendFile(String filename) throws Exception // envio de archivos
    {

        File f = new File(filename);
        if (!f.exists()) {
            JOptionPane.showMessageDialog(null, "El archivo no existe...", "No existe", JOptionPane.ERROR_MESSAGE);
            dout.writeUTF("Archivo no encontrado");
            return;
        }

        dout.writeUTF(f.getName());

        String msgFromServer = din.readUTF();
        if (msgFromServer.compareTo("El archivo ya existe") == 0) {
            int opcion = JOptionPane.showConfirmDialog(null, "El archivo ya existe. ¿Desea sobrescribir?",
                    "Archivo existe en servidor", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (opcion == JOptionPane.YES_OPTION) {
                dout.writeUTF("Y");
            } else {
                dout.writeUTF("N");
                return;
            }
        }

        JOptionPane.showMessageDialog(null, "Enviando el archivo...", "Enviando...", JOptionPane.INFORMATION_MESSAGE);
        Cliente.cliente.jtxtArea.setText("");
        FileInputStream fin = new FileInputStream(f);

        XYSeries serie_ventana = new XYSeries("Ventana"), serie_umbral = new XYSeries("SSH"),
                serie_congestion = new XYSeries("Limite de Congestión");

        //Variables de RENO
        Random aleatorios = new Random();
        final int CONGESTION_MAXIMO = 100;
        int vent = 1, umbral = 32, cont = 1, aux = 1;
        int limite = CONGESTION_MAXIMO - aleatorios.nextInt(CONGESTION_MAXIMO - umbral + 1);
        final String CONGESTION = "Control de Congestión", RAPIDA = "Restransmisión rápida", LENTA = "Slow Start", TIEMPOFUERA = "Tiempo Fuera";


        int ch;
        do {
            ch = fin.read();
            if (vent < umbral) {
                if ((aux) == 1) Cliente.cliente.jTextAreaReno.append(LENTA + "\n");
                else Cliente.cliente.jTextAreaReno.append(RAPIDA + "\n");
                vent *= 2;
            } else if (limite > vent && vent >= umbral) {
                Cliente.cliente.jTextAreaReno.append(CONGESTION + "\n");
                vent++;
            } else if (limite <= vent) {
                Cliente.cliente.jTextAreaReno.append(TIEMPOFUERA + "\n");
                vent /= 2;
                umbral = limite / 2;
                limite = CONGESTION_MAXIMO - aleatorios.nextInt(CONGESTION_MAXIMO - umbral + 1);
            }
            Cliente.cliente.jTextAreaReno.append("CONTADOR: " + cont + "\tVENTANA: " + vent + "\tSSH: " + umbral + "\tCONGESTION: " + limite + "\n");
            serie_ventana.add(cont, vent);
            serie_umbral.add(cont, umbral);
            serie_congestion.add(cont, limite);
            cont++;
            dout.writeUTF(String.valueOf(ch));
            int ventana_indx = 1;
            Cliente.cliente.jtxtArea.append(din.readUTF() + "\n");
            while (ch != -1 && ventana_indx < vent) {
                ventana_indx++;
                ch = fin.read();
                dout.writeUTF(String.valueOf(ch));
                Cliente.cliente.jtxtArea.append(din.readUTF() + "\n");
            }
            aux++;
        } while (ch != -1);
        fin.close();
        XYSeriesCollection datos = new XYSeriesCollection();
        datos.addSeries(serie_ventana);
        datos.addSeries(serie_umbral);
        datos.addSeries(serie_congestion);
        JFreeChart grafo_reno = ChartFactory.createXYLineChart("FTP Reno", "Rafaga de Transmission", "Paquetes en la Ventana", datos, PlotOrientation.VERTICAL, true, true, false);
        ChartFrame ventana_grafo_reno = new ChartFrame("FTP Reno", grafo_reno);
        ventana_grafo_reno.pack();
        ventana_grafo_reno.setVisible(true);
    }

    void ReceiveFile(String file_remoto, String file_local) throws Exception // recibe archivos
    {
        String fileName = file_remoto;
        /*System.out.print("Introduzca el nombre del archivo :");
        fileName = br.readLine();*/
        dout.writeUTF(fileName);
        String msgFromServer = din.readUTF();

        if (msgFromServer.compareTo("Archivo no encontrado") == 0) {
            JOptionPane.showMessageDialog(null, "Archivo no encontrado en el servidor...", "Archivo no encontrado en Servidor", JOptionPane.ERROR_MESSAGE);
            return;
        } else if (msgFromServer.compareTo("LISTO") == 0) {
            JOptionPane.showMessageDialog(null,"Recepción de archivo...", "Recibiendo archivo", JOptionPane.INFORMATION_MESSAGE);
            File f = new File(file_local);
            if (f.exists()) {
                int opcion = JOptionPane.showConfirmDialog(null, "El archivo ya existe. ¿Desea sobrescribir?", "Archivo local ya existe", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (opcion == JOptionPane.NO_OPTION || opcion == JOptionPane.CLOSED_OPTION || opcion == JOptionPane.CANCEL_OPTION) {
                    dout.flush();
                    return;
                }
            }
            Cliente.cliente.jtxtArea.setText("");
            FileOutputStream fout = new FileOutputStream(f);
            int ch;
            String temp;
            do {
                temp = din.readUTF();
                ch = Integer.parseInt(temp);
                if (ch != -1) {
                    fout.write(ch);
                }
            } while (ch != -1);
            fout.close();
            Cliente.cliente.jtxtArea.append(din.readUTF());
        }

    }

    public void ListFiles() throws IOException {
        String archivo = din.readUTF();
        ArrayList<String> archivos = new ArrayList<>();
        archivos.add("");
        while (!archivo.equals("All files have been listed.")) {
            archivos.add(archivo);
            archivo = din.readUTF();
        }
        Cliente.cliente.jcbxLista.setModel(new DefaultComboBoxModel<>(archivos.toArray()));
    }

}
