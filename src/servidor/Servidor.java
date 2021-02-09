package servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Servidor {
    
    static Vector<Jugadores> jugador = new Vector<>();
    static int i = 1;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        try {
            InetAddress host = null;
            try {
                host = InetAddress.getLocalHost();
                System.out.println("Esta es mi direccion IP: "+host.getHostAddress());
            } catch (UnknownHostException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            int puerto = 5000;
            ServerSocket serverSocket = new ServerSocket(puerto);
            Socket socket;
            
            while(true){
                socket = serverSocket.accept();
                System.out.println(jugador);
                System.out.println("Nuevo jugador recibido en "+socket);
                DataInputStream dataIS = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOS = new DataOutputStream(socket.getOutputStream());
                Jugadores match = new Jugadores(socket, "jugador " + i, dataIS, dataOS, i);
                System.out.println(jugador);
                Thread thread = new Thread(match);
                //Agremamos el jugador a la lista
                jugador.add(match);
                thread.start();
                i++;
            }
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class Jugadores implements Runnable{
    
    String name;
    DataInputStream dataIS;
    DataOutputStream dataOS;
    Socket socket;
    boolean estado;
    int turno = 0;
    int i;
    
    public Jugadores(Socket socket, String name, DataInputStream in, DataOutputStream out, int i){
        this.socket = socket;
        this.name = name;
        this.estado = true;
        this.i = i;
    }
    
    @Override
    public void run() {
        try{
            String msm = "";
            this.dataIS = new DataInputStream(socket.getInputStream());
            this.dataOS = new DataOutputStream(socket.getOutputStream());
        
            //Agregaremos al mensaje el indice del jugador contrincante
            if(i%2 == 1)
                msm += i+1;
            else
                msm += i-1;
            
            //Enviamos el turno y contrincante
            dataOS.writeUTF(msm);
            
            while(true){
                System.out.println("Esperando nuevo mensaje...");
                msm = dataIS.readUTF();
                
                if(msm.equals("*")){
                    System.out.println("He cerrados los puertos en *");
                    this.estado = false;
                    this.socket.close();
                    break;
                }
                
                StringTokenizer token = new StringTokenizer(msm, "/");
                String enviar = token.nextToken();
                String jugador = token.nextToken();
                System.out.println("Coordenadas a enviar "+enviar);
                                
                //Buscamos el jugador en la lista
                for(Jugadores buscar : Servidor.jugador){
                    if(buscar.name.equals(jugador) && buscar.estado == true){
                        dataOS = new DataOutputStream(socket.getOutputStream());
                        buscar.dataOS.writeUTF(enviar);
                        System.out.println("Mensaje enviado");
                        break;
                    }else
                        System.out.println("El jugador no se encuentra");
                }
            }
        }catch(Exception e){e.printStackTrace();};
        
        try{
            System.out.println("He cerrado conexion al final");
            this.dataIS.close();
            this.dataOS.close();
        }catch(IOException e){e.printStackTrace();}
    }   
}
