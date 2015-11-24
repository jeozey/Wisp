package com.rj.connection;

public class SocketConnectionManager {

	private SocketConnectionManager() {
		if (socketConnectionPool == null) {
			socketConnectionPool = new SocketConnectionPool(new SocketConnectionFactory());
			System.out.println("Java Socket connection pool created.");
		}
	}

	private SocketConnectionPool socketConnectionPool;

	public static SocketConnectionManager getInstance() {
		return SocketConnectionManagerSingletonHolder.instance;
	}

	public SocketConnectionPool getSocketConnectionPool() {

		return socketConnectionPool;
	}

	private static class SocketConnectionManagerSingletonHolder {

		private static SocketConnectionManager instance = new SocketConnectionManager();
	}
}
