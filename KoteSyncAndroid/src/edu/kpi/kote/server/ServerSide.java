package edu.kpi.kote.server;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ServerSide implements Runnable {

  public interface ReceiverListener {
    void dataReceived(byte [] data);
  }

//	private static final String key = "HG58YZ3CR9";

	private final Socket socket;
	private final ReceiverListener listener;

	public static ServerSide processConnection(final Socket socket, final ReceiverListener listener) {
	  return new ServerSide(socket, listener);
	}

	private ServerSide(final Socket socket, final ReceiverListener listener) {
		this.socket = socket;
		this.listener = listener;
		final Thread tr = new Thread(this);
		tr.start();
	}

	@Override
  public void run() {
		try {
			final InetAddress inetAddress = socket.getInetAddress();
			final String hostName = inetAddress.getHostName();

			System.out.println("New client logged from " + hostName);

			final InputStream inputStream = socket.getInputStream();
			final DataInputStream dis = new DataInputStream(inputStream);

			final List<Byte> listData = new LinkedList<Byte>();
			int curByte;
			while ((curByte = dis.read()) >= 0) {
				listData.add((byte) curByte);
			}

//			final byte[] decryptedFile = TripleDESEncryptor.decrypt(transmitedFile,
//					key);

			final byte [] data = new byte[listData.size()];
			int i = 0;
			for (final Byte b : listData) {
			  data[i] = b;
			  i++;
			}

			if (listener != null) {
			  listener.dataReceived(data);
			}

			dis.close();
			socket.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
