package edu.kpi.kote.activity;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.MessageFormat;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.kpi.kote.R;
import edu.kpi.kote.client.ClientSide;
import edu.kpi.kote.registration.Registrator;
import edu.kpi.kote.server.ServerSide;
import edu.kpi.kote.server.ServerSide.ReceiverListener;

/**
 * Application main (and single (: ) screen.
 */
public class MainActivity extends Activity {
  /** Chat log bundle key: we use it when save chat log during activity destruction-creation. */
  private static final String CONVERSATION_KEY = "conversation_key";
  /** Chat current message bundle key: we use it when save chat log during activity destruction-creation. */
  private static final String CHAT_EDIT_KEY = "chat_edir_key";
  /** Server started flag bundle key: we use it when save chat log during activity destruction-creation. */
  private static final String SERVER_STARTED_KEY = "server_started_key";

  /** Application will start server on this port. */
  public static final int SERVER_PORT = 4001;

  /** Name of current device. */
  final static String machine1 = "device1";
  /** Name of device to which we will send messages. */
  final static String machine2 = "device2";

  // Main screen views
  private TextView chatView;
  private EditText chatEdit;
  private Button chatButton;

  // Chat log
  private StringBuilder conversation;

  // This one of the activity lifecycle stages where we can set activity layout, prepare server and initialize
  // other stuff
  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set activity layout
    setContentView(R.layout.activity_main);

    // We should find views in our activity
    chatView = (TextView) findViewById(R.id.chat_text);
    chatEdit = (EditText) findViewById(R.id.chat_edit);
    chatButton = (Button) findViewById(R.id.chat_button);

    conversation = new StringBuilder();

    // If this activity is being created not in the first time we should restore its state
    if (savedInstanceState != null) {
      if (savedInstanceState.getString(CONVERSATION_KEY) != null) {
        final String savedConversation = savedInstanceState.getString(CONVERSATION_KEY);
        conversation = new StringBuilder(savedConversation);
        chatView.setText(savedConversation);
      }

      if (savedInstanceState.getString(CHAT_EDIT_KEY) != null) {
        final String savedChatEdit = savedInstanceState.getString(CHAT_EDIT_KEY);
        conversation = new StringBuilder(savedChatEdit);
        chatEdit.setText(savedChatEdit);
      }
    }

    // Click on the "Send" button triggers message sending
    chatButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(final View v) {
        // We shouldn't sent empty messages
        if (!"".equals(chatEdit.getText().toString().trim())) {
          // Android forbids any work with network in the main GUI thread, because such work can make application choppy
          new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(final Void... params) {
              // This stuff will be executen in the background thread
              Log.i("KOTE", "Start transmittion");

              final String clientLogin = machine1;
              final String clientPassword = "pass";

              final String deviceTo = machine2;

              final ClientSide client = new ClientSide(clientLogin, clientPassword);
              final String sentText = chatEdit.getText().toString();
              client.startTransmission(deviceTo, sentText.getBytes());
              return sentText;
            }

            @Override
            protected void onPostExecute(final String result) {
              // This code will be executed in the GUI thread, so there we can set some view parameters. For example,
              // we can set text in text fields.
              chatEdit.setText("");
              conversation.append("<- ");
              conversation.append(result);
              conversation.append("\n");
              chatView.setText(conversation);

            }
          }.execute();
        }
      }
    });

    // If server was started already we should do nothing, else we should start server
    if (savedInstanceState != null && !savedInstanceState.getBoolean(SERVER_STARTED_KEY)) {
      return;
    }

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

  public void startServer() throws SocketException, Exception {
    // SERVER
    final String serverLogin = machine1;
    final String serverPassword = "pass";

    notifyIAmHere(serverLogin, serverPassword);

    final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

    final ReceiverListener listener1 = new ReceiverListener() {
      @Override
      public void dataReceived(final byte[] data) {
        conversation.append("-> ");
        conversation.append(new String(data));
        conversation.append("\n");
        chatView.post(new Runnable() {
          @Override
          public void run() {
            chatView.setText(conversation);
          }
        });
        Log.i("KOTE", new String(data));
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
    Log.i("KOTE", MessageFormat.format("notifyIAmHere({0}:{1})", addr, SERVER_PORT));
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  @Override
  protected void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putString(CONVERSATION_KEY, conversation.toString());
    outState.putString(CHAT_EDIT_KEY, chatEdit.getText().toString());
    outState.putBoolean(SERVER_STARTED_KEY, true);
  }
}
