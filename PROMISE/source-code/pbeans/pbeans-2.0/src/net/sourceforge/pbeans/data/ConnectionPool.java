package net.sourceforge.pbeans.data;

import java.util.*;
import java.sql.*;
import javax.sql.*;

import java.util.logging.*;

class ConnectionPool {
	private static final Logger logger = Logger.getLogger(ConnectionPool.class.getName());
	private final LinkedList availableConnections = new LinkedList();
	private final Collection connections = new HashSet();
	private final Object monitor = this;
	private final DataSource dataSource;

	private int maxConnections;
	private int timeout;

	public ConnectionPool (DataSource ds, int maxConnections, int timeout) {
		this.dataSource = ds;
		this.maxConnections = maxConnections;
		this.timeout = timeout;
	}

	public void setMaxConnections (int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public void setConnectionTimeout (int timeout) {
		this.timeout = timeout;
	}

	public ConnectionWrapper getConnectionWrapper() throws SQLException {
		for (;;) {
			try {
				ConnectionWrapper cw;
				synchronized (monitor) {
					cw = (ConnectionWrapper) availableConnections.removeFirst();
				}
				if (cw.isClosed() || cw.hasTimedOut()) {
					try {
						// This removes it from connections list.
						cw.destroy();
					} catch (SQLException se) {
						// ignore
					}
					continue;
				}
				cw.touch();
				return cw;
			} catch (NoSuchElementException nse) {
				if(logger.isLoggable(Level.INFO)) {
					logger.info("getConnectionWrapper(): Creating connections.");
				}
				ensureConnectionsExist();
			}
		}
	}

	private void ensureConnectionsExist() throws SQLException {
		for (;;) {
			synchronized (monitor) {
				if (this.connections.size() < this.maxConnections) {
					Connection c = this.dataSource.getConnection();
					ConnectionWrapper cw = new ConnectionWrapper (c);
					this.connections.add (cw);
					this.availableConnections.add (cw);
					break;
				}
				else {
					ConnectionWrapper[] cws = (ConnectionWrapper[]) this.connections.toArray(new ConnectionWrapper[0]);
					boolean closedSomething = false;
					for (int i = 0; i < cws.length; i++) {
						if (cws[i].isClosed()) {
							this.connections.remove (cws[i]);
							closedSomething = true;
							break;
						}
						else if (cws[i].hasTimedOut()) {
							try {
								// This removes it from connections list
								cws[i].destroy();
							} catch (SQLException se) {
								// ignore
							}
							closedSomething = true;
							break;
						}
					}
					if (!closedSomething) {
						throw new SQLException ("Connection pool has been maxed out. Increase maxConnections.");
					}
				}
			}
		}
	}

	class ConnectionWrapper {
		private final Connection connection;
		private volatile long lastTouched;

		public ConnectionWrapper (Connection c) {
			this.connection = c;
			touch();
		}

		public void touch() {
			this.lastTouched = System.currentTimeMillis();
		}

		public boolean hasTimedOut() {
			return System.currentTimeMillis() - this.lastTouched >= timeout;
		}

		public void release() {
			synchronized (monitor) {
				availableConnections.add (this);
			}
		}

		public boolean isClosed() throws SQLException {
			return connection.isClosed();
		}

		public void destroy() throws SQLException {
			try {
				this.connection.close();
			} finally {
				synchronized (monitor) {
					connections.remove(this);
				}				
			}
		}

		public PreparedStatement prepareStatement (String sql) throws SQLException {
			touch();
			return this.connection.prepareStatement (sql);
		}
		
		public PreparedStatement prepareStatement(String sql, int resultSetType,
                int resultSetConcurrency) throws SQLException {
			touch();
			return this.connection.prepareStatement (sql, resultSetType, resultSetConcurrency);
		}				

		
		public Connection getConnection() {
			return this.connection;
		}
	}
}
