package edu.kpi.kote.client;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import edu.kpi.kote.registration.Registrator;


public class ClientSide {
//	private static final String key = "HG58YZ3CR9";

  private final String login;
  private final String password;

  public ClientSide(final String login, final String password) {
    this.login = login;
    this.password = password;
  }

	public void startTransmission(final String deviceTo, final byte[] data) {
	  try {
      final String deviceToConnect = "server";

      final String connectionInfo =
          Registrator.connectToDevice(Registrator.CONTEXT_PATH, login, password, deviceToConnect);

      String serverAddress = null;
      String serverPort = null;

      final JSONObject connectionInfoJSON = new JSONObject(connectionInfo);
      serverAddress = (String) connectionInfoJSON.get("address");
      serverPort = (String) connectionInfoJSON.get("port");
      System.out.println("Connecting to: " + serverAddress + ":" + serverPort);

      performFileTransmission(serverAddress, Integer.parseInt(serverPort), data);
    } catch (final JSONException e) {
      System.err.println("Server received incorrect JSON format.\n" + e.toString());
    } catch (final Exception e) {
      e.printStackTrace();
    }
	}

	public void performFileTransmission(final String serverAddress, final int serverPort, final byte[] data) throws Exception {
		final Socket clientSocket = new Socket(serverAddress, serverPort);
		System.out.println("Client started");

		final OutputStream outputStream = clientSocket.getOutputStream();
		final DataOutputStream dos = new DataOutputStream(outputStream);

//		final byte[] encryptedFile = TripleDESEncryptor.encrypt(fileAsByteArray,
//				key);

    for (int i = 0; i < data.length; i++) {
      dos.writeByte(data[i]);
    }
    dos.flush();
    dos.close();

    clientSocket.close();

		System.out.println("Client finished");
	}

}
