package beans.city;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.NoSuchEntityException;

import util.DBTool;

public class CityBean implements EntityBean {
	private static final long serialVersionUID = 1L;
	private Integer parentID;
	private Integer ID;
	private String name;
	private int population;
	private int square;
	private EntityContext context;
	
	private static final String TABLE_NAME = "CITY";
	
	public void setParentID(int parentId) {
		this.parentID = parentId;
	}
	
	public int getParentID() {
		return parentID;
	}
	
	public int getID() {
		return ID;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setPopulation(int population) {
		this.population = population;
	}
	
	public int getPopulation() {
		return population;
	}
	
	public void setSquare(int square) {
		this.square = square;
	}
	
	public int getSquare() {
		return square;
	}

	@Override
	public void ejbActivate() {
		ID = (Integer) context.getPrimaryKey();
	}

	@Override
	public void ejbLoad() {
		try {
			loadInstance();
		} catch (SQLException e) {
			throw new EJBException("Loading failed: " + e.getMessage(), e);
		}
	}

	@Override
	public void ejbPassivate() {
		ID = null;
	}

	@Override
	public void ejbRemove() {
		try {
			removeInstance();
		} catch (SQLException e) {
			throw new EJBException("Removing failed: " + e.getMessage(), e);
		}
	}

	@Override
	public void ejbStore() {
		try {
			storeInstance();
		} catch (SQLException e) {
			throw new EJBException("Updating failed: " + e.getMessage(), e);
		}
	}

	public void ejbPostCreate(Integer pid, String name, int population, int square) {}
	
	public Integer ejbCreate(Integer pid, String name, int population, int square) {
		try {
			this.ID = createInstance(pid, name, population, square);
		} catch (Exception e) {
			throw new EJBException("Creation failed: " + e.getMessage(), e);
		}
		
		this.parentID = pid;
		this.name = name;
		this.square = square;
		this.population = population;
		return ID;
	}

	public Integer ejbFindByPrimaryKey(Integer key) {
		try {
			if(selectByPK(key)) {
				return key;
			} else {
				return -1;
			}
		} catch (SQLException e) {
			throw new EJBException("Find by PK failed: " + e.getMessage(), e);
		}
	}

	public Collection<Integer> ejbFindAll() {
		try {
			return selectAll();
		} catch (SQLException e) {
			throw new EJBException("Find all failed: " + e.getMessage(), e);
		}
	}

	@Override
	public void setEntityContext(EntityContext context) {
		this.context = context;
	}

	@Override
	public void unsetEntityContext() {
		context = null;
	}
	
	private Integer createInstance(Integer pid, String name, int population, int square) throws SQLException {

		Connection con = null;
		PreparedStatement stm = null;
		
		this.ID = Integer.valueOf(DBTool.getTool().getNextIdFor(TABLE_NAME));
		
		if(ID == -1) {
			DBTool.getTool().releaseConnection();
			throw new SQLException("Invalid table name or DB access problem - " + TABLE_NAME);
		}
		
		con = DBTool.getTool().getConnection();
		stm = con.prepareStatement("insert into " + TABLE_NAME + " values (?, ?, ?, ?, ?)");
		stm.setInt(1, pid);
		stm.setInt(2, ID);
		stm.setString(3, name);
		stm.setInt(4, population);
		stm.setInt(5, square);
		
		stm.executeUpdate();
		
		stm.close();
		DBTool.getTool().releaseConnection();
		return ID;
	}
	
	private void loadInstance() throws SQLException {
		Connection con = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		
		this.ID = (Integer) context.getPrimaryKey();
		
		con = DBTool.getTool().getConnection();
		stm = con.prepareStatement("select PARENT_ID, NAME, POPULATION, SQUARE from " + TABLE_NAME + " where ID = ?");
		stm.setInt(1, ID);
		rs = stm.executeQuery();
		if(rs.next()) {
			this.parentID = rs.getInt(1);
			this.name = rs.getString(2);
			this.population = rs.getInt(3);
			this.square = rs.getInt(4);
		} else {
			throw new NoSuchEntityException("City with ID = " + ID + " is not found in the database");
		}
		
		rs.close();
		stm.close();
		DBTool.getTool().releaseConnection();
		
	}

	private void removeInstance() throws SQLException {
		
		Connection con = null;
		PreparedStatement stm = null;
		
		con = DBTool.getTool().getConnection();
		stm = con.prepareStatement("delete from " + TABLE_NAME + " where ID = ?");
		stm.setInt(1, ID);
		stm.executeUpdate();
		
		stm.close();
		
		DBTool.getTool().releaseConnection();
	}
	
	private void storeInstance() throws SQLException {
		Connection con = null;
		PreparedStatement stm = null;
		
		con = DBTool.getTool().getConnection();
		stm = con.prepareStatement("update " + TABLE_NAME + " set PARENT_ID = ?, "
																+ "NAME = ?, "
																+ "POPULATION = ?, "
																+ "SQUARE = ? "
															+ "where ID = ?");
		
		stm.setInt(1, parentID);
		stm.setString(2, name);
		stm.setInt(3, population);
		stm.setInt(4, square);
		stm.setInt(5, ID);
		
		int rowsUpd = stm.executeUpdate();
		
		if(rowsUpd == 0) {
			throw new EJBException("Updating city with ID = " + ID + " failed.");
		}
		
		stm.close();
		DBTool.getTool().releaseConnection();
		
	}
	
	private boolean selectByPK(Integer key) throws SQLException {
		Connection con = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		boolean exists = false;
		
		con = DBTool.getTool().getConnection();
		stm = con.prepareStatement("select ID from " + TABLE_NAME + " where ID = ?");
		stm.setInt(1, key);
		rs = stm.executeQuery();
		exists = rs.next();
		
		rs.close();
		stm.close();
		DBTool.getTool().releaseConnection();
		
		return exists;
	}
	
	private Collection<Integer> selectAll() throws SQLException {
		Connection con = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		Collection<Integer> list = new ArrayList<Integer>();
		
		con = DBTool.getTool().getConnection();
		stm = con.prepareStatement("select ID from " + TABLE_NAME);
		rs = stm.executeQuery();
		
		while(rs.next()) {
			list.add(rs.getInt(1));
		}
		
		rs.close();
		stm.close();
		DBTool.getTool().releaseConnection();
		
		return list;
	}
}