package edu.kpi.kote;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

import edu.kpi.kote.client.ClientSide;
import edu.kpi.kote.registration.Registrator;
import edu.kpi.kote.server.ServerSide;
import edu.kpi.kote.server.ServerSide.ReceiverListener;

/**
 * Entry point.
 * @author Pustovit Michael, pustovitm@gmail.com
 */
public class Main {

  public static final int SERVER_PORT = 8281;

  public static void notifyIAmHere(final String login, final String password) throws SocketException, Exception {
    Registrator.notifyICanHear(Registrator.CONTEXT_PATH, login, password, Registrator.getInternetAddress(), Integer.toString(SERVER_PORT));
  }

  final static String machine1 = "mihDev1";
  final static String machine2 = "mihDev2";

  public static void startServer() throws SocketException, Exception {
    // SERVER
    final String serverLogin = machine1;
    final String serverPassword = "pass";

//    notifyIAmHere(serverLogin, serverPassword);

    final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

    System.out.println("Server started on port " + SERVER_PORT);

    final ReceiverListener listener1 = new ReceiverListener() {
      @Override
      public void dataReceived(final byte[] data) {
        System.out.println("Received: " + new String(data));
      }
    };

    while (true) {
      final Socket clientSocket = serverSocket.accept();
      ServerSide.processConnection(clientSocket, listener1);
    }
  }

  public static void main(final String[] args) throws Exception {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          startServer();
        } catch (final Exception e) {
          e.printStackTrace();
        }
      }
    }).start();

    final Scanner scan = new Scanner(System.in);
    String line;
    while (!(line = scan.nextLine()).equals("exit")) {
      final String clientLogin = machine1;
      final String clientPassword = "pass";

      final String deviceTo = machine2;

      final ClientSide client = new ClientSide(clientLogin, clientPassword);
      client.startTransmission(deviceTo, line.getBytes());
    }

    scan.close();
  }

}
