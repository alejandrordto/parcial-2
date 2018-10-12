package com.mycompany.servidor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *clase que extiende un hilo, y que sera la que usaremos para usar el cliente
 * @author Alejandro Rodriguez
 */

public class Cargador implements Runnable{
    private Socket clientSocket;
    /**
     * metodo creador de el cargador
     * @param clientSocket representa el socket de cliente que se va a usar 
     */
    public Cargador(Socket clientSocket) {
        this.clientSocket = clientSocket;

    }
    /**
     * metodo en el cual se va a leer la peticion echa por el cliente, luego con el GET
     * se sacara el recurso pedido, si este no existe o no se coloca, se mandara por defecto
     * a una pagina de error, si hay un png el formato sera de png, y con un html el formato
     * sera de texto, se sacaran los bytes del archivo y de un encabezado, estos se juntaran
     * y el cliente los colocara en la pantalla.
     */
    public void run() {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;
            String inputu = null;
            String format = null;
            String data = null;
            byte[] bytes = null;
            while ((inputLine = in.readLine()) != null) {
                try {
                    if (inputLine.startsWith("GET")) {
                        inputu = inputLine;
                    }
                } catch (java.lang.StringIndexOutOfBoundsException e) {
                }
                //System.out.println(inputLine);
                if (!in.ready()) {
                    break;
                }
            }
            if (inputu != null) {
                String[] temp;
                temp = inputu.split(" ");
                String flag = "";
                try {
                if (temp[1].endsWith(".html")) {
                    bytes = Files.readAllBytes(new File("./" + temp[1].substring(1)).toPath());
                    data = "" + bytes.length;
                    format = "text/html";
                } else if (temp[1].endsWith("png")) {
                    bytes = Files.readAllBytes(new File("./" + temp[1].substring(1)).toPath());
                    data = "" + bytes.length;
                    format = "image/png";
                } else {
                    bytes = Files.readAllBytes(new File("./error.html").toPath());
                    data = "" + bytes.length;
                    format = "text/html";
                    
                }
                } catch (NoSuchFileException e){
                    bytes = Files.readAllBytes(new File("./error.html").toPath());
                    data = "" + bytes.length;
                    format = "text/html";
                }
                inputu = flag;
            }
            outputLine = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: "
                    + format
                    + "\r\n"
                    + "Content-Length: "
                    + data
                    + "\r\n\r\n";
            byte[] bite = outputLine.getBytes();
            try {
                byte[] salida = new byte[bytes.length + bite.length];
                for (int i = 0; i < bite.length; i++) {
                    salida[i] = bite[i];
                }
                for (int i = bite.length; i < bite.length + bytes.length; i++) {
                    salida[i] = bytes[i - bite.length];
                }
                clientSocket.getOutputStream().write(salida);
            } catch (java.lang.NullPointerException e) {

            }
            clientSocket.close();

        }
        catch (IOException ex) {
     Logger.getLogger(Cargador.class.getName()).log(Level.SEVERE, null, ex);
    }
    }
    

}