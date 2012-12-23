package edu.kpi.kote.registration;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

public class Registrator {

//	public final static String CONTEXT_PATH = "http://localhost:8081";
	public final static String CONTEXT_PATH = "http://kote-obormote.appspot.com";

	public static void main(final String[] args) {
		try {
			final String contextPath = CONTEXT_PATH;

			final String login = "login";
			final String password = "password";
			final String deviceToConnect = "deviceTo";

			registerDevice(contextPath, login, password);

			final String connectionInfo =
				connectToDevice(contextPath, login, password, deviceToConnect);
			System.out.println(connectionInfo);

		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	public static String connectToDevice(final String contextPath, final String login,
			final String password, final String deviceToConnect) throws Exception {
		final String connectUrl = "/connect";
		final HashMap<String, String> connectParams = new HashMap<String, String>();
		connectParams.put("login", login);
		connectParams.put("password", password);
		connectParams.put("deviceTo", deviceToConnect);
		return performPost(contextPath + connectUrl, connectParams);
	}

	public static void registerDevice(final String contextPath, final String login,
			final String password) throws Exception {
		final String registerUrl = "/register";
		final HashMap<String, String> registerParams = new HashMap<String, String>();
		registerParams.put("login", login);
		registerParams.put("password", password);
		final String result = performPost(contextPath + registerUrl, registerParams);
		System.out.println("registerDevice: "+result);
	}

	public static void notifyICanHear(final String contextPath, final String login,
			final String password, final String address, final String port) throws Exception {
		final String icanhearUrl = "/icanhearon";
		final HashMap<String, String> icanhearParams = new HashMap<String, String>();
		icanhearParams.put("login", login);
		icanhearParams.put("password", password);
		icanhearParams.put("address", address);
		icanhearParams.put("port", port);
		final String result = performPost(contextPath + icanhearUrl, icanhearParams);
		System.out.println("notifyICanHear: "+result);
	}

	public int performGet(final String serviceUrl,
			final HashMap<String, String> params) throws Exception {
		HttpURLConnection conn = null;
		try {
			final StringBuilder sb = new StringBuilder();
			sb.append(serviceUrl);
			sb.append('?');
			final Iterator<String> iterator = params.keySet().iterator();
			while (iterator.hasNext()) {
				final String key = iterator.next();
				sb.append(key);
				sb.append("=");
				sb.append(params.get(key));
				if (iterator.hasNext()) {
					sb.append("&");
				}
			}
			final URL url = new URL(sb.toString());
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			final int code = conn.getResponseCode();
			return code;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	public static String performPost(final String serviceUrl,
			final HashMap<String, String> params) throws Exception {
		HttpURLConnection conn = null;
		try {
			final StringBuilder sb = new StringBuilder();
			final Iterator<String> iterator = params.keySet().iterator();
			while (iterator.hasNext()) {
				final String key = iterator.next();
				sb.append(key);
				sb.append("=");
				sb.append(params.get(key));
				if (iterator.hasNext()) {
					sb.append("&");
				}
			}
			final String urlParameters = sb.toString();
			final URL url = new URL(serviceUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length",
					"" + Integer.toString(urlParameters.getBytes().length));
			conn.setRequestProperty("Content-Language", "en-US");
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);

			// Send request
			final DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			final int code = conn.getResponseCode();
			if (code == 409){
				return "Device already registered.";
			}
			if (code != 200) {
				throw new IOException("Server responced error: " + code);
			}

			// Get Response
			final InputStream is = conn.getInputStream();
			final BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			final StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();

			return response.toString();


		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	public static String getExternalIP() throws Exception {
		final URL whatismyip = new URL(
				"http://automation.whatismyip.com/n09230945.asp");
		final URLConnection connection = whatismyip.openConnection();
		connection.addRequestProperty("Protocol", "Http/1.1");
		connection.addRequestProperty("Connection", "keep-alive");
		connection.addRequestProperty("Keep-Alive", "1000");
		connection.addRequestProperty("User-Agent", "Web-Agent");

		final BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		return in.readLine();
	}

	public static String getInternetAddress() throws SocketException {
		for (final Enumeration<NetworkInterface> interfaces = NetworkInterface
				.getNetworkInterfaces(); interfaces.hasMoreElements();) {
			final NetworkInterface cur = interfaces.nextElement();
			if (cur.isLoopback()) {
				continue;
			}
			for (final InterfaceAddress addr : cur.getInterfaceAddresses()) {
				if (addr == null) {
					continue;
				}
				final InetAddress inet_addr = addr.getAddress();
				if (!(inet_addr instanceof Inet4Address)) {
					continue;
				}
				return inet_addr.getHostAddress();
			}
		}

		return null;
	}
}
