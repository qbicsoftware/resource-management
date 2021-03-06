/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like
 * booking devices or planning resources for services and integration of relevant data into the
 * common portal infrastructure. Copyright (C) 2017 Aydın Can Polatkan & David Wojnar
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package facs.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.ui.components.calendar.event.BasicEvent;
import com.vaadin.ui.components.calendar.event.CalendarEvent;

import facs.model.BookingBean;
import facs.model.DeviceBean;
import facs.model.MachineOccupationBean;
import facs.model.UserBean;

public enum Database {

  Instance;
  private String password;
  private String user;
  private String host;
  Connection conn = null;

  public void init(String user, String password, String host) {
    // check if com.mysql.jdbc.Driver exists. If not try to add it
    String mysqlDriverName = "com.mysql.jdbc.Driver";
    Enumeration<Driver> tmp = DriverManager.getDrivers();
    boolean existsDriver = false;
    while (tmp.hasMoreElements()) {
      Driver d = tmp.nextElement();
      if (d.toString().equals(mysqlDriverName)) {
        existsDriver = true;
        break;
      }
      // System.out.println("Database: " + d.toString());
    }
    if (!existsDriver) {
      // Register JDBC driver
      // According http://docs.oracle.com/javase/6/docs/api/java/sql/DriverManager.html
      // this should not be needed anymore. But without it I get the following error:
      // java.sql.SQLException: No suitable driver found for
      // jdbc:mysql://localhost:3306/facs_facility
      // Does not work for serlvets, just for portlets :(
      try {
        Class.forName(mysqlDriverName);
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    this.password = password;
    this.user = user;
    this.host = host;
  }


  public void changeTimeBlock() {

  }

  public void removeTimeBlock() {

  }

  public void addTimeBlock() {

  }

  public int addUsedTimeBlock(String userName, String fullName, String application, String role,
      String departement, String institution, Date login, Date logout, String buildVersion,
      String cytometer, String serialNo, String custom) {
    int usedTimeBlockId = -1;
    if (userName == null || userName.isEmpty() || institution == null || institution.isEmpty()) {
      return usedTimeBlockId;
    }
    String sql =
        "INSERT INTO resource_occupation (deviceId, userId, device_user_name, full_name, application, role, departement, institution, login, logout, buildversion, cytometer, serialno, custom) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, userName);
      // TODO
      // !!!
      statement.setString(2, stringOrEmpty(fullName));
      statement.setString(3, stringOrEmpty(fullName));
      statement.setString(4, stringOrEmpty(fullName));
      statement.setString(5, stringOrEmpty(fullName));
      statement.setString(6, stringOrEmpty(fullName));
      statement.setString(7, stringOrEmpty(fullName));
      statement.setString(8, stringOrEmpty(fullName));
      statement.setString(9, stringOrEmpty(fullName));
      statement.setString(10, stringOrEmpty(fullName));
      statement.setString(11, stringOrEmpty(fullName));
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        usedTimeBlockId = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return usedTimeBlockId;
  }

  /**
   * returns either str or an empty string if str is null
   * 
   * @param str
   * @return
   */
  private String stringOrEmpty(String str) {
    // TODO Auto-generated method stub
    return str == null ? new String() : str;
  }

  public void userLogin(String user_ldap, String webbrowser, String ip) {
    String sql = "INSERT INTO user_login (user_ldap, webbrowser, ip) VALUES(?,?,?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, user_ldap);
      statement.setString(2, webbrowser);
      statement.setString(3, ip);
      statement.execute();
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  public void logEverything(String user_ldap, String comment) {
    String sql = "INSERT INTO user_log (user_ldap, comment) VALUES(?,?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, user_ldap);
      statement.setString(2, comment);
      statement.execute();
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  public String getUserAdminPanelAccessByLDAPId(String uuid) {
    String userrole = "V";

    String sql = "SELECT admin_panel FROM user WHERE user_ldap=?";

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, uuid);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userrole = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userrole;
  }

  public String getUserRoleByUserId(String uuid) {
    String userrole = "N/A";

    String sql =
        "SELECT group_name FROM groups INNER JOIN user ON user.group_id = groups.group_id WHERE user_id=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, uuid);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userrole = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userrole;
  }

  public String getUserRoleByLDAPId(String uuid) {
    String userrole = "V";

    String sql = "SELECT group_id FROM user WHERE user_ldap=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, uuid);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userrole = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userrole;
  }

  public String getUserRoleNameByLDAPId(String uuid) {
    String userrole = "V";

    String sql =
        "SELECT group_name FROM user INNER JOIN groups ON user.group_id = groups.group_id WHERE user_ldap=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, uuid);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userrole = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userrole;
  }

  /**
   * Returns the cost of the selected device according to the user's group
   * 
   * @param calendar_name
   * @param service_name
   * @param group_id
   * @return
   */
  public int getDeviceCostPerGroup(String calendar_name, String service_name, String group_id) {
    int cost = 0;
    String sql;

    if (service_name != null) {
      sql =
          "SELECT cost FROM costs INNER JOIN calendars ON costs.calendar_id = calendars.calendar_id WHERE calendars.calendar_name=? AND calendars.description=? AND costs.group_id=?";
    } else {
      sql =
          "SELECT cost FROM costs INNER JOIN calendars ON costs.calendar_id = calendars.calendar_id WHERE calendars.calendar_name=? AND calendars.description IS ? AND costs.group_id=?";
    }
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, calendar_name);
      statement.setString(2, service_name);
      statement.setString(3, group_id);
      ResultSet rs = statement.executeQuery();
      // System.out.println("getDeviceCostPerGroup: "+statement);
      while (rs.next()) {
        cost = rs.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return cost;
  }

  /**
   * Returns the device ID by querying the device name
   * 
   * @param device_name
   * @return
   */
  public String getDeviceIDByName(String device_name) {
    String userrole = "V";

    String sql = "SELECT device_id FROM devices WHERE device_name=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, device_name);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userrole = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userrole;
  }


  /**
   * Returns the user name by querying the user ID
   * 
   * @param uuid
   * @return
   */
  public boolean hasAdminPanelAccess(String user_id) {
    boolean user_access = false;

    String sql = "SELECT admin_panel FROM user WHERE user_id = ?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, user_id);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        user_access = rs.getBoolean(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return user_access;
  }

  /**
   * Returns the user name by querying the user ID
   * 
   * @param uuid
   * @return
   */
  public String getUserNameByUserID(String uuid) {
    String userrole = "V";

    String sql = "SELECT user_name FROM user WHERE user_ldap = ?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, uuid);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userrole = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userrole;
  }

  /**
   * Returns the user name by querying the user ID
   * 
   * @param uuid
   * @return
   */
  public String getEmailbyUserName(String username) {

    String sql = "SELECT email FROM user WHERE user_name = ?";

    String email = "'helpdesk@qbic.uni-tuebingen.de'";

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, username);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        email = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return email;
  }

  /**
   * Returns the user ID from the user table by using users LDAP ID
   * 
   * @param uuid
   * @return
   */
  public String getUserLDAPIDbyID(String ID) {
    String userLDAP = "";

    String sql = "SELECT user_ldap FROM user WHERE user_id=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, ID);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userLDAP = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userLDAP;
  }

  /**
   * Returns the user LDAP ID from the user table by using users UserName
   * 
   * @param uuid
   * @return
   */
  public String getUserLDAPIDbyUserName(String user_name) {
    String userLDAP = "";

    String sql = "SELECT user_ldap FROM user WHERE user_name=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, user_name);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userLDAP = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userLDAP;
  }

  /**
   * Returns the user ID from the user table by using users LDAP ID
   * 
   * @param uuid
   * @return
   */
  public String getUserIDbyLDAPID(String uuid) {
    String userrole = "Basic · only view the calendar and/or request.";

    String sql = "SELECT user_id FROM user WHERE user_ldap=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, uuid);
      ResultSet rs = statement.executeQuery();
      // System.out.println("getUserIDbyLDAPID: " + statement);
      while (rs.next()) {
        userrole = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userrole;
  }

  /**
   * Returns the user group ID
   * 
   * @param userWorkgroupName
   * @return
   */
  public String getUserWorkgroupIDByName(String userWorkgroupName) {
    String userrole = "Basic · only view the calendar and/or request.";

    String sql = "SELECT workgroup_id FROM workgroups WHERE workgroup_name=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, userWorkgroupName);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userrole = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userrole;
  }


  /**
   * Returns the user group ID
   * 
   * @param userGroupName
   * @return
   */
  public String getUserGroupIDByName(String userGroupName) {
    String userrole = "Basic · only view the calendar and/or request.";

    String sql = "SELECT group_id FROM groups WHERE group_name=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, userGroupName);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userrole = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userrole;
  }

  /**
   * Returns the specified user's Group Description by using user's LDAP ID
   * 
   * @param uuid
   * @param device_id
   * @return
   */
  public String getUserWorkgroupByUserId(String user_id) {
    String workgroup_name = "";

    String sql =
        "SELECT workgroup_name FROM workgroups INNER JOIN user ON workgroups.workgroup_id = user.workgroup_id WHERE user_id=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, user_id);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        workgroup_name = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return workgroup_name;
  }

  /**
   * Returns the specified user's Group Description by using user's LDAP ID
   * 
   * @param uuid
   * @param device_id
   * @return
   */
  public String getUserGroupDescriptionByLDAPId(String uuid, String device_id) {
    String userrole = "V";

    String sql =
        "SELECT role_description FROM roles INNER JOIN user_roles ON roles.role_id = user_roles.role_id INNER JOIN user ON user_roles.user_id = user.user_id WHERE user_ldap=? AND device_id=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, uuid);
      statement.setString(2, device_id);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userrole = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userrole;
  }

  /**
   * Returns the specified user's Group Description by using user's LDAP ID
   * 
   * @param uuid
   * @param device_id
   * @return
   */
  public String getUserGroupDescriptionByUserID(String uuid, String device_id) {
    String userrole = "N/A";

    String sql =
        "SELECT role_description FROM roles INNER JOIN user_roles ON roles.role_id = user_roles.role_id INNER JOIN user ON user_roles.user_id = user.user_id INNER JOIN devices ON user_roles.device_id = devices.device_id WHERE user.user_id=? AND device_name=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, uuid);
      statement.setString(2, device_id);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userrole = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userrole;
  }

  /**
   * Returns the user role ID of the user for the specified device
   * 
   * @param userRoleDesc
   * @return
   */
  public String getUserRoleIDbyDesc(String userRoleDesc) {
    String userrole = "Basic · only view the calendar and/or request.";

    String sql = "SELECT role_id FROM roles WHERE role_description=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, userRoleDesc);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userrole = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userrole;
  }

  /**
   * Returns the user role description of the user for the specified device the user role
   * description defines the user role in detail
   * 
   * @param uuid
   * @param device_name
   * @return
   */
  public String getUserRoleDescByLDAPId(String uuid, String device_name) {
    String userrole = "Basic · only view the calendar and/or request.";

    String sql =
        "SELECT role_description FROM user_roles INNER JOIN user ON user_roles.user_id = user.user_id INNER JOIN devices ON user_roles.device_id = devices.device_id INNER JOIN roles on user_roles.role_id = roles.role_id WHERE user_ldap=? AND device_name=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, uuid);
      statement.setString(2, device_name);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userrole = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userrole;
  }

  /**
   * Returns the user's role name for the specified device like 'Admin', 'A', 'B', 'C' or 'V'
   * 
   * @param uuid
   * @param device_name
   * @return
   */
  public String getUserRoleByLDAPId(String uuid, String device_name) {
    String userrole = "V";

    String sql =
        "SELECT role_name FROM user_roles INNER JOIN user ON user_roles.user_id = user.user_id INNER JOIN devices ON user_roles.device_id = devices.device_id INNER JOIN roles on user_roles.role_id = roles.role_id WHERE user_ldap=? AND device_name=?";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, uuid);
      statement.setString(2, device_name);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userrole = rs.getString(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();

      if (userrole.isEmpty())
        userrole = "V";

    }

    catch (SQLException e) {
      e.printStackTrace();
    }

    return userrole;
  }

  public boolean adminUpdatesUserGroups(String uuid, String group_id) {
    int count = 0;
    boolean success = false;

    String sqlCheck = "SELECT COUNT(*) FROM user_groups WHERE user_id=?";

    try (Connection connCheck = login();
        PreparedStatement statementCheck =
            connCheck.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setString(1, uuid);
      ResultSet resultCheck = statementCheck.executeQuery();
      // System.out.println("Exists: " + statementCheck);
      while (resultCheck.next()) {
        count = resultCheck.getInt(1);
      }
      // System.out.println("resultCheck: " + count);
    } catch (SQLException e) {
      e.printStackTrace();
    }


    if (count > 0) {
      String sqlUpdate = "UPDATE user_groups SET group_id=? WHERE user_id=?";
      try (Connection connUpdate = login();
          PreparedStatement statementUpdate = connUpdate.prepareStatement(sqlUpdate)) {
        statementUpdate.setString(1, group_id);
        statementUpdate.setString(2, uuid);
        int result = statementUpdate.executeUpdate();
        // System.out.println("Update: " + statementUpdate);
        success = (result > 0);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } else {
      String sqlInsert = "INSERT INTO user_groups (user_id, group_id) VALUES (?,?)";
      try (Connection connInsert = login();
          PreparedStatement statementInsert = connInsert.prepareStatement(sqlInsert)) {
        statementInsert.setString(1, uuid);
        statementInsert.setString(2, group_id);
        int result = statementInsert.executeUpdate();
        // System.out.println("Insert: " + statementInsert);
        success = (result > 0);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    return success;
  }

  /**
   * Sets the user role for the selected device for the specified user. Returns a boolean value in
   * case of success or fail.
   * 
   * @param user_role
   * @param uuid
   * @param device_name
   * @return
   */
  public boolean adminUpdatesUserRoleForDevice(String user_role, String uuid, String device_name) {
    int count = 0;
    boolean success = false;

    String sqlCheck =
        "SELECT COUNT(*) FROM user_roles WHERE role_id=? AND user_id=? AND device_id=?";

    try (Connection connCheck = login();
        PreparedStatement statementCheck =
            connCheck.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setString(1, user_role);
      statementCheck.setString(2, uuid);
      statementCheck.setString(3, device_name);
      ResultSet resultCheck = statementCheck.executeQuery();
      // System.out.println("Exists: " + statementCheck);
      while (resultCheck.next()) {
        count = resultCheck.getInt(1);
      }
      // System.out.println("resultCheck: " + count);
    } catch (SQLException e) {
      e.printStackTrace();
    }


    if (count > 0) {
      String sqlUpdate = "UPDATE user_roles SET role_id=?  WHERE user_id=? AND device_id=?";
      try (Connection connUpdate = login();
          PreparedStatement statementUpdate = connUpdate.prepareStatement(sqlUpdate)) {
        statementUpdate.setString(1, user_role);
        statementUpdate.setString(2, uuid);
        statementUpdate.setString(3, device_name);
        int result = statementUpdate.executeUpdate();
        // System.out.println("Update: " + statementUpdate);
        success = (result > 0);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } else {
      String sqlInsert = "INSERT INTO user_roles (user_id, role_id, device_id) VALUES (?,?,?)";
      try (Connection connInsert = login();
          PreparedStatement statementInsert = connInsert.prepareStatement(sqlInsert)) {
        statementInsert.setString(1, uuid);
        statementInsert.setString(2, user_role);
        statementInsert.setString(3, device_name);
        int result = statementInsert.executeUpdate();
        // System.out.println("Insert: " + statementInsert);
        success = (result > 0);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    return success;
  }

  /**
   * Sets the user role for the selected device for the specified user. Returns a boolean value in
   * case of success or fail.
   * 
   * @param workgroup_id
   * @param uuid
   * @return
   */

  public boolean adminUpdatesUserWorkgroup(String workgroup_id, String uuid) {
    boolean success = false;

    String sqlUpdate = "UPDATE user SET workgroup_id=? WHERE user_id=?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sqlUpdate)) {
      statement.setString(1, workgroup_id);
      statement.setString(2, uuid);
      // System.out.println("adminUpdatesUserWorkgroup: " + statement);
      int result = statement.executeUpdate();
      // System.out.println("getShitDone: " + statement);
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return success;
  }


  /**
   * Sets the user's group for the specified user. Returns a boolean value in case of success or
   * fail.
   * 
   * @param user_group
   * @param uuid
   * @return
   */
  public boolean adminUpdatesUserGroup(String user_group, String uuid) {
    // boolean exists = false;
    boolean success = false;
    /*
     * String sqlCheck = "SELECT user_id FROM user_groups WHERE group_id = ? AND user_id = ?"; try
     * (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sqlCheck)) {
     * statement.setString(1, user_group); statement.setString(2, uuid); int result =
     * statement.executeUpdate(); // System.out.println("getShitDone: " + statement); exists =
     * (result > 0); } catch (SQLException e) { e.printStackTrace(); }
     * 
     * if (exists) { String sqlUpdate = "UPDATE user_groups SET group_id=? WHERE user_id=?"; try
     * (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sqlUpdate)) {
     * statement.setString(1, user_group); statement.setString(2, uuid); int result =
     * statement.executeUpdate(); System.out.println("getShitDone: " + statement); success = (result
     * > 0); } catch (SQLException e) { e.printStackTrace(); } } else { String sqlInsert =
     * "INSERT INTO user_groups (user_id, group_id) VALUES (?,?)"; try (Connection conn = login();
     * PreparedStatement statement = conn.prepareStatement(sqlInsert)) { statement.setString(1,
     * uuid); statement.setString(2, user_group); int result = statement.executeUpdate(); //
     * System.out.println("getShitDone: " + statement); success = (result > 0); } catch
     * (SQLException e) { e.printStackTrace(); } }
     */
    String sql = "UPDATE user SET group_id=? WHERE user_id=?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, user_group);
      statement.setString(2, uuid);
      // System.out.println("adminUpdatesUserGroup: " + statement);
      int result = statement.executeUpdate();
      // System.out.println("getShitDone: " + statement);
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return success;
  }

  /**
   * Sets the user role for the selected device for the specified user. Returns a boolean value in
   * case of success or fail.
   * 
   * @param user_role
   * @param uuid
   * @param device_name
   * @return
   */
  public boolean getShitDone(String user_role, String uuid, String device_name) {
    boolean success = false;

    String sql = "UPDATE user_roles SET role_id=? WHERE user_id=? AND device_id=?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, user_role);
      statement.setString(2, uuid);
      statement.setString(3, device_name);
      int result = statement.executeUpdate();
      // System.out.println("getShitDone: "+sql);
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return success;
  }

  /**
   * Sets the user's group for the specified user. Returns a boolean value in case of success or
   * fail.
   * 
   * @param user_group
   * @param uuid
   * @return
   */
  public boolean getShitDoneAgain(String user_group, String uuid) {
    boolean success = false;

    String sql = "UPDATE user SET group_id=? WHERE user_ldap=?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, user_group);
      statement.setString(2, uuid);
      int result = statement.executeUpdate();
      // System.out.println("getShitDone: "+sql);
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return success;
  }

  public int addBooking(String uuid, String device_name, Date start, Date end, long duration,
      String service, String kostenstelle, double cost) {
    int booking_id = -1;
    if (uuid == null || uuid.isEmpty() || start == null || end == null) {
      return booking_id;
    }

    // System.out.println("Database.java 131 util start: " + start);
    java.sql.Timestamp sqlStart = new java.sql.Timestamp(start.getTime());
    java.sql.Timestamp sqlEnd = new java.sql.Timestamp(end.getTime());
    // System.out.println("Database.java 131 sql start: " + sqlStart);
    // java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(timestamp.getTime());
    String sql =
        "INSERT INTO booking (user_ldap, device_name, start, end, duration, service, kostenstelle, price) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, uuid);
      statement.setString(2, device_name);
      statement.setTimestamp(3, sqlStart);
      statement.setTimestamp(4, sqlEnd);
      statement.setLong(5, duration);
      statement.setString(6, service);
      statement.setString(7, kostenstelle);
      statement.setDouble(8, cost);
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        booking_id = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return booking_id;
  }

  public int addBooking(String uuid, String device_name, Date start, Date end, long duration,
      String service, String kostenstelle, double cost, boolean confirmation) {
    int booking_id = -1;
    if (uuid == null || uuid.isEmpty() || start == null || end == null) {
      return booking_id;
    }

    // System.out.println("Database.java 131 util start: " + start);
    java.sql.Timestamp sqlStart = new java.sql.Timestamp(start.getTime());
    java.sql.Timestamp sqlEnd = new java.sql.Timestamp(end.getTime());
    // System.out.println("Database.java 131 sql start: " + sqlStart);
    // java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(timestamp.getTime());
    String sql =
        "INSERT INTO booking (user_ldap, device_name, start, end, duration, service, kostenstelle, price, confirmation) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, uuid);
      statement.setString(2, device_name);
      statement.setTimestamp(3, sqlStart);
      statement.setTimestamp(4, sqlEnd);
      statement.setLong(5, duration);
      statement.setString(6, service);
      statement.setString(7, kostenstelle);
      statement.setDouble(8, cost);
      statement.setBoolean(9, confirmation);
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        booking_id = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return booking_id;
  }


  public int addInvoice(String uuid, String device_name, Date start, Date end, long duration,
      String service, String kostenstelle, double cost) {
    int booking_id = -1;
    if (uuid == null || uuid.isEmpty() || start == null || end == null) {
      return booking_id;
    }

    kostenstelle = getKostenstelleByLDAPId(uuid);

    // System.out.println("Database.java 131 util start: " + start);
    java.sql.Timestamp sqlStart = new java.sql.Timestamp(start.getTime());
    java.sql.Timestamp sqlEnd = new java.sql.Timestamp(end.getTime());
    // System.out.println("Database.java 131 sql start: " + sqlStart);
    // java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(timestamp.getTime());
    String sql =
        "INSERT INTO logs (device_id, device_name, user_name, user_full_name, start, end, duration, cost, invoiced) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, getDeviceIDByName(device_name));
      statement.setString(2, device_name);
      statement.setString(3, uuid);
      statement.setString(4, getUserNameByUserID(uuid));
      statement.setTimestamp(5, sqlStart);
      statement.setTimestamp(6, sqlEnd);
      statement.setLong(7, duration);
      statement.setDouble(8, cost);
      statement.setInt(9, 2);
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        booking_id = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return booking_id;
  }

  public int addInvoice(String uuid, String device_name, Date start, Date end, long duration,
      String service, String kostenstelle, double cost, boolean confirmation) {
    int booking_id = -1;
    if (uuid == null || uuid.isEmpty() || start == null || end == null) {
      return booking_id;
    }

    // System.out.println("Database.java 131 util start: " + start);
    java.sql.Timestamp sqlStart = new java.sql.Timestamp(start.getTime());
    java.sql.Timestamp sqlEnd = new java.sql.Timestamp(end.getTime());
    // System.out.println("Database.java 131 sql start: " + sqlStart);
    // java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(timestamp.getTime());
    String sql =
        "INSERT INTO invoicing (user_ldap, device_name, start, end, duration, service, kostenstelle, price, confirmation) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, uuid);
      statement.setString(2, device_name);
      statement.setTimestamp(3, sqlStart);
      statement.setTimestamp(4, sqlEnd);
      statement.setLong(5, duration);
      statement.setString(6, service);
      statement.setString(7, kostenstelle);
      statement.setDouble(8, cost);
      statement.setBoolean(9, confirmation);
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        booking_id = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return booking_id;
  }



  /**
   * return all device names to display inside the drop-down menu
   * 
   * @return
   */
  public ArrayList<String> getAllUserNames() {
    ArrayList<String> list = new ArrayList<String>();
    String sql = "SELECT user_name FROM user";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("user_name"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }


  public ArrayList<CalendarEvent> getAllBookingsPlusMachineOutput(String uuid, String device_name) {
    ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
    String sql =
        "SELECT DISTINCT * FROM logs INNER JOIN user ON logs.user_full_name = user.user_name WHERE user_ldap != 'deleted' AND logs.device_name = ? AND logs.invoiced IS NULL;";

    String sql2 =
        "SELECT DISTINCT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE user.user_ldap != 'deleted' AND booking.`deleted` IS NULL AND booking.device_name = ?;";

    try (Connection conn = login();
        PreparedStatement statement2 = conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS)) {
      statement2.setString(1, device_name);
      ResultSet rs2 = statement2.executeQuery();

      while (rs2.next()) {

        BasicEvent cannotbedeleted2 =
            new BasicEvent(rs2.getString("user.user_name"), "Contact: "
                + rs2.getString("user.email") + " · Start: " + rs2.getTimestamp("booking.start")
                + " · End: " + rs2.getTimestamp("booking.end"), rs2.getTimestamp("booking.start"),
                rs2.getTimestamp("booking.end"));
        cannotbedeleted2.setStyleName("color3");
        events.add(cannotbedeleted2);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, device_name);
      ResultSet rs = statement.executeQuery();

      while (rs.next()) {

        BasicEvent cannotbedeleted =
            new BasicEvent(rs.getString("logs.user_full_name"), "Contact: "
                + rs.getString("user.email") + " · Start: " + rs.getTimestamp("logs.start")
                + " · End: " + rs.getTimestamp("logs.end"), rs.getTimestamp("logs.start"),
                rs.getTimestamp("logs.end"));
        cannotbedeleted.setStyleName("color5");
        events.add(cannotbedeleted);

      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return events;
  }


  public ArrayList<CalendarEvent> getAllInvoicingBookings(String uuid, String device_name) {
    ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
    String sql =
        "SELECT * FROM logs INNER JOIN user ON logs.user_name = user.user_ldap WHERE invoiced = 2 AND device_name = ?";
    // device_id = device_id+1;

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, device_name);
      ResultSet rs = statement.executeQuery();

      while (rs.next()) {

        String service = rs.getString("service") + " · ";
        if (rs.getString("service") == null)
          service = "";

        if (uuid.equals(rs.getString("user_ldap"))) {
          BasicEvent canbedeleted =
              new BasicEvent(rs.getString("user.user_name"), service + "K: "
                  + rs.getString("kostenstelle") + " · " + "Approx. Cost: €" + rs.getString("cost")
                  + "-", rs.getTimestamp("start"), rs.getTimestamp("end"));
          canbedeleted.setStyleName("color5");
          events.add(canbedeleted);
        } else if (uuid.equals(rs.getString("user_ldap"))) {
          BasicEvent cannotbedeleted =
              new BasicEvent(rs.getString("user.user_name"), service + "K: "
                  + rs.getString("kostenstelle") + " · " + "Approx. Cost: €" + rs.getString("cost")
                  + "-", rs.getTimestamp("start"), rs.getTimestamp("end"));
          cannotbedeleted.setStyleName("color1");
          events.add(cannotbedeleted);
        } else {
          BasicEvent cannotbedeleted =
              new BasicEvent(rs.getString("user.user_name"), "Contact: " + rs.getString("email")
                  + " · Tel: " + rs.getString("phone"), rs.getTimestamp("start"),
                  rs.getTimestamp("end"));
          cannotbedeleted.setStyleName("color3");
          events.add(cannotbedeleted);
        }

      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return events;
  }

  public ArrayList<CalendarEvent> getAllBookings(String uuid, String device_name) {
    ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
    String sql =
        "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE deleted IS NULL AND device_name = ?";
    // device_id = device_id+1;

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, device_name);
      ResultSet rs = statement.executeQuery();

      while (rs.next()) {

        String service = rs.getString("service") + " · ";
        if (rs.getString("service") == null)
          service = "";

        if (uuid.equals(rs.getString("user_ldap")) && rs.getString("confirmation") == null) {
          BasicEvent canbedeleted =
              new BasicEvent(rs.getString("user.user_name"), service + "K: "
                  + rs.getString("kostenstelle") + " · " + "Approx. Cost: €"
                  + rs.getString("price") + "-", rs.getTimestamp("start"), rs.getTimestamp("end"));
          canbedeleted.setStyleName("color5");
          events.add(canbedeleted);
        } else if (uuid.equals(rs.getString("user_ldap")) && rs.getString("confirmation") != null) {
          BasicEvent cannotbedeleted =
              new BasicEvent(rs.getString("user.user_name"), service + "K: "
                  + rs.getString("kostenstelle") + " · " + "Approx. Cost: €"
                  + rs.getString("price") + "-", rs.getTimestamp("start"), rs.getTimestamp("end"));
          cannotbedeleted.setStyleName("color1");
          events.add(cannotbedeleted);
        } else {
          BasicEvent cannotbedeleted =
              new BasicEvent(rs.getString("user.user_name"), "Contact: " + rs.getString("email")
                  + " · Tel: " + rs.getString("phone"), rs.getTimestamp("start"),
                  rs.getTimestamp("end"));
          cannotbedeleted.setStyleName("color3");
          events.add(cannotbedeleted);
        }

      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return events;
  }

  public ArrayList<CalendarEvent> getAllBookings(String device_name) {
    ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
    String sql =
        "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE device_name = ?";

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, device_name);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        BasicEvent canbedeleted =
            new BasicEvent(rs.getString("user.user_name"), "Contact: " + rs.getString("email"),
                rs.getTimestamp("start"), rs.getTimestamp("end"));
        canbedeleted.setStyleName("color5");
        events.add(canbedeleted);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return events;
  }

  public int getAllBookingTotalCount() {
    int count = 0;
    String sql = "SELECT COUNT(*) FROM booking WHERE deleted IS NULL";

    try (Connection connCheck = login();
        PreparedStatement statementCheck =
            connCheck.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ResultSet resultCheck = statementCheck.executeQuery();
      // System.out.println("Exists: " + statementCheck);
      while (resultCheck.next()) {
        count = resultCheck.getInt(1);
      }
      // System.out.println("resultCheck: " + count);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return count;
  }

  public int getAllUnconfirmedCount() {
    int count = 0;
    String sql =
        "SELECT COUNT(*) FROM booking WHERE confirmation IS NOT NULL AND deleted IS NULL AND start > DATE(NOW())";
    try (Connection connCheck = login();
        PreparedStatement statementCheck =
            connCheck.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ResultSet resultCheck = statementCheck.executeQuery();
      // System.out.println("Exists: " + statementCheck);
      while (resultCheck.next()) {
        count = resultCheck.getInt(1);
      }
      // System.out.println("resultCheck: " + count);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return count;
  }

  public java.util.List<BookingBean> getAllBookings() {
    ArrayList<BookingBean> bookings = new ArrayList<BookingBean>();
    String sql = "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap";

    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        bookings.add(new BookingBean(rs.getInt("booking_id"), rs.getString("user_name"), rs
            .getString("phone"), rs.getString("device_name"), rs.getTimestamp("start"), rs
            .getTimestamp("end"), rs.getString("service"), rs.getDouble("price"), rs
            .getBoolean("confirmation")));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return bookings;
  }

  public java.util.List<BookingBean> getAllBookingsPerDevice(String device_name) {
    ArrayList<BookingBean> bookings = new ArrayList<BookingBean>();
    String sql =
        "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE deleted IS NULL AND confirmation IS NULL AND device_name = ?";

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, device_name);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        bookings.add(new BookingBean(rs.getInt("booking_id"), rs.getString("user_name"), rs
            .getString("phone"), rs.getString("device_name"), rs.getTimestamp("start"), rs
            .getTimestamp("end"), rs.getString("service"), rs.getDouble("price"), rs
            .getBoolean("confirmation")));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return bookings;
  }

  public java.util.List<UserBean> getUsers() {
    ArrayList<UserBean> users = new ArrayList<UserBean>();
    String sql =
        "SELECT * FROM user INNER JOIN groups ON user.group_id=groups.group_id INNER JOIN workgroups ON user.workgroup_id=workgroups.workgroup_id;";
    // System.out.println("here");
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      ResultSet rs = statement.executeQuery();

      while (rs.next()) {
        users.add(new UserBean(rs.getString("user_ldap"), rs.getInt("user_id"), rs
            .getString("user_name"), rs.getString("group_name"), rs.getString("workgroup_name"), rs
            .getString("street"), rs.getString("postcode"), rs.getString("city"), rs
            .getString("institute_name"), rs.getString("kostenstelle"), rs.getString("project"), rs
            .getString("email"), rs.getString("phone"), rs.getInt("admin_panel")));
      }
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return users;
  }

  public java.util.List<BookingBean> getDeletedBookings() {
    ArrayList<BookingBean> bookings = new ArrayList<BookingBean>();
    String sql =
        "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE deleted IS NOT NULL";

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        bookings.add(new BookingBean(rs.getInt("booking_id"), rs.getString("user_name"), rs
            .getString("phone"), rs.getString("device_name"), rs.getTimestamp("start"), rs
            .getTimestamp("end"), rs.getString("service"), rs.getDouble("price"), rs
            .getBoolean("confirmation")));
      }
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return bookings;
  }

  public java.util.List<BookingBean> getAwaitingRequests() {
    ArrayList<BookingBean> bookings = new ArrayList<BookingBean>();
    String sql =
        "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE confirmation IS NOT NULL AND deleted IS NULL";
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        bookings.add(new BookingBean(rs.getInt("booking_id"), rs.getString("user_name"), rs
            .getString("phone"), rs.getString("device_name"), rs.getTimestamp("start"), rs
            .getTimestamp("end"), rs.getString("service"), rs.getDouble("price"), rs
            .getBoolean("confirmation")));
      }
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return bookings;
  }

  public ArrayList<CalendarEvent> getMyBookings(String uuid) {
    ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
    // System.out.println("Database.java 165 getAllBookings: ");
    String sql = "SELECT * FROM booking WHERE user_ldap = ?";
    String sql2 = "SELECT * FROM booking WHERE user_ldap != ?";

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, uuid);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        BasicEvent canbedeleted =
            new BasicEvent(uuid, "This time frame is already occupied.", rs.getTimestamp("start"),
                rs.getTimestamp("end"));
        canbedeleted.setStyleName("color4");
        events.add(canbedeleted);
        // System.out.println("uuid: " + uuid + " user_ldap: " +rs.getString("user_ldap"));
        // System.out.println("getAllBookings:" + rs.getTime("start") + " " + rs.getTime("end"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, uuid);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        BasicEvent reserved =
            new BasicEvent(uuid, "This time frame is already occupied.", rs.getTimestamp("start"),
                rs.getTimestamp("end"));
        reserved.setStyleName("color5");
        events.add(4, reserved);
      }

      conn.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return events;
  }

  public java.util.List<BookingBean> getMyBookingsGrid(String uuid) {
    ArrayList<BookingBean> bookings = new ArrayList<BookingBean>();
    String sql =
        "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE deleted IS NULL AND booking.user_ldap = ?";

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, uuid);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        bookings.add(new BookingBean(rs.getInt("booking_id"), rs.getString("kostenstelle"), rs
            .getString("phone"), rs.getString("device_name"), rs.getTimestamp("start"), rs
            .getTimestamp("end"), rs.getString("service"), rs.getDouble("price"), rs
            .getBoolean("confirmation")));
      }
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return bookings;
  }

  public java.util.List<BookingBean> getMyNext3HoursBookings(String uuid, Date start, Date end) {
    ArrayList<BookingBean> bookings = new ArrayList<BookingBean>();

    java.sql.Timestamp sqlStart = new java.sql.Timestamp(start.getTime());
    java.sql.Timestamp sqlEnd = new java.sql.Timestamp(end.getTime());

    String sql =
        "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE deleted IS NULL AND booking.user_ldap = ? AND booking.start > ? AND booking.start <= ?";

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, uuid);
      statement.setTimestamp(2, sqlStart);
      statement.setTimestamp(3, sqlEnd);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        bookings.add(new BookingBean(rs.getInt("booking_id"), rs.getString("kostenstelle"), rs
            .getString("phone"), rs.getString("device_name"), rs.getTimestamp("start"), rs
            .getTimestamp("end"), rs.getString("service"), rs.getDouble("price"), rs
            .getBoolean("confirmation")));
      }
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return bookings;
  }

  public java.util.List<BookingBean> getMyUpcomingBookings(String uuid, Date start) {
    ArrayList<BookingBean> bookings = new ArrayList<BookingBean>();

    java.sql.Timestamp sqlStart = new java.sql.Timestamp(start.getTime());

    String sql =
        "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE deleted IS NULL AND booking.user_ldap = ? AND booking.start > ?";

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, uuid);
      statement.setTimestamp(2, sqlStart);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        bookings.add(new BookingBean(rs.getInt("booking_id"), rs.getString("kostenstelle"), rs
            .getString("phone"), rs.getString("device_name"), rs.getTimestamp("start"), rs
            .getTimestamp("end"), rs.getString("service"), rs.getDouble("price"), rs
            .getBoolean("confirmation")));
      }
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return bookings;
  }

