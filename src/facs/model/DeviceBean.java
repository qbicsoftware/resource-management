package facs.model;

import java.io.Serializable;

public class DeviceBean implements Serializable {
  private static final long serialVersionUID = -6717244163406823687L;

  private int id;
  private String name;
  private String description;
  private boolean restriction; 
  
  public DeviceBean(int id, String name, String description, boolean restriction) {
    super();
    this.id = id;
    this.name = name;
    this.setDescription(description);
    this.setRestriction(restriction);
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean getRestriction() {
    return restriction;
  }

  public void setRestriction(boolean restriction) {
    this.restriction = restriction;
  }
  
}
