package dao;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UserDAO class handles validation of user credentials
 * @author Gan Shen Ying
 */
public class UserDAO {
   /**
     * The validate method is used to check user's email and 
     * password matches email and password stored in database.
     * @param email This is the user's input email
     * @param password This is the user's input password
     * @return Return true if input matches database information
     */
    public static boolean validate(String email, String password){
        if(password.equals(retrievePassword(email))){
            return true;
        } else{
            return false;
        }
    }
    /**
     * The retrievePassword method is used to establish connection with 
     * database and retrieve password associated with the email entered
     * @param email This is the email to search in the database
     * @return Return password if the email exists in the database, empty 
     * string otherwise
     */
    public static String retrievePassword(String email){
        //Establishing connection to MySQL database
        try {
            Connection conn = ConnectionManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select password from demographics where email like ?");
            
            stmt.setString(1, email + "@%");
            ResultSet rs = stmt.executeQuery();
            
            if(rs.next()){
                return rs.getString("password"); 
            }
            ConnectionManager.close(conn, stmt, rs);
            
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
}
