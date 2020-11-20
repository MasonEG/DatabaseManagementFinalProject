import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class main {
    public static String[] loginDialog() {
        String result[] = new String[2];
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbUsername = new JLabel("Username: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbUsername, cs);

        JTextField tfUsername = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(tfUsername, cs);

        JLabel lbPassword = new JLabel("Password: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbPassword, cs);

        JPasswordField pfPassword = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(pfPassword, cs);
        panel.setBorder(new LineBorder(Color.GRAY));

        String[] options = new String[] { "OK", "Cancel" };
        int ioption = JOptionPane.showOptionDialog(null, panel, "Login", JOptionPane.OK_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (ioption == 0) // pressing OK button
        {
            result[0] = tfUsername.getText();
            result[1] = new String(pfPassword.getPassword());
        }
        return result;
    }

    /**
     * @param stmt
     * @param sqlQuery
     * @throws SQLException
     */
    private static void runQuery(Statement stmt, String sqlQuery) throws SQLException {
        ResultSet rs;
        ResultSetMetaData rsMetaData;
        String toShow;
        rs = stmt.executeQuery(sqlQuery);
        rsMetaData = rs.getMetaData();
        System.out.println(sqlQuery);
        toShow = "";
        while (rs.next()) {
            for (int i = 0; i < rsMetaData.getColumnCount(); i++) {
                toShow += rs.getString(i + 1) + ", ";
            }
            toShow += "\n";
        }
        JOptionPane.showMessageDialog(null, toShow);
    }

    /**
     * Show an example of an insert statement
     * @param conn Valid database connection
     * 		  fname: Name of a food to check
     */
    private static void insertFood(Connection conn, String fname) {

        if (conn==null || fname==null) throw new NullPointerException();
        try {
            // we want to make sure that all the query and update statements
            // are considered as one unit; both got done or none got done

            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            ResultSet rs;
            int id=0;

            // get the maximum id from the food table
            rs = stmt.executeQuery("select max(fid) from food");
            while (rs.next()) {
                id = rs.getInt(1);
            }
            rs.close();
            stmt.close();


            PreparedStatement inststmt = conn.prepareStatement(
                    " insert into food (fid,fname) values(?,?) "); //two parameters are needed, and we set them in line 106 and 108

            // first column has the new food id that is unique
            inststmt.setInt(1, id+1);
            // second ? has the food name
            inststmt.setString(2, fname);

            int rowcount = inststmt.executeUpdate();

            System.out.println("Number of rows updated:" + rowcount);
            inststmt.close();
            // confirm that these are the changes you want to make
            conn.commit();
            // if other parts of the program needs commit per SQL statement
            // conn.setAutoCommit(true);
        } catch (SQLException e) {}

    }

	/* this example shows how to use ? to give a different value
	 to a query
	 @param conn: Valid connection to a dbms
	        iname: the name of the ingredient to check
	*/

    private static void checkIngredient(Connection conn, String iname) {

        if (conn==null || iname==null) throw new NullPointerException();
        try {

            ResultSet rs =null;
            String toShow ="";

            PreparedStatement lstmt = conn.prepareStatement(
                    "select count(*) from ingredient where iname= ?");  //? is the part should be filled, we set it in line 142

            // clear previous parameter values
            lstmt.clearParameters();
            // first column has the new food id that is unique
            lstmt.setString(1, iname);

            // execute the query
            rs=lstmt.executeQuery();
            // advance the cursor to the first record

            rs.next();
            int count = rs.getInt(1);
            lstmt.close();

            System.out.println("count=" + count);

            if (count > 0) {
                toShow = "The ingredient " + iname + " exists";
            }
            else toShow = "The ingredient " + iname + " does not exist";

            JOptionPane.showMessageDialog(null, toShow);

        } catch (SQLException e) {}

    }

    public static void main(String[] args) {
        String dbServer = "jdbc:mysql://localhost:3306/kitchen?useSSL=false";
        // For compliance with existing applications not using SSL the verifyServerCertificate property is set to ‘false’,
        String userName = "";
        String password = "";

        String result[] = loginDialog();
        userName = result[0];
        password = result[1];

        Connection conn;
        Statement stmt;
        if (result[0]==null || result[1]==null) {
            System.out.println("Terminating: No username nor password is given");
            return;
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(dbServer, userName, password);
            stmt = conn.createStatement();
            String sqlQuery = "";

            String option = "";
            String instruction = "Enter 1: Find hashtags that appeared in the most number of states in a given year." + "\n"
                    + "Enter 2: Find users who used a given hashtag in a given state in a given month/year." + "\n"
                    + "Enter 3: Find most followed users in  a given party." + "\n"
                    + "Enter 4: Display rank of users based on retweet in a given month/year." + "\n"
                    + "Enter 5: Find users who were mentioned the most in tweets of users of a given party in a given month/year." + "\n"
                    + "Enter 6: Find most used hashtags." + "\n"
                    + "Enter 7: Insert new user." + "\n"
                    + "Enter 8: Delete an entire user." + "\n"
                    + "Enter 9: End program.";

            while (true) {
                option = JOptionPane.showInputDialog(instruction);
                if (option.equals("1")) {
                    String hashtagNum = JOptionPane.showInputDialog("Enter number of hashtags: ");
                    String yearNum = JOptionPane.showInputDialog("Enter year: ");
                } else if (option.equalsIgnoreCase("2")) {
                    sqlQuery = "select f.fname, count(r.iid), sum(r.amount) from food f inner join recipe r on r.fid = f.fid inner join ingredient i on i.iid = r.iid group by f.fname";
                    runQuery(stmt, sqlQuery);
                } else if (option.equals("3")) {
                    sqlQuery = "select f.fname from food f where f.fid not in (select r.fid from recipe r inner join ingredient i on i.iid = r.iid where i.iname = 'Green Onion');";
                    runQuery(stmt, sqlQuery);
                } else if (option.equals("4")) {
                    sqlQuery = "select i.iname, r.amount from food f inner join recipe r on r.fid = f.fid inner join ingredient i on i.iid = r.iid where f.fname = 'Pad Thai'";
                    runQuery(stmt, sqlQuery);
                } else if (option.equals("5")) {
                    String fname=JOptionPane.showInputDialog("Enter foodname:");
                    insertFood(conn, fname);
                } else if (option.equals("6")) {
                    String iname=JOptionPane.showInputDialog("Enter exact name of the ingredient to check:");
                    checkIngredient(conn, iname);
                }
                } else if (option.equals("7")) {
                    String iname=JOptionPane.showInputDialog("Enter exact name of the ingredient to check:");
                    checkIngredient(conn, iname);
                }
                else if (option.equals("8")) {
                    String iname=JOptionPane.showInputDialog("Enter exact name of the ingredient to check:");
                    checkIngredient(conn, iname);
                }
        else if (option.equals("9")){
                    break;
                }
            }

            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Program terminates due to errors");
            e.printStackTrace(); // for debugging
        }
    }

}
