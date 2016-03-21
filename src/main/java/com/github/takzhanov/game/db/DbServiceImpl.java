package com.github.takzhanov.game.db;

import com.github.takzhanov.game.dao.UsersDao;
import com.github.takzhanov.game.domain.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbServiceImpl implements DbService {
    private final static Logger logger = LoggerFactory.getLogger(DbServiceImpl.class.getClass());
    private final Connection connection;

    static {
        try {
            DriverManager.registerDriver((Driver) Class.forName("org.h2.Driver").newInstance());
        } catch (SQLException | InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public DbServiceImpl() {
        this.connection = DbServiceImpl.getH2Connection();
    }

    @Override
    public void printConnectionInfo() {
        try {
            logger.info("DB name: " + connection.getMetaData().getDatabaseProductName());
            logger.info("DB version: " + connection.getMetaData().getDatabaseProductVersion());
            logger.info("Driver: " + connection.getMetaData().getDriverName());
            logger.info("Autocommit: " + connection.getAutoCommit());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UserProfile getUser(long id) throws DbException {
        try {
            return (new UsersDao(connection).getById(id));
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public UserProfile findUserByLogin(String login) throws DbException {
        try {
            UsersDao dao = new UsersDao(connection);
            return dao.getById(dao.findIdByLogin(login));
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public long addUser(UserProfile userProfile) throws DbException {
        try {
            connection.setAutoCommit(false);
            UsersDao dao = new UsersDao(connection);
            dao.createTable();
            dao.insertUser(userProfile);
            connection.commit();
            return dao.findIdByLogin(userProfile.getLogin());
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {
            }
            throw new DbException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
        }
    }

    @Override
    public void cleanUp() throws DbException {
        UsersDao dao = new UsersDao(connection);
        try {
            dao.dropTable();
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    static Connection getH2Connection() {
        try {
            String url = "jdbc:h2:./h2db";
            String name = "tully";
            String pass = "tully";

            return DriverManager.getConnection(url, name, pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}