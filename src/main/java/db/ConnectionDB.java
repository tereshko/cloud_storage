package db;

import org.sqlite.SQLiteConfig;
import utils.GetPropertieValue;

import java.sql.*;
import java.util.Random;


public class ConnectionDB {

    GetPropertieValue getPropertieValue = new GetPropertieValue();
    final private String url = getPropertieValue.getSQLLITE_URL();

    public Connection connect() {
        Connection conn = null;
        try {
            SQLiteConfig config = new SQLiteConfig();
            config.setReadOnly(false);
            conn = DriverManager.getConnection(url, config.toProperties());
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public int addUser(String username, String passwordSHA) {
        System.out.println("Add user in to DB");

        System.out.println("user: " + username);
        System.out.println("password: " + passwordSHA);
        ResultSet rsQuery = null;
        Connection conn = null;
        Statement stmt = null;
        boolean rs = false;

        conn = connect();

        Integer userID = -1;

        String query = "INSERT INTO users (username, password) VALUES ('" + username + "', '" + passwordSHA + "');";

        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            rs = stmt.execute(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //get ID of user from DB for return
        if (rs != true) {
            String queryForGetID = "SELECT ID FROM `users` WHERE username='" + username + "';";

            try {
                rsQuery = stmt.executeQuery(queryForGetID);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            try {
                if (rsQuery.next()) {
                    try {
                        userID = rsQuery.getInt("ID");
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            try {
                rsQuery.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        try {
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        try {
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        //Create folder for user
        //       createNewFolder(userID);

        System.out.println("Add user in to DB. User added. usernameID: " + userID);
        return userID;
    }

    public int getIDFromUsername(String username) {
        System.out.println("Get ID from Session use username from web");
        ResultSet rsQuery = null;
        Connection conn = null;
        Statement stmt = null;
        int userId = -1;

        conn = connect();

        String queryForGetUsernameTable = "SELECT `username` FROM `users`;";
        String queryForGetUsernameId = "SELECT ID FROM users WHERE username ='" + username + "';";

        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            rsQuery = stmt.executeQuery(queryForGetUsernameTable);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        while (true) {
            try {
                if (!rsQuery.next()) break;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            String str = null;

            try {
                str = rsQuery.getString("username");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            if (str.equals(username)) {
                try {
                    rsQuery = stmt.executeQuery(queryForGetUsernameId);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                try {
                    rsQuery.next();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                try {
                    userId = Integer.parseInt(rsQuery.getString("ID"));
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                break;
            }
        }

        try {
            rsQuery.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        try {
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        try {
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        System.out.println("Get ID from DB use username. ID is: " + userId);
        return userId;
    }

    public boolean comparePass(int ID, String passwordSHA256) {
        System.out.println("Compare password");

        ResultSet rsQuery = null;
        Connection conn = null;
        Statement stmt = null;

        conn = connect();

        String queryForGetPassword = "SELECT password FROM users WHERE ID ='" + ID + "';";

        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            rsQuery = stmt.executeQuery(queryForGetPassword);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        String userPasswordFromDB = null;

        try {
            if (rsQuery.next()) {
                try {
                    userPasswordFromDB = rsQuery.getString("password");
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            rsQuery.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        try {
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        try {
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        System.out.println("pass from DB: " + userPasswordFromDB);
        System.out.println("pass is come passwordSHA256: " + passwordSHA256);

        boolean isPasswordMatch = false;
        if (userPasswordFromDB == null) {
            isPasswordMatch = false;
        } else {
            isPasswordMatch = userPasswordFromDB.equals(passwordSHA256);
        }

        System.out.println("Compare password. Password is equals: " + isPasswordMatch);

        return isPasswordMatch;
    }

    public String createNewFolder(int ID) {

        String folder = folderGenerator(ID);
        String query = "INSERT INTO folders (ID, folder) VALUES ('" + ID + "', '" + folder + "');";

        Connection conn = null;
        Statement stmt = null;
        boolean rs = false;

        conn = connect();

        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            rs = stmt.execute(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        try {
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        System.out.println("Folder Created: " + folder);

        return folder;
    }

    public String getFolderName(int ID) {

        String queryForGetFolderName = "SELECT folder FROM folders WHERE ID ='" + ID + "';";

        ResultSet rsQuery = null;
        Connection conn = null;
        Statement stmt = null;
        String folderName = null;

        conn = connect();

        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            rsQuery = stmt.executeQuery(queryForGetFolderName);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            if (rsQuery.next()) {
                try {
                    folderName = rsQuery.getString("folder");
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            rsQuery.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        try {
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        try {
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return folderName;
    }

    private String folderGenerator(int ID) {
        String asciiUpperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String asciiLowerCase = asciiUpperCase.toLowerCase();
        String digits = "1234567890";
        String seedChars = asciiUpperCase + asciiLowerCase + digits;

        StringBuilder sb = new StringBuilder();
        int i = 0;
        int length = 17;
        Random rand = new Random();
        while (i < length) {
            sb.append(seedChars.charAt(rand.nextInt(seedChars.length())));
            i++;
        }
        return sb.toString() + ID;
    }

}
