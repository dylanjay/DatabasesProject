/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Messenger {
    
    //clear function 
    public static void clearTerminal(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Messenger
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Messenger (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Messenger

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
         if(outputHeader){
            for(int i = 1; i <= numCol; i++){
            System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();
            outputHeader = false;
         }
         for (int i=1; i<=numCol; ++i)
	   System.out.print(rs.getString (i).trim() + "\t" );
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
      // creates a statement object 
      Statement stmt = this._connection.createStatement (); 
 
      // issues the query instruction 
      ResultSet rs = stmt.executeQuery (query); 
 
      /* 
       ** obtains the metadata object for the returned result set.  The metadata 
       ** contains row and column info. 
       */ 
      ResultSetMetaData rsmd = rs.getMetaData (); 
      int numCol = rsmd.getColumnCount (); 
      int rowCount = 0; 
 
      // iterates through the result set and saves the data returned by the query. 
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>(); 
      while (rs.next()){
          List<String> record = new ArrayList<String>(); 
         for (int i=1; i<=numCol; ++i) 
            record.add(rs.getString (i)); 
         result.add(record); 
      }//end while 
      stmt.close (); 
      return result; 
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Messenger.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if
      
      //Greeting();
      Messenger esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Messenger object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Messenger (dbname, dbport, user, "");
	 String query = String.format("alter table message alter msg_timestamp set default now();");
	 esql.executeUpdate(query);

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            clearTerminal();
            Greeting();
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                clearTerminal();
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Access Contacts List");
                System.out.println("2. Access Blocks List");
                System.out.println("3. Start or leave a chat");
                System.out.println("4. Browse active chats");
                System.out.println("5. Delete account");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1:
                     boolean contactMenu = true;
                     while(contactMenu) {
                       clearTerminal();
                       System.out.println("Contacts Menu");
                       System.out.println("------------");
                       System.out.println("1. Browse Contacts List");
                       System.out.println("2. Add to Contacts List");
                       System.out.println("3. Remove from Contacts List");
                       System.out.println(".........................");
                       System.out.println("9. Go back to Main Menu");
                       switch(readChoice()){
                           case 1: ListContacts(esql, authorisedUser); break;
                           case 2: AddToContact(esql, authorisedUser); break;
                           case 3: RemoveContact(esql, authorisedUser); break;
                           case 9: contactMenu = false; break;
                           default: System.out.println("Unrecognized choice!"); break;
                       }
                     }
                     break;
                   case 2:
                     boolean blockMenu = true;
                     while(blockMenu) {
                       clearTerminal();
                       System.out.println("Contacts Menu");
                       System.out.println("------------");
                       System.out.println("1. Browse Blocks List");
                       System.out.println("2. Add to Blocks List");
                       System.out.println("3. Remove from Blocks List");
                       System.out.println(".........................");
                       System.out.println("9. Go back to Main Menu");
                       switch(readChoice()){
                           case 1: ListBlocks(esql, authorisedUser); break;
                           case 2: AddToBlock(esql, authorisedUser); break;
                           case 3: RemoveBlock(esql, authorisedUser); break;
                           case 9: blockMenu = false; break;
                           default: System.out.println("Unrecognized choice!"); break;
                       }
                     }
		     break;
                   case 3: StartOrLeaveChat(esql, authorisedUser); break;
		   case 4: ListChats(esql, authorisedUser); break;
     		   case 5: if(DeleteUser(esql, authorisedUser) == 1){usermenu = false;} break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main
  
   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(Messenger esql){
      try{
          clearTerminal();
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();

	 //Creating empty contact\block lists for a user
	 esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('block')");
	 int block_id = esql.getCurrSeqVal("user_list_list_id_seq");
         esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('contact')");
	 int contact_id = esql.getCurrSeqVal("user_list_list_id_seq");
         
	 String query = String.format("INSERT INTO USR (phoneNum, login, password, block_list, contact_list) VALUES ('%s','%s','%s',%s,%s)", phone, login, password, block_id, contact_id);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

  public static int DeleteUser(Messenger esql, String authorisedUser){
    try{
      System.out.print("\tAre you sure you wish to delete your account?(y/n): ");
      String answer = in.readLine();
      if(answer.equals("y"))
      {
	String query = String.format("SELECT l.member AS user FROM chat_list l WHERE l.member = '%s' UNION SELECT m.sender_login AS user FROM message m WHERE m.sender_login = '%s' UNION SELECT u.list_member AS user FROM user_list_contains u WHERE u.list_member = '%s'", authorisedUser, authorisedUser, authorisedUser);
	int userNum = esql.executeQuery(query);
	if(userNum < 1)
	{
	  query = String.format("DELETE FROM usr WHERE login = '%s'", authorisedUser);
	  esql.executeUpdate(query);
	  String output = String.format("\t%s succesfully removed user %s!\n", authorisedUser, authorisedUser);
	  System.out.print(output);
	  return 1;
	}
	else
	{
	  System.out.print("\tError, you have records referring to this account so cannot be deleted!\n");
	  return -1;
	}
	
      }
      return -1;
    }
    catch(Exception e){
      System.err.println(e.getMessage());
      return -1;
    }
  }
   
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Messenger esql){
      try{
          clearTerminal();
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
	 else
	   System.out.print("\tUser name or password entered is not valid\n");
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   public static void AddToContact(Messenger esql, String authorisedUser){
      // Your code goes here.
      try{
          clearTerminal();
          //ask user who to add
          System.out.println("\tEnter user id to add(blank to go back): ");
          String targetUser = in.readLine();
          
          //if blank user id, then we return back
          if(targetUser == ""){
              return;
          }
          
          //we need to validate if the user exists, return error if not
          String query = String.format("SELECT * FROM Usr WHERE login = '%s'", targetUser);
          int userNum = esql.executeQuery(query);
          if(userNum != 1){
              System.out.print("\tError, can not find user!\n");
              return;
          }
          
          //we need to get the contact list of the current user, and add the target to that list
          query = String.format("INSERT INTO user_list_contains (list_id, list_member) SELECT contact_list, '%s' FROM usr WHERE login = '%s'", targetUser, authorisedUser);
          esql.executeUpdate(query);
          String output = String.format("\t%s succesfully added user %s!\n", authorisedUser, targetUser);
          System.out.print(output);
          return;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }//end
  
  public static void AddToBlock(Messenger esql, String authorisedUser){
    try{
      //ask user who to add
      System.out.println("\tEnter user id to add(blank to go back): ");
      String targetUser = in.readLine();
      
      //if blank user id, then we return back
      if(targetUser == ""){
        return;
      }
      
      //we need to validate if the user exists, return error if not
      String query = String.format("SELECT * FROM Usr WHERE login = '%s'", targetUser);
      int userNum = esql.executeQuery(query);
      if(userNum != 1){
        System.out.print("\tError, can not find user!\n");
        return;
      }
      
      //we need to get the contact list of the current user, and add the target to that list
      query = String.format("INSERT INTO user_list_contains (list_id, list_member) SELECT block_list, '%s' FROM usr WHERE login = '%s'", targetUser, authorisedUser);
      esql.executeUpdate(query);
      String output = String.format("\t%s succesfully added user %s!\n", authorisedUser, targetUser);
      System.out.print(output);
      return;
    }
    catch(Exception e){
      System.err.println(e.getMessage());
      return;
    }
  }

   public static void ListContacts(Messenger esql, String authorisedUser){
     try{
         clearTerminal();
       String query = String.format("SELECT login, status FROM usr WHERE login IN (SELECT l.list_member FROM usr u, user_list_contains l WHERE u.login = '%s' AND u.contact_list = l.list_id)", authorisedUser);
       esql.executeQueryAndPrintResult(query);
       int num = esql.executeQuery(query);
       if(num < 1)
       {
	 System.out.print("\tContact List is empty\n");
	 return;
       }
     }
     catch(Exception e){
       System.err.println(e.getMessage());
       return;
     }
   }//end

  public static void ListBlocks(Messenger esql, String authorisedUser){
    try{
      clearTerminal();
      String query = String.format("SELECT login FROM usr WHERE login IN (SELECT l.list_member FROM usr u, user_list_contains l WHERE u.login = '%s' AND u.block_list = l.list_id)", authorisedUser);
      esql.executeQueryAndPrintResult(query);
      int num = esql.executeQuery(query);
       if(num < 1)
       {
	 System.out.print("\tContact List is empty\n");
	 return;
       }
    }
    catch(Exception e){
      System.err.println(e.getMessage());
      return;
    }
  }

  public static void ListChats(Messenger esql, String authorisedUser){
     try{
       String query = String.format("SELECT l.chat_id, l.member FROM chat_list l, chat c WHERE c.init_sender = '%s' AND c.chat_id = l.chat_id", authorisedUser);
       esql.executeQuery(query);
       int num = esql.executeQuery(query);
       if(num < 1)
       {
	 System.out.print("\tChat List is empty\n");
	 return;
       }
       ShowChat(esql, authorisedUser);
       boolean inBrowseChat = true;
       while (inBrowseChat){
	 System.out.print("\nEnter chat room id to view messages(blank to go back): ");
	 String targetChat = in.readLine();
	 
	 if (targetChat == "") {
	   inBrowseChat = false;
	   return;
	 }
	 
	 //check if the chat room exists
	 query = String.format("SELECT * FROM chat WHERE chat_id = %s AND init_sender = '%s'", targetChat, authorisedUser);
	 List<List< String >> result = esql.executeQueryAndReturnResult(query);
	 if(result.size() == 1){
	   MessageMenu(esql, authorisedUser, targetChat);
	   return;
	 }
	 else{
	   System.out.println("Error, you do not own chat.");
	 }
       }
     }
     catch(Exception e){
       System.err.println(e.getMessage());
       return;
     }
   }//end

  public static void MessageMenu(Messenger esql, String authorisedUser, String targetChat){
    try{
      boolean messageMenu = true;
      while(messageMenu) {
	System.out.println("Messages Menu");
	System.out.println("------------");
	System.out.println("1. View Messages");
	System.out.println("2. Add New Message");
	System.out.println("3. Edit Existing Message");
	System.out.println("4. Delete an Existing Message");
	System.out.println(".........................");
	System.out.println("9. Go back to Browse Chats");
	switch(readChoice()){
	case 1: PrintChats(esql, authorisedUser, targetChat); break;
	case 2: NewMessage(esql, authorisedUser, targetChat); break;
	case 3: EditMessage(esql, authorisedUser, targetChat); break;
	case 4: RemoveMessage(esql, authorisedUser, targetChat); break;
	case 9: messageMenu = false; break;
	default: System.out.println("Unrecognized choice!"); break;
	}
      }
    }
    catch(Exception e){
      System.err.println(e.getMessage());
      return;
    }
  }

  public static void EditMessage(Messenger esql, String authorisedUser, String targetChat){
    try{
      System.out.println("\tEnter message id to alter(blank to go back): ");
      String msg = in.readLine();

      if(msg.equals("")){
	return;
      }

      String query = String.format("SELECT sender_login FROM message WHERE chat_id = %d AND msg_id = %d", Integer.parseInt(targetChat), Integer.parseInt(msg));
      int num = esql.executeQuery(query);

      if(num != 1)
      {
	System.out.println("\tmessage you are trying to alter does not belong to you");
	return;
      }

      System.out.println("\tEnter new message(blank to go back): ");
      String text = in.readLine();

      if(text.equals("")){
	return;
      }

      query = String.format("UPDATE message SET msg_text = '%s' WHERE msg_id = %d", text, Integer.parseInt(msg));
      esql.executeUpdate(query);

      System.out.println("\tMessage was successfully altered");
    }
    catch(Exception e){
      System.err.println(e.getMessage());
      return;
    }
  }
  
  public static void RemoveMessage(Messenger esql, String authorisedUser, String targetChat){
    try{
      System.out.println("\tEnter message id to remove(blank to go back): ");
      String msg = in.readLine();

      if(msg.equals("")){
	return;
      }

      String query = String.format("SELECT sender_login FROM message WHERE chat_id = %d AND msg_id = %d", Integer.parseInt(targetChat), Integer.parseInt(msg));
      int num = esql.executeQuery(query);

      if(num != 1)
      {
	System.out.println("\tmessage you are trying to remove does not belong to you");
	return;
      }

      query = String.format("DELETE FROM message WHERE msg_id = %d", Integer.parseInt(msg));
      esql.executeUpdate(query);

      System.out.println("\tMessage was successfully removed");
    }
    catch(Exception e){
      System.err.println(e.getMessage());
      return;
    }
  }

  public static void PrintChats(Messenger esql, String authorisedUser, String chatId){
    try{
      //ask user what chat to open
      //System.out.println("\tEnter chat id to open(blank to go back): ");
      //String chat = in.readLine();
      
      //if blank chat id, then we return back
      //if(chat.equals( "")){
	//System.out.println("exit");
      //  return;
      //}
      boolean keep = true;
      int off = 0;
      while(keep)
      {
	//String query = String.format("SELECT msg_id, msg_text, msg_timestamp, sender_login FROM message WHERE chat_id = %d", Integer.parseInt(chatId) );
	String query = String.format("SELECT msg_id, msg_text, msg_timestamp, sender_login FROM message WHERE chat_id = %d ORDER BY msg_timestamp LIMIT 10 OFFSET %d", Integer.parseInt(chatId), off);
	//esql.executeQueryAndPrintResult(query);
	//List<List<String>> result = esql.executeQueryAndReturnResult(query);
	//for(int i = 0; i < result.size(); i++){
	// for(int j = 0; j < result.get(i).size(); j++){
	//   
	// }
	//}
	int num = esql.executeQueryAndPrintResult(query);
	//String output = String.format("num = %d\n", num);
	//System.out.print(output);
	if(num < 1)
	{
	  System.out.print("\tNo messages in this chat\n");
	  return;
	}

	if(num == 10)
	{
	  boolean loop = true;
	  while(loop)
	  {
	    System.out.println("Printed are the first 10 messages would you like to view the next 10?(y/n)");
	    String answer = in.readLine();
	    if(answer.equals("n"))
	    {
	      loop = false;
	      keep = false;
	    }
	    if(answer.equals("y"))
	    {
	      off = off + 10;
	      loop = false;
	    }
	    if(!answer.equals("n") && !answer.equals("y"))
	      System.out.println("\tInput not recognized");
	  }
	}
	if(num < 10)
	  keep = false;
      }
    }
     catch(Exception e){
       System.err.println(e.getMessage());
       return;
     }
  }

  public static void RemoveContact(Messenger esql, String authorisedUser){
    try{
      System.out.println("\tEnter user id to remove(blank to go back): ");
      String targetUser = in.readLine();
      
      //if blank user id, then we return back
      if(targetUser == ""){
	return;
      }
      
      //we need to validate if the user exists, return error if not
      String query = String.format("SELECT * FROM Usr WHERE login = '%s'", targetUser);
      int userNum = esql.executeQuery(query);
      if(userNum != 1){
	System.out.print("\tError, can not find user!\n");
	return;
      }
      
      //we need to get the contact list of the current user, and add the target to that list
      query = String.format("DELETE FROM user_list_contains WHERE list_id = (SELECT contact_list FROM usr WHERE login = '%s') AND list_member = '%s'", authorisedUser, targetUser);
      esql.executeUpdate(query);
      String output = String.format("\t%s succesfully removed user %s!\n", authorisedUser, targetUser);
      System.out.print(output);
      return;
    }
    catch(Exception e){
      System.err.println(e.getMessage());
      return;
    }
  }

  public static void RemoveBlock(Messenger esql, String authorisedUser){
    try{
      System.out.println("\tEnter user id to remove(blank to go back): ");
      String targetUser = in.readLine();
      
      //if blank user id, then we return back
      if(targetUser == ""){
	return;
      }
      
      //we need to validate if the user exists, return error if not
      String query = String.format("SELECT * FROM Usr WHERE login = '%s'", targetUser);
      int userNum = esql.executeQuery(query);
      if(userNum != 1){
	System.out.print("\tError, can not find user!\n");
	return;
      }
      
      //we need to get the contact list of the current user, and add the target to that list
      query = String.format("DELETE FROM user_list_contains WHERE list_id = (SELECT block_list FROM usr WHERE login = '%s') AND list_member = '%s'", authorisedUser, targetUser);
      esql.executeUpdate(query);
      String output = String.format("\t%s succesfully removed user %s!\n", authorisedUser, targetUser);
      System.out.print(output);
      return;
    }
    catch(Exception e){
      System.err.println(e.getMessage());
      return;
    }
  }
  
  public static void NewMessage(Messenger esql, String authorisedUser, String chatId){
    try{
      //ShowChat(esql, authorisedUser);
      //System.out.print("Which chat do you want to write a message in: ");
      //String chat_id = in.readLine();

      //check if the chat exists
      //String query  = String.format("SELECT * FROM chat WHERE chat_id = %d", Integer.parseInt(chat_id) );
      //int result = esql.executeQuery(query);

      //if(result != 1){
      //return;
      //}

      System.out.println("\tEnter message(blank to go back): ");
      String msg = in.readLine();
      
      //if blank msg, then we return back
      if(msg.equals("")){
	return;
      }
      
      String query = String.format("INSERT INTO message (msg_text, sender_login, chat_id) VALUES('%s', '%s', %d)", msg, authorisedUser, Integer.parseInt(chatId));
      esql.executeUpdate(query);
      String output = String.format("\tThe message '%s' was written successfully\n", msg);
      System.out.print(output);
      return;
    }
    catch(Exception e){
      System.err.println(e.getMessage());
      return;
    }
  }//end 
  
   public static void StartOrLeaveChat(Messenger esql, String authorisedUser){
       //this function will handle user starting or leaving a chat
       clearTerminal();
       
       //first we ask if the user wants to leave or start a chat
       System.out.println("Start, Leave, or Modify a Chat");
       System.out.println("---------------------");
       System.out.println("1. Start a chat");
       System.out.println("2. Leave a chat");
       System.out.println("3. Modify a chat");
       System.out.println("---------------------");
       System.out.println("9. Back to Main Menu");
       
       //switch logic read user input
       switch (readChoice()){
           case 1: StartChat(esql, authorisedUser); break;
           case 2: LeaveChat(esql, authorisedUser); break;
           case 3: ModifyChat(esql, authorisedUser); break;
           case 9: return;
           default : System.out.println("Unrecognized choice!"); break;
       }
       
   }
   
   //helper function to create chat for user
   public static void StartChat(Messenger esql, String authorisedUser){
       clearTerminal();
       
       //first we want to ask  user for chat type
       System.out.println("Start a Chat");
       System.out.println("----------------");
       System.out.println("1. Group Chat");
       System.out.println("2. Private Chat");
       System.out.println(".................");
       System.out.println("9. Go back");
       System.out.println("----------------");
       
       //switch logic read user input
       switch (readChoice()){
           case 1: 
                //start a group chat
                System.out.println("Making Group chat...");
                try{
                  String query = String.format("INSERT INTO chat (chat_type, init_sender) VALUES('group', '%s')", authorisedUser);
                  esql.executeUpdate(query);
                  int chat_id = esql.getCurrSeqVal("chat_chat_id_seq");
                  
                  //add auth user to private chat
                  query = String.format("INSERT INTO chat_list (chat_id, member) VALUES(%d, '%s')",chat_id , authorisedUser);
                  esql.executeUpdate(query);
                  
                  System.out.println("Group chat " + chat_id + " created!");
                  //since the chat is group, we will not ask the user to populate the chat right now. 
                }
                catch(Exception e){
                  System.err.println(e.getMessage());
                  return;
                }
                break;
           case 2: 
                //start a private chat
                System.out.println("Making Private chat...");
                try{
                  //now we must ask the user who to private chat with
                  System.out.print("Who would you like to chat with privately?: ");
                  String targetUser = in.readLine();
                  
                  //we need to validate if the user exists, return error if not
                  if(!UserExists(esql, targetUser)){
                      return;
                  }
                  
                  //create private chat
                  String query = String.format("INSERT INTO chat (chat_type, init_sender) VALUES('private', '%s')", authorisedUser);
                  esql.executeUpdate(query);
                  int chat_id = esql.getCurrSeqVal("chat_chat_id_seq");
                  
                  //add auth user to private chat
                  query = String.format("INSERT INTO chat_list (chat_id, member) VALUES(%d, '%s')",chat_id , authorisedUser);
                  esql.executeUpdate(query);
                  
                  //add target user to private chat
                  query = String.format("INSERT INTO chat_list (chat_id, member) VALUES(%d, '%s')",chat_id , targetUser);
                  esql.executeUpdate(query);
                  
                  System.out.println("Private chat " + chat_id + " with " + targetUser+ " is created!");
                   
                }
                catch(Exception e){
                  System.err.println(e.getMessage());
                  return;
                }
                break;
           case 9: return;
           default : System.out.println("Unrecognized choice!"); break;
       }
   }

   //helper function to leave chat for user
   public static void LeaveChat(Messenger esql, String authorisedUser){
       try{
           clearTerminal();
           boolean inLeaveChat = true;

           //call function to show chats
           ShowChat(esql, authorisedUser);

           //event loop for leave chat
           while (inLeaveChat){

               System.out.print("\nEnter chat room id to leave(blank to go back): ");
               String targetChat = in.readLine();

               if (targetChat == "") {
                   inLeaveChat = false;
                   return;
               }

               //check if the chat room exists
               String query = String.format("SELECT * FROM chat WHERE chat_id = %s", targetChat);
               List<List< String >> result = esql.executeQueryAndReturnResult(query);
               if(result.size() == 1){
                   query = String.format("SELECT init_sender FROM chat WHERE chat_id = %s", targetChat);
                   result = esql.executeQueryAndReturnResult(query);

                   System.out.print(result.get(0).get(0).replace(" ", ""));

                   //check if chat was started by current user
                   if(result.get(0).get(0).replace(" ", "") == authorisedUser){
                       //chat is started by current user, delete chat and remove all users
                       query = String.format("DELETE FROM chat WHERE chat_id = %s", targetChat);
                       esql.executeUpdate(query);
                       //cascade handles members in the chat list
                   }
                   else {
                       //chat is not started by current user, simply remove him from chat
                       query = String.format("DELETE FROM chat_list WHERE chat_id = %s AND memeber = '%s'", targetChat, authorisedUser);
                       esql.executeUpdate(query);
                   }
               }
               else {
		 String output = String.format("\nError! No chat with id %s.", targetChat);
		 System.out.print(output);
               }
           }
       }
       catch(Exception e) {
           System.err.println(e.getMessage());
           return;
       }
   }
   
   //helper function to modify chat for user
   public static void ModifyChat(Messenger esql, String authorisedUser){
       try{
           clearTerminal();

           //call function to show chats
           ShowChat(esql, authorisedUser);

           //start loop to ask what chat to modify
           boolean inModifyChat = true;
           while (inModifyChat){
               System.out.print("\nEnter chat room id to modify(blank to go back): ");
               String targetChat = in.readLine();

               if (targetChat == "") {
                   inModifyChat = false;
                   return;
               }

               //check if the chat room exists
               String query = String.format("SELECT * FROM chat WHERE chat_id = %s AND init_sender = '%s'", targetChat, authorisedUser);
	       List<List< String >> result = esql.executeQueryAndReturnResult(query);
               if(result.size() == 1){
                   ModifyChatOptions(esql, authorisedUser, targetChat);
		   return;
               }
               else{
                   System.out.print("Error, you do not own chat.");
               }
           }
       }
       catch(Exception e) {
           System.err.println(e.getMessage());
           return;
       }

   }

    public static void ModifyChatOptions(Messenger esql, String authorisedUser, String chatId){
        //print new menu to handle modification options
        clearTerminal();
	String output = String.format("What action would you like to perform on chat %s", chatId);
	System.out.println(output);
        System.out.println("----------------------------------------");
        System.out.println("(1) Add a member to group chat");
        System.out.println("(2) Remove a member from group chat");
        System.out.println("----------------------------------------");
        System.out.println("(9) Go back");

        //switch logic read user input
        switch (readChoice()){
            case 1: ModifyAddMember(esql, authorisedUser, chatId); break;
            case 2: ModifyRemoveMember(esql, authorisedUser, chatId); break;
            case 9: return;
            default : System.out.println("Unrecognized choice!"); break;
        }

    }

    public static void ModifyAddMember(Messenger esql, String authorisedUser, String chatId){
        try{
	  clearTerminal();
	  String output = String.format("Who to add to chat %s:", chatId);
	  System.out.print(output);
	  String targetUser = in.readLine();
	  
	  
	  if (targetUser == "") {
            return;
	  }
	  
	  //check if the user exists
	  if(!UserExists(esql, targetUser)){
            return;
	  }
	  
	  String query = String.format("INSERT INTO chat_list (chat_id, member) VALUES (%d, '%s')", Integer.parseInt( chatId ), targetUser);
            esql.executeUpdate(query);
            System.out.println("Added " + targetUser + " to chat " + chatId);
        }
        catch(Exception e) {
            System.err.println(e.getMessage());
            return;
        }
    }

    public static void ModifyRemoveMember(Messenger esql, String authorisedUser, String chatId){
        try{
	  clearTerminal();
	  String output = String.format("Who to remove from chat %s:", chatId);
	  System.out.print(output);
	  String targetUser = in.readLine();
	  
	  if (targetUser == "") {
            return;
	  }
	  
	  //check if the user exists
	  if(!UserExists(esql, targetUser)){
            return;
	  }
	  String query = String.format("DELETE FROM chat_list WHERE chat_id = %s AND member = '%s'", chatId, targetUser);
            esql.executeUpdate(query);
            System.out.println("Removed " + targetUser + " from chat " + chatId);
        }
        catch(Exception e) {
            System.err.println(e.getMessage());
            return;
        }
    }

    public static void ShowChat(Messenger esql, String authorisedUser){
      try{
	//first we output all the chats with the chat memebrs so user can see
	String query = String.format("SELECT chat_id FROM chat_list WHERE member = '%s'", authorisedUser);
	List<List<String>> result = esql.executeQueryAndReturnResult(query);
	
	//for loop to iterate through results; results contains the id of every chat the user initiated
	for(int i = 0; i < result.size(); i++){
	  
	  //query to get all the members of a chat
	  query = String.format("SELECT member FROM chat_list WHERE chat_id = %s", result.get(i).get(0));
	  List<List<String>> chatMembers = esql.executeQueryAndReturnResult(query);
	  
	  for (int j = 0; j < result.get(i).size(); j++){
	    
	    //print out room number
	    System.out.print(result.get(i).get(j) + "\t");
	    
	    //iterate through members
	    for (int l = 0; l < chatMembers.size(); l++){
	      
	      for(int m = 0; m < chatMembers.get(l).size(); m++){
		System.out.print(chatMembers.get(l).get(m).replace(" ", "") + " ");
	      }
	    }
	    System.out.print("\n");
	  }
	}
      }
      catch(Exception e) {
	System.err.println(e.getMessage());
	return;
      }
    }
   
   public static void Query6(Messenger esql){
      // Your code goes here.
      clearTerminal();
      // ...
      // ...
   }//end Query6


    //subroutines (helper functions)
    public static boolean UserExists(Messenger esql, String userId){
        try{
            String query = String.format("SELECT * FROM Usr WHERE login = '%s'", userId);
            int userNum = esql.executeQuery(query);
            if(userNum != 1){
                System.out.print("\tError, can not find user!\n");
                return false;
            }
            return true;
        }
        catch(Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

}//end Messenger
