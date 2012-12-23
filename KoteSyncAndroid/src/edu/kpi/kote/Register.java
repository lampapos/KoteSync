package edu.kpi.kote;

import java.net.SocketException;

import edu.kpi.kote.registration.Registrator;

public class Register {

  public static void registerDevice(final String login, final String password) throws SocketException, Exception {
    Registrator.registerDevice(Registrator.CONTEXT_PATH, login, password);
  }

  /**
   * @param args
   * @throws Exception
   * @throws SocketException
   */
  public static void main(final String[] args) throws SocketException, Exception {
    registerDevice("server", "pass");
  }

}
