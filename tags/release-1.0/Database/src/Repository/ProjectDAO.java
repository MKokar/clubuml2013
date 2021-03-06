package Repository;
/**
 * @author
 * Joanne Zhuo
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException; 

public class ProjectDAO {
	public final static String PROJECT_NAME = "ClubUML"; 
	public final static String PROJECT_Desc = "ClubUML First Project"; 
	public final static byte PROJECT_Achived = 0;   
	
	/** Get project from DB */
	public static int getProjectId()
	{
		int projectId = -1;
		try {
			Connection conn = DbManager.getConnection();
			projectId = retrieveProject(conn);  
			if (projectId == 0) {
				// If we don't have the project existed in DB, then create one first 
				addProject(conn);
				// retrieve the newly added project
				projectId = retrieveProject(conn);
			} 
		    conn.close();  
		} catch (SQLException e) {
			System.out.println("Using default model.");
		}
		return projectId; 
	}
	
	/** Get project from DB */
	public static int retrieveProject(Connection conn)
	{
		int  projectId = 0;
		try {
			PreparedStatement pstmt = conn.prepareStatement(
			"SELECT * FROM project where projectName = ? ;");
			pstmt.setString(1, PROJECT_NAME); 

			// Execute the SQL statement and store result into the ResultSet
			ResultSet rs = pstmt.executeQuery();  
			if (rs.next()) {
				projectId = rs.getInt("project_Id");
			}  
			rs.close();
			pstmt.close();
			
		} catch (SQLException e) {
			System.out.println("Using default model.");
		}
		return projectId; 
	}
	
	/** Add our default project into DB */
	public static boolean addProject (Connection conn) { 
		try { 
			PreparedStatement pstmt = conn.prepareStatement(
			"INSERT into project(projectName, description, achived) VALUES(?,?,?);");
			pstmt.setString(1, PROJECT_NAME);
			pstmt.setString(2, PROJECT_Desc);
			pstmt.setByte(3, PROJECT_Achived); 

			// Execute the SQL statement and update database accordingly.
			pstmt.executeUpdate();
			pstmt.close(); 
		} 
		catch (SQLException e) {
			System.out.println("Creating project error!");
			return false;
		} 
		return true;
	}
	
	/** Remove a project from DB */
	public static boolean removeProject() 
	{
		try {
			Connection conn = DbManager.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(
			"DELETE FROM project where projectName = ?;");
			pstmt.setString(1, PROJECT_NAME);

			// Execute the SQL statement and update database accordingly.
			pstmt.executeUpdate();
		 
			pstmt.close();
		    conn.close(); 
			return true;
		} catch (SQLException e) {
			throw new IllegalArgumentException (e.getMessage(), e);
		}
	}
}
