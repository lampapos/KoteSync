package edu.kpi.kote.activity;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import edu.kpi.kote.R;
import edu.kpi.kote.registration.Registrator;
import edu.kpi.kote.server.ServerSide;
import edu.kpi.kote.server.ServerSide.ReceiverListener;

public class MainActivity extends Activity {
  public static final int SERVER_PORT = 4001;

  public void startServer() throws SocketException, Exception {
    // SERVER
    final String serverLogin = "mihDev1";
    final String serverPassword = "pass";

    notifyIAmHere(serverLogin, serverPassword);

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

  public static void notifyIAmHere(final String login, final String password) throws SocketException, Exception {
    final String addr =  Registrator.getInternetAddress();
    Registrator.notifyICanHear(Registrator.CONTEXT_PATH, login, password, addr, Integer.toString(SERVER_PORT));
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

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
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }
}