  public java.util.List<BookingBean> getMyPastBookings(String uuid, Date start) {
    ArrayList<BookingBean> bookings = new ArrayList<BookingBean>();

    java.sql.Timestamp sqlStart = new java.sql.Timestamp(start.getTime());

    String sql =
        "SELECT * FROM booking INNER JOIN user ON booking.user_ldap = user.user_ldap WHERE deleted IS NULL AND booking.user_ldap = ? AND booking.start <= ?";

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, uuid);
      statement.setTimestamp(2, sqlStart);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        bookings.add(new BookingBean(rs.getInt("booking_id"), rs.getString("kostenstelle"), rs
            .getString("phone"), rs.getString("device_name"), rs.getTimestamp("start"), rs
            .getTimestamp("end"), rs.getString("service"), rs.getDouble("price"), rs
            .getBoolean("confirmation")));
      }
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return bookings;
  }

  public ArrayList<CalendarEvent> getAllMyBookings(String uuid, String device_name) {
    ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
    // System.out.println("Database.java 192 getAllBookings: ");
    String sql = "SELECT * FROM booking WHERE user_ldap = ? AND device_name = ?";
    // device_id = device_id+1;

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, uuid);
      statement.setString(2, device_name);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        BasicEvent canbedeleted =
            new BasicEvent(uuid, "This time frame is already occupied.", rs.getTimestamp("start"),
                rs.getTimestamp("end"));
        canbedeleted.setStyleName("color5");
        events.add(canbedeleted);
        // System.out.println("uuid: " + uuid + " user_ldap: " +rs.getString("user_ldap"));
        // System.out.println("getAllBookings:" + rs.getTime("start") + " " + rs.getTime("end"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return events;
  }

  public ArrayList<CalendarEvent> getOtherBookings(String uuid) {
    ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();

    String sql = "SELECT * FROM booking WHERE user_ldap != ?";

    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, uuid);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        BasicEvent reserved =
            new BasicEvent(uuid, "This time frame is already occupied.", rs.getTimestamp("start"),
                rs.getTimestamp("end"));
        reserved.setStyleName("color5");
        events.add(reserved);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return events;
  }

  public int addInstitute(String name, String institute, String street, String postalCode,
      String city, String country) {
    int instituteId = -1;
    if (name == null || name.isEmpty()) {
      return instituteId;
    }
    String sql =
        "INSERT INTO workgroups (workgroup_name, instite_name, street, postcode, city, country) VALUES(?, ?, ?, ?, ?, ?)";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.setString(2, institute);
      statement.setString(3, street);
      statement.setString(4, postalCode);
      statement.setString(5, city);
      statement.setString(6, country);
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        instituteId = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return instituteId;
  }

  public void addWorkingGroup(String name) {
    String sql = "INSERT INTO workgroups (workgroup_name) VALUES(?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.execute();
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void addNewUser(String name) {
    String sql = "INSERT INTO user (user_name) VALUES(?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.execute();
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public boolean deleteUser(String index) {
    boolean success = false;
    String sql = "INSERT user_deleted SELECT * FROM user WHERE user.user_id = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, index);
      statement.execute();
      // nothing will be in the database, until you commit it!
      // conn.commit();
      success = true;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    if (success) {
      String sql2 = "DELETE FROM user WHERE user.user_id = ?";
      try (Connection conn = login();
          PreparedStatement statement2 =
              conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS)) {

        statement2.setString(1, index);
        statement2.execute();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      success = true;
    }
    return success;
  }

  public int addKostenstelle(String name, String abbreviation) {
    int costLocId = -1;
    if (name == null || name.isEmpty()) {
      return costLocId;
    }
    String sql = "INSERT INTO kostenstelle (kostenstelle_code, kostenstelle_name) VALUES(?, ?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.setString(2, abbreviation);
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        costLocId = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return costLocId;
  }

  public Date getLastDate(Date referenceDate) {
    String sql = "SELECT start FROM logs WHERE invoiced = 2 ORDER BY log_id DESC LIMIT 1;";
    Date date = referenceDate;
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        date = rs.getDate("start");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return date;
  }

  public String getKostenstelleByUserId(int userId) {
    String sql =
        "SELECT kostenstelle.kostenstelle_code FROM kostenstelle INNER JOIN user ON user.kostenstelle=kostenstelle.kostenstelle_code WHERE user.user_id = ?";
    String kostenstelle = "";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, userId);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        kostenstelle = rs.getString("kostenstelle_code");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return kostenstelle;
  }

  public String getKostenstelleByLDAPId(String userLDAP) {
    String sql = "SELECT kostenstelle FROM user WHERE user_ldap = ?";
    String kostenstelle = "";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, userLDAP);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        kostenstelle = rs.getString("kostenstelle");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return kostenstelle;
  }

  public void addCategory(String name) {
    String sql = "INSERT INTO kostenstelle (kostenstelle_code) VALUES(?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      System.out.print("Triggered: " + sql);
      statement.setString(1, name);
      statement.execute();
      // nothing will be in the database, until you commit it!
      conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public int addUser(String name, String workinggroup, String institute, String email, String role,
      String phone) {
    int userId = -1;
    if (name == null || name.isEmpty()) {
      return userId;
    }
    String sql =
        "INSERT INTO users (name, workgroup, institute_id, email, role, phone) VALUES(?, ?, ?, ?, ?, ?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.setString(2, workinggroup);
      statement.setInt(3, getInstituteIdByName(institute));
      statement.setString(4, email);
      statement.setString(5, role);
      statement.setString(6, phone);
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        userId = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return userId;
  }

  public void addKostenStelleToUser(int userId, String kostenstelle) {
    int kostenstelleId = getKostenstelleIdByName(kostenstelle);
    if (kostenstelleId == -1) {
      throw new IllegalArgumentException("Kostenstelle: " + kostenstelle
          + " does not exist in the database (or database is not reachable).");
    }

    addKostenStelleToUser(userId, kostenstelleId);
  }

  public boolean getDeviceRestriction(String device_name, String selected_service) {
    boolean restriction = false;
    String sql = "SELECT restriction FROM calendars WHERE device_name = ? AND description = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, device_name);
      statement.setString(2, selected_service);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        restriction = rs.getBoolean("restriction");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return restriction;
  }

  public boolean getDeviceRestriction(String device_name) {
    boolean restriction = false;
    String sql = "SELECT restriction FROM devices WHERE device_name = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, device_name);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        restriction = rs.getBoolean("restriction");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return restriction;
  }

  public int getInstituteIdByName(String institute) {
    int kostenstelleId = -1;
    String sql = "SELECT institute_id FROM institute WHERE name = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, institute);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        kostenstelleId = rs.getInt("institute_id");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return kostenstelleId;
  }

  public int getKostenstelleIdByName(String kostenstelle_code) {
    int kostenstelleId = -1;
    String sql = "SELECT kostenstelle_id FROM kostenstelle WHERE name = ?";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, kostenstelle_code);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        kostenstelleId = rs.getInt("kostenstelle_id");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return kostenstelleId;
  }

  public boolean addKostenStelleToUser(int userId, int kostenstelleId) {
    boolean added = true;
    String sql =
        "INSERT INTO users_cost_locations_junction (user_id, cost_location_id) VALUES(?, ?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setInt(1, userId);
      statement.setInt(2, kostenstelleId);
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        userId = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      added = false;
    }
    return added;
  }


  /**
   * adds a device/resource to the database and returns its id if operation succeeded. returns -1
   * else. Note that if label or description are empty or null, nothing is written to the database
   * and -1 is returned.
   * 
   * @param label
   * @param description
   * @param restricted
   * @return
   */
  public int addDevice(String label, String description, boolean restricted) {
    int deviceId = -1;
    if (label == null || label.isEmpty() || description == null || description.isEmpty()) {
      return deviceId;
    }
    String sql = "INSERT INTO devices (device_name, description, restriction) VALUES (?, ?, ?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, label);
      statement.setString(2, description);
      statement.setBoolean(3, restricted);
      // execute the statement, data IS NOT commit yet
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        deviceId = rs.getInt(1);
      }
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return deviceId;
  }

  /**
   * removes the given device bean if its id can be found in the database.
   * 
   * @param db
   * @return
   */
  public boolean removeDevice(DeviceBean db) {
    return removeDevice(db.getId());
  }

  public boolean removeBooking(BookingBean db) {
    return removeBooking(db.getID());
  }

  public boolean purgeBooking(BookingBean db) {
    return purgeBooking(db.getID());
  }

  public boolean restoreBooking(BookingBean db) {
    return restoreBooking(db.getID());
  }

  public boolean denyBooking(BookingBean db) {
    return denyBooking(db.getID());
  }

  public boolean confirmBooking(BookingBean db) {
    return confirmBooking(db.getID());
  }

  public boolean confirmed(BookingBean db) {
    return confirmedBooking(db.getID());
  }


  /**
   * removes the given booking bean if its id can be found in the database.
   * 
   * @param db
   * @return
   */
  public boolean removeBooking(int booking_id) {
    boolean success = false;
    if (booking_id < 0)
      return success;
    String sql = "UPDATE booking SET deleted = 1 WHERE booking_id = ?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, booking_id);
      int result = statement.executeUpdate();
      success = (result > 0);
      // System.out.println("Database 728: "+ success);
    } catch (SQLException e) {
      // System.out.println("Database 730: "+ success);
      e.printStackTrace();
    }
    // System.out.println("Database 733: "+ success);
    return success;
  }

  public boolean removeBooking(Date start, String device_name) {
    boolean success = false;

    java.sql.Timestamp sqlStart = new java.sql.Timestamp(start.getTime());

    String sql = "UPDATE booking SET deleted = 1 WHERE start = ? AND device_name = ?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setTimestamp(1, sqlStart);
      statement.setString(2, device_name);
      int result = statement.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return success;
  }

  public boolean removeInvoice(Date start, String device_name) {
    boolean success = false;

    java.sql.Timestamp sqlStart = new java.sql.Timestamp(start.getTime());

    String sql = "UPDATE logs SET invoiced = 3 WHERE start = ? AND device_name = ?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setTimestamp(1, sqlStart);
      statement.setString(2, device_name);
      int result = statement.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return success;
  }

  public boolean markAsFaulty(Date start, String device_name) {
    boolean success = false;

    java.sql.Timestamp sqlStart = new java.sql.Timestamp(start.getTime());

    String sql = "UPDATE booking SET service = 'Faulty' WHERE start = ? AND device_name = ?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setTimestamp(1, sqlStart);
      statement.setString(2, device_name);
      int result = statement.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return success;
  }

  public boolean restoreBooking(int booking_id) {
    boolean success = false;
    if (booking_id < 0)
      return success;
    String sql = "UPDATE booking SET deleted = NULL WHERE booking_id = ?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, booking_id);
      int result = statement.executeUpdate();
      success = (result > 0);
      // System.out.println("Database 746: "+ success);
    } catch (SQLException e) {
      // System.out.println("Database 748: "+ success);
      e.printStackTrace();
    }
    // System.out.println("Database 751: "+ success);
    return success;
  }

  public boolean purgeBooking(int booking_id) {
    boolean success = false;
    if (booking_id < 0)
      return success;
    String sql = "DELETE from booking WHERE booking_id = ?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, booking_id);
      int result = statement.executeUpdate();
      success = (result > 0);
      // System.out.println("Database 764: "+ success);
    } catch (SQLException e) {
      // System.out.println("Database 766: "+ success);
      e.printStackTrace();
    }
    // System.out.println("Database 769: "+ success);
    return success;
  }

  public boolean denyBooking(int booking_id) {
    boolean success = false;
    if (booking_id < 0)
      return success;
    String sql = "UPDATE booking SET deleted = 1 WHERE booking_id = ?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, booking_id);
      int result = statement.executeUpdate();
      success = (result > 0);
      // System.out.println("Database 782: "+ success);
    } catch (SQLException e) {
      // System.out.println("Database 784: "+ success);
      e.printStackTrace();
    }
    // System.out.println("Database 787: "+ success);
    return success;

  }

  public boolean confirmBooking(int booking_id) {
    boolean success = false;
    if (booking_id < 0)
      return success;
    String sql = "UPDATE booking SET confirmation = NULL WHERE booking_id = ?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, booking_id);
      int result = statement.executeUpdate();
      success = (result > 0);
      // System.out.println(result + " Database 801: "+ success);
    } catch (SQLException e) {
      // System.out.println("Database 803: "+ success);
      e.printStackTrace();
    }
    // System.out.println(" Database 806: "+ success);
    return success;
  }

  public boolean confirmedBooking(int booking_id) {
    boolean success = false;
    String sql = "SELECT confirmation FROM booking WHERE booking_id = ?";

    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, booking_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        success = rs.getBoolean("confirmation");
      }
      // System.out.println("id: "+booking_id+" bool:"+success);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return success;
  }

  /**
   * removes the given device bean if its id can be found in the database.
   * 
   * @param db
   * @return
   */
  public boolean removeDevice(int deviceId) {
    boolean success = false;
    if (deviceId < 0)
      return success;
    String sql = "DELETE FROM devices WHERE device_id = ?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, deviceId);
      int result = statement.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return success;
  }

  /**
   * returns a DeviceBean for the given Id. If it does not exits or an error occurs null is returned
   * 
   * @param deviceId
   * @return
   */
  public DeviceBean getDeviceById(int deviceId) {
    DeviceBean devbean = null;
    if (deviceId < 0)
      return devbean;

    String sql = "SELECT * FROM devices WHERE device_id = ?";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, deviceId);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        devbean =
            new DeviceBean(deviceId, rs.getString("device_name"), rs.getString("description"),
                rs.getBoolean("restriction"));
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return devbean;
  }

  public boolean updateDevice(DeviceBean bean) {
    boolean success = false;
    if (bean.getId() < 0 || bean.getName() == null || bean.getName().isEmpty()
        || bean.getDescription() == null || bean.getDescription().isEmpty())
      return success;
    String sql =
        "UPDATE devices SET device_name = ?, description = ?, restriction = ? WHERE device_id = ?";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, bean.getName());
      statement.setString(2, bean.getDescription());
      statement.setBoolean(3, bean.getRestriction());
      statement.setInt(4, bean.getId());
      int result = statement.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return success;
  }


  // direct usage of connection to database. Should not be visible to the outside world
  /**
   * 
   * Undoes all changes made in the current transaction. Does not undo, if conn IS in auto commit
   * mode
   * 
   * @param conn
   * @param closeConnection
   */
  @SuppressWarnings("unused")
  private void rollback(Connection conn, boolean closeConnection) {

    try {
      if (!conn.getAutoCommit()) {
        conn.rollback();
      }
      if (closeConnection) {
        logout(conn);
      }
    } catch (SQLException e) {
      if (conn != null && closeConnection) {
        logout(conn);
      }
      // TODO log everything
      e.printStackTrace();
    }
  }

  /**
   * logs into database with the parameters given in {@link Database.init}
   * 
   * @return Connection, otherwise null if connecting to the database fails
   */
  private Connection login() {
    try {
      return DriverManager.getConnection(host, user, password);

    } catch (SQLException e) {
      // TODO log login failure
      e.printStackTrace();
    }
    return null;
  }

  /**
   * trys to close the given connection and release it
   * 
   * From java documentation: It is strongly recommended that an application explicitly commits or
   * rolls back an active transaction prior to calling the close method. If the close method is
   * called and there is an active transaction, the results are implementation-defined.
   * 
   * 
   * @param conn
   */
  private void logout(Connection conn) {
    try {
      conn.close();
    } catch (SQLException e) {
      // TODO log logout failure
      e.printStackTrace();
    }
    conn = null;
  }

  /**
   * return all devices that are in the database.
   * 
   * @return
   */
  public java.util.List<DeviceBean> getDevices() {
    ArrayList<DeviceBean> list = new ArrayList<DeviceBean>();
    String sql = "SELECT * FROM devices";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(new DeviceBean(rs.getInt("device_id"), rs.getString("device_name"), rs
            .getString("description"), rs.getBoolean("restriction")));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * return all device names to display inside the drop-down menu
   * 
   * @return
   */
  public ArrayList<String> getDeviceNames() {
    ArrayList<String> list = new ArrayList<String>();
    String sql = "SELECT device_name FROM devices";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("device_name"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * return the years which are in the logged months table
   * 
   * @return
   */
  public ArrayList<String> getLoggedMonths() {
    ArrayList<String> list = new ArrayList<String>();
    String sql = "SELECT DISTINCT MONTH(start) FROM logs";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("MONTH(start)"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * return the years which are in the logged years table
   * 
   * @return
   */
  public ArrayList<String> getLoggedYears() {
    ArrayList<String> list = new ArrayList<String>();
    String sql = "SELECT DISTINCT YEAR(start) FROM logs";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("YEAR(start)"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * return all role names to display inside the drop-down menu
   * 
   * @return
   */
  public ArrayList<String> getUserRoles() {
    ArrayList<String> list = new ArrayList<String>();
    String sql = "SELECT role_description FROM roles";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("role_description"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }

  public ArrayList<String> getUserWorkgroups() {
    ArrayList<String> list = new ArrayList<String>();
    String sql = "SELECT workgroup_name FROM workgroups ORDER BY workgroup_name";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("workgroup_name"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }


  // when invoice for the logs are generated the items in the logs are updated as invoiced
  public boolean itemInvoiced(int itemId) {
    boolean success = false;
    if (itemId < 0)
      return success;
    String sql = "UPDATE logs SET invoiced = 1 WHERE log_id = ?";
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, itemId);
      int result = statement.executeUpdate();
      success = (result > 0);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return success;

  }

  /**
   * return all group names to display inside the drop-down menu
   * 
   * @return
   */
  public ArrayList<String> getUserGroups() {
    ArrayList<String> list = new ArrayList<String>();
    String sql = "SELECT group_name FROM groups";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("group_name"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * return all device names to display inside the drop-down menu
   * 
   * @return
   */
  public ArrayList<String> getKostenstelleCodes() {
    ArrayList<String> list = new ArrayList<String>();
    String sql = "SELECT kostenstelle_code FROM kostenstelle";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("kostenstelle_code"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }


  public ArrayList<String> getProjects() {
    ArrayList<String> list = new ArrayList<String>();
    String sql = "SELECT DISTINCT project FROM user WHERE project IS NOT NULL";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); Statement statement = conn.createStatement()) {
      ResultSet rs = statement.executeQuery(sql);
      while (rs.next()) {
        list.add(rs.getString("project"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }

  // INSERT INTO role (name) VALUES ('unknown');
  public boolean addRole(String role) {
    boolean isSuccess = true;
    if (role == null) {
      return !isSuccess;
    }
    String sql = "INSERT INTO role (name) VALUES(?)";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, role);

      // execute the statement, data IS NOT commit yet
      statement.execute();
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      isSuccess = false;
    }
    return isSuccess;

  }

  /**
   * retuns the user id for a given user or -1 if user can not be found.
   * 
   * @param fullName
   * @return
   */
  public int findUserByFullName(String fullName) {
    // select user_id from users where name = '?';
    int userId = -1;
    if (fullName == null)
      return userId;

    String sql = "SELECT user_id FROM user WHERE user_name = ?";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, fullName);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        userId = rs.getInt("user_id");
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return userId;

  }

  /**
   * retuns the user id for a given user or -1 if user can not be found.
   * 
   * @param fullName
   * @return
   */
  public int findUserIDByLDAPID(String ldapID) {
    // select user_id from users where name = '?';
    int userId = -1;
    if (ldapID == null)
      return userId;

    String sql = "SELECT user_id FROM user WHERE user_ldap = ?";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, ldapID);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        userId = rs.getInt("user_id");
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return userId;

  }

  /**
   * retuns the user id of the main contact person for a given group member user or -1 if user can
   * not be found.
   * 
   * @param fullName
   * @return
   */
  public int findMainContactIDByGroupMembereFullName(String fullName) {
    // select user_id from users where name = '?';
    int userId = -1;
    if (fullName == null)
      return userId;

    String sql =
        "SELECT workgroups.`main_contact_id` FROM user INNER JOIN workgroups ON user.`workgroup_id`=workgroups.`workgroup_id` WHERE user.user_name=?";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, fullName);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        userId = rs.getInt("main_contact_id");
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return userId;

  }

  /**
   * try to find device_id in table that connects possible user_ids with resources or -1 if user can
   * not be found
   * 
   * @param deviceUserId
   * @return
   */
  public int findUserByDeviceUserId(String deviceUserId) {
    // TODO Auto-generated method stub
    return -1;
  }

  /**
   * Try to match a device user id to a full name in the database. Returns the user ids of all
   * matched users or an empty set
   * 
   * @param deviceUserId
   * @return
   */
  public Set<Integer> matchDeviceUserIdToUserName(String deviceUserId) {
    // select * from users where name like '%name%';
    HashSet<Integer> userIds = new HashSet<Integer>();

    if (deviceUserId == null)
      return userIds;

    String sql = "SELECT user_id FROM user WHERE user_name LIKE ?";
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      // % is an sql wild card used to match zero or more characters
      statement.setString(1, "%" + deviceUserId + "%");
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        userIds.add(rs.getInt("user_id"));
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userIds;
  }

  /**
   * returns -1 if that specific timeblock can not be found or the first timeblock that can be found
   * with that settings.
   * 
   * @param deviceId
   * @param userName
   * @param userFullName
   * @param start
   * @param end
   * @return
   */
  public int isPhysicalTimeBlock(int deviceId, String userName, String userFullName, Date start,
      Date end) {
    int userId = -1;
    String startStatement = "";
    String endStatement = "";
    if (start == null) {
      startStatement = "start_time IS NULL";
    } else {
      startStatement = "start_time = ?";
    }
    if (end == null) {
      endStatement = "end_time IS NULL";
    } else {
      endStatement = "end_time = ?";
    }

    String sql =
        "SELECT id FROM physical_time_blocks WHERE resource_id = ? AND resource_user_name = ? AND resource_specific_id = ? AND "
            + startStatement + "  AND " + endStatement;
    // The following statement is an try-with-devices statement, which declares two devices,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, deviceId);
      statement.setString(2, userFullName);
      statement.setString(3, userName);
      if (start != null) {
        statement.setTimestamp(4, new Timestamp(start.getTime()));
      }
      if (end != null) {
        statement.setTimestamp(5, new Timestamp(end.getTime()));
      }

      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        userId = rs.getInt("id");
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return userId;
  }


  /**
   * Adds a physical time block into the database. BE AWARE: It is added without further checking.
   * If you want to be sure that this time block was not added check it with isPhysicalTimeBlock
   * 
   * @param deviceId
   * @param userName
   * @param userFullName
   * @param start
   * @param end
   * @return
   */
  public boolean addPhysicalTimeBlock(int deviceId, String deviceName, String userName,
      String userFullName, Date startRound, Date start, Date end, long duration, String byUser,
      float Cost) {
    // select user_id from users where name = '?';
    boolean id = false;
    if (deviceId == -1 || userName == null) {
      return id;
    }

    int count = 0;

    String sqlCheck =
        "SELECT COUNT(*) FROM logs WHERE device_id = ? AND device_name = ? AND user_name = ? AND start = ? AND end = ? AND by_user = ?";

    try (Connection connCheck = login();
        PreparedStatement statementCheck =
            connCheck.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
      statementCheck.setInt(1, deviceId);
      statementCheck.setString(2, deviceName);
      statementCheck.setString(3, userName);


      if (start == null) {
        statementCheck.setNull(4, java.sql.Types.TIMESTAMP);
      } else {
        statementCheck.setTimestamp(4, new Timestamp(start.getTime()));
      }

      if (end == null) {
        statementCheck.setNull(5, java.sql.Types.TIMESTAMP);
      } else {
        statementCheck.setTimestamp(5, new Timestamp(end.getTime()));
      }

      statementCheck.setString(6, byUser);

      ResultSet resultCheck = statementCheck.executeQuery();
      // System.out.println("Exists: " + statementCheck);
      while (resultCheck.next()) {
        count = resultCheck.getInt(1);
      }
      // System.out.println("resultCheck: " + count);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (count == 0) {
      userFullName = userFullName.replace("\"", "");

      String sql =
          "INSERT INTO logs (device_id, device_name, user_name, user_full_name, start_round, start, end, duration, by_user, corrupt, cost) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

      // The following statement is an try-with-resources statement, which declares two resources,
      // conn and statement, which will be automatically closed when the try block terminates
      try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {
        statement.setInt(1, deviceId);
        statement.setString(2, deviceName);
        statement.setString(3, userName);
        statement.setString(4, userFullName);

        if (start == null) {
          statement.setNull(5, java.sql.Types.TIMESTAMP);
        } else {
          statement.setTimestamp(5, new Timestamp(startRound.getTime()));
        }

        if (start == null) {
          statement.setNull(6, java.sql.Types.TIMESTAMP);
        } else {
          statement.setTimestamp(6, new Timestamp(start.getTime()));
        }

        if (end == null) {
          statement.setNull(7, java.sql.Types.TIMESTAMP);
        } else {
          statement.setTimestamp(7, new Timestamp(end.getTime()));
        }

        statement.setLong(8, duration);

        statement.setString(9, byUser);

        // System.out.println("Username: " + userFullName + " length: " + userFullName.length()
        // + " is empty: " + userFullName.isEmpty());

        if (userFullName.length() < 2 || end == null || start == null) {
          int corrupt = 1;
          statement.setInt(10, corrupt);
        } else
          statement.setNull(10, java.sql.Types.INTEGER);

        statement.setFloat(11, Cost);

        // System.out.println("Statement: " + statement + " Sql: " + sql);

        statement.executeUpdate();
        // if (rs.next()) {
        // id =rs.getInt("id");
        // }
        statement.close();
      } catch (SQLException e) {
        e.printStackTrace();
        return false;
      }
      return true;
    }

    return true;

  }

  public boolean addUserGroup(String name) {
    String sql = "INSERT INTO usergroups (name) VALUES(?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, name);
      statement.execute();
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public boolean addResourceCostPerGroup(int resourceId, String usergroup, float cost) {
    // INSERT INTO group_resource_cost (usergroup,resource_id, cost) VALUES ('test2',1,25.24);
    String sql = "INSERT INTO group_resource_cost (usergroup,resource_id, cost) VALUES (?,?,?)";
    // The following statement is an try-with-resources statement, which declares two resources,
    // conn and statement, which will be automatically closed when the try block terminates
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, usergroup);
      statement.setInt(2, resourceId);
      statement.setFloat(3, cost);
      statement.execute();
      // nothing will be in the database, until you commit it!
      // conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
    return true;

  }

  public List<MachineOccupationBean> getPhysicalTimeBlocks() {
    String sql = "SELECT * FROM logs";
    List<MachineOccupationBean> obean = new ArrayList<MachineOccupationBean>();
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {

      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        MachineOccupationBean m = new MachineOccupationBean();
        m.setId(rs.getInt("log_id"));
        m.setDeviceId(rs.getInt("device_id"));
        m.setUserFullName(rs.getString("user_full_name"));
        m.setUserName(rs.getString("user_name"));
        m.setStart(rs.getTimestamp("start"));
        m.setEnd(rs.getTimestamp("end"));
        m.setDuration(rs.getInt("logs.duration"));
        obean.add(m);
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return obean;
  }

  public List<MachineOccupationBean> getMatchedTimeBlocks() {

    String sql =
        "SELECT DISTINCT logs.log_id logs.device_id, logs.device_name, user.kostenstelle, logs.start, logs.end, logs.duration, logs.cost, user.user_name  FROM logs INNER JOIN booking INNER JOIN user WHERE logs.`device_name` = booking.`device_name` AND logs.`start_round` = booking.`start` AND logs.`user_full_name` = user.`user_name`";

    List<MachineOccupationBean> obean = new ArrayList<MachineOccupationBean>();
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {

      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        MachineOccupationBean m = new MachineOccupationBean();
        m.setLogId(rs.getInt("logs.log_id"));
        m.setDeviceId(rs.getInt("logs.device_id"));
        m.setDeviceName(rs.getString("logs.device_name"));
        m.setUserFullName(rs.getString("user.user_name"));
        m.setStart(rs.getTimestamp("logs.start"));
        m.setEnd(rs.getTimestamp("logs.end"));
        m.setDuration(rs.getInt("logs.duration"));
        m.setCost(rs.getFloat("logs.cost"));
        obean.add(m);
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return obean;
  }

  public List<MachineOccupationBean> getMatchedTimeBlocksSetDates(String dateStart, String dateEnd) {

    String sql =
        "SELECT DISTINCT logs.log_id, logs.device_id, logs.device_name, user.kostenstelle, logs.start, logs.end, logs.duration, logs.cost, user.user_name  FROM logs INNER JOIN booking INNER JOIN user WHERE logs.invoiced IS NULL AND logs.`device_name` = booking.`device_name` AND logs.`start_round` = booking.`start`AND logs.`user_full_name` = user.`user_name` AND logs.start BETWEEN '"
            + dateStart + "' AND '" + dateEnd + "'";

    List<MachineOccupationBean> obean = new ArrayList<MachineOccupationBean>();
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {

      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        MachineOccupationBean m = new MachineOccupationBean();
        m.setLogId(rs.getInt("logs.log_id"));
        m.setDeviceId(rs.getInt("logs.device_id"));
        m.setDeviceName(rs.getString("logs.device_name"));
        m.setUserFullName(rs.getString("user.user_name"));
        m.setStart(rs.getTimestamp("logs.start"));
        m.setEnd(rs.getTimestamp("logs.end"));
        m.setDuration(rs.getInt("logs.duration"));
        m.setCost(rs.getFloat("logs.cost"));
        obean.add(m);
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return obean;
  }

  public List<MachineOccupationBean> getPhysicalTimeBlocksSetDates(String dateStart, String dateEnd) {

    String sql =
        "SELECT * FROM logs WHERE invoiced IS NULL AND corrupt IS NULL AND start BETWEEN '"
            + dateStart + "' AND '" + dateEnd + "'";
    List<MachineOccupationBean> obean = new ArrayList<MachineOccupationBean>();

    // System.out.println(sql);

    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {

      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        MachineOccupationBean m = new MachineOccupationBean();
        m.setLogId(rs.getInt("log_id"));
        m.setDeviceId(rs.getInt("device_id"));
        m.setUserFullName(rs.getString("user_full_name"));
        m.setUserName(rs.getString("user_name"));
        m.setStart(rs.getTimestamp("start"));
        m.setEnd(rs.getTimestamp("end"));
        m.setDuration(rs.getInt("duration"));
        m.setCost(rs.getFloat("cost"));
        obean.add(m);
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return obean;
  }

  public List<MachineOccupationBean> getNoCostTimeBlocks() {
    String sql = "SELECT * FROM logs WHERE cost = 0 AND invoiced IS NULL";
    List<MachineOccupationBean> obean = new ArrayList<MachineOccupationBean>();
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {

      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        MachineOccupationBean m = new MachineOccupationBean();
        m.setDeviceId(rs.getInt("device_id"));
        m.setDeviceName(rs.getString("logs.device_name"));
        m.setUserFullName(rs.getString("user_full_name"));
        m.setUserName(rs.getString("user_name"));
        m.setStart(rs.getTimestamp("start"));
        m.setEnd(rs.getTimestamp("end"));
        m.setDuration(rs.getInt("duration"));
        m.setCost(rs.getFloat("cost"));
        obean.add(m);
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return obean;
  }

  public List<MachineOccupationBean> getNoCostTimeBlocksSetDates(String dateStart, String dateEnd) {
    String sql =
        "SELECT * FROM logs WHERE cost = 0 AND start BETWEEN '" + dateStart + "' AND '" + dateEnd
            + "'";
    List<MachineOccupationBean> obean = new ArrayList<MachineOccupationBean>();
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {

      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        MachineOccupationBean m = new MachineOccupationBean();
        m.setLogId(rs.getInt("log_id"));
        m.setDeviceId(rs.getInt("device_id"));
        m.setDeviceName(rs.getString("logs.device_name"));
        m.setUserFullName(rs.getString("user_full_name"));
        m.setUserName(rs.getString("user_name"));
        m.setStart(rs.getTimestamp("start"));
        m.setEnd(rs.getTimestamp("end"));
        m.setDuration(rs.getInt("duration"));
        m.setCost(rs.getFloat("cost"));
        obean.add(m);
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return obean;
  }

  public List<MachineOccupationBean> getInvoicedTimeBlocksSetDates(String dateStart, String dateEnd) {
    String sql =
        "SELECT * FROM logs WHERE invoiced = 1 AND start BETWEEN '" + dateStart + "' AND '"
            + dateEnd + "'";
    List<MachineOccupationBean> obean = new ArrayList<MachineOccupationBean>();
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {

      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        MachineOccupationBean m = new MachineOccupationBean();
        m.setLogId(rs.getInt("log_id"));
        m.setDeviceId(rs.getInt("device_id"));
        m.setDeviceName(rs.getString("logs.device_name"));
        m.setUserFullName(rs.getString("user_full_name"));
        m.setUserName(rs.getString("user_name"));
        m.setStart(rs.getTimestamp("start"));
        m.setEnd(rs.getTimestamp("end"));
        m.setDuration(rs.getInt("logs.duration"));
        m.setCost(rs.getFloat("cost"));
        obean.add(m);
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return obean;
  }

  public List<MachineOccupationBean> getInvoiceCalendarTimeBlocksSetDates(String dateStart,
      String dateEnd) {
    String sql =
        "SELECT * FROM logs WHERE invoiced = 2 AND start BETWEEN '" + dateStart + "' AND '"
            + dateEnd + "'";
    // String sql =
    // "SELECT * FROM invoicing WHERE start BETWEEN '" + dateStart + "' AND '" + dateEnd + "'";
    List<MachineOccupationBean> obean = new ArrayList<MachineOccupationBean>();
    try (Connection conn = login(); PreparedStatement statement = conn.prepareStatement(sql)) {

      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        MachineOccupationBean m = new MachineOccupationBean();
        m.setLogId(rs.getInt("log_id"));
        m.setDeviceId(rs.getInt("device_id"));
        m.setDeviceName(rs.getString("logs.device_name"));
        m.setUserFullName(rs.getString("user_full_name"));
        m.setUserName(rs.getString("user_name"));
        m.setStart(rs.getTimestamp("start"));
        m.setEnd(rs.getTimestamp("end"));
        m.setDuration(rs.getInt("logs.duration"));
        m.setCost(rs.getFloat("cost"));
        obean.add(m);
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return obean;
  }

  public UserBean getUserByLDAPId(String userId) {
    String sql =
        "SELECT user.user_id, user.user_ldap, user.user_name, user.group_id, workgroups.workgroup_name, workgroups.institute_name, user.kostenstelle, user.project, user.admin_panel FROM user INNER JOIN workgroups ON user.workgroup_id=workgroups.workgroup_id WHERE user.user_ldap = ?";
    // 1 2 3 4
    UserBean ret = new UserBean();
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, userId);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        ret.setId(rs.getInt(1));
        ret.setLDAP(rs.getString(2));
        ret.setName(rs.getString(3));
        ret.setGroupID(rs.getString(4));
        ret.setWorkgroup(rs.getString(5));
        ret.setInstitute(rs.getString(6));
        ret.setKostenstelle(rs.getString(7));
        ret.setProject(rs.getString(8));
        ret.setAdminPanel(rs.getInt(9));
        // TODO get the correct ones
        List<String> kostenStelle = new ArrayList<String>();
        String k = getKostenstelleByUserId(ret.getId());
        kostenStelle.add(k.isEmpty() ? "unknown" : k);
        // ret.setKostenstelle(kostenStelle);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return ret;
  }

  public UserBean getUserById(int userId) {
    String sql =
        "SELECT user.user_id, user.user_name, user.kostenstelle, workgroups.workgroup_name, workgroups.institute_name, workgroups.street, workgroups.postcode, workgroups.city, user.project, user.admin_panel FROM user INNER JOIN workgroups on user.workgroup_id = workgroups.workgroup_id WHERE user.user_id = ?";
    // 1 2 3 4
    UserBean ret = new UserBean();
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setInt(1, userId);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        ret.setId(rs.getInt(1));
        ret.setName(rs.getString(2));
        ret.setKostenstelle(rs.getString(3));
        ret.setWorkgroup(rs.getString(4));
        ret.setInstitute(rs.getString(5));
        ret.setStreet(rs.getString(6));
        ret.setPostCode(rs.getString(7));
        ret.setCity(rs.getString(8));
        ret.setProject(rs.getString(9));
        ret.setAdminPanel(rs.getInt(10));
        // TODO get the correct ones - when one user has more than one kostenstelle then reimplement
        // using the code below
        // List<String> kostenStelle = new ArrayList<String>();
        // String k = getKostenstelleByUserId(ret.getId());
        // kostenStelle.add(k.isEmpty() ? "unknown" : k);
        // ret.setKostenstelle(kostenStelle);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return ret;
  }

  /**
   * Trys to get cost (per hour?) of a resource for a given user. User have to have a usergroup
   * which has a cost value for that resource. Returns -1 if it can not find a value in the
   * database.
   * 
   * @param uuid
   * @param calendar_id
   * @return
   */
  public float getCostByResourceAndUserIds(int uuid, int calendar_id) {
    // select group_resource_cost.cost from group_resource_cost INNER JOIN user_usergroup ON
    // group_resource_cost.usergroup=user_usergroup.usergroup WHERE user_usergroup.user_id=7 AND
    // group_resource_cost.resource_id=3;
    String sql =
        "SELECT cost from costs INNER JOIN user ON costs.group_id = user.group_id WHERE user.user_id=? AND costs.calendar_id=?";
    float cost = -1f;
    try (Connection conn = login();
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setInt(1, uuid);
      statement.setInt(2, calendar_id);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        cost = rs.getFloat("cost");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return cost;
  }

}
