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
package facs.components;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.liferay.portal.model.User;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.themes.ValoTheme;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;
import facs.db.DBManager;
import facs.model.DeviceBean;

public class Settings extends CustomComponent {
  private static final long serialVersionUID = 2183973381935176872L;
  private Grid devicesGrid;


  public Settings(User user) {

    Date dNow = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("dd.MMM.yyyy HH:mm:ss");
    System.out.println(ft.format(dNow) + "  INFO  Settings accessed! - User: "
        + LiferayAndVaadinUtils.getUser().getScreenName());

    this.setCaption("Settings");
    TabSheet settings = new TabSheet();
    settings.addStyleName(ValoTheme.TABSHEET_FRAMED);
    settings.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);

    // TODO: a new device functionality is temporarily removed from the user interface
    // settings.addTab(newDeviceGrid());

    // upload csv files of devices
    settings.addTab(new UploadBox());

    setCompositionRoot(settings);
  }

  // temporarily unavailable: see above
  /*
   * private Component newDeviceGrid() {
   * 
   * 
   * VerticalLayout devicesLayout = new VerticalLayout(); devicesLayout.setCaption("Devices");
   * 
   * HorizontalLayout buttonLayout = new HorizontalLayout(); Button add = new Button("Add");
   * 
   * add.setIcon(FontAwesome.PLUS);
   * 
   * // there will now be space around the test component // components added to the test component
   * will now not stick together but have space between // them devicesLayout.setMargin(true);
   * devicesLayout.setSpacing(true); buttonLayout.setMargin(true); buttonLayout.setSpacing(true);
   * 
   * add.addClickListener(new ClickListener() {
   * 
   * 
   * private static final long serialVersionUID = 1920052856393517754L;
   * 
   * @Override public void buttonClick(ClickEvent event) { addNewDevice(); }
   * 
   * }); buttonLayout.addComponent(add); BeanItemContainer<DeviceBean> devices = getDevices();
   * 
   * GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(devices);
   * gpc.addGeneratedProperty("delete", new PropertyValueGenerator<String>() {
   * 
   * 
   * private static final long serialVersionUID = 4398909257922492690L;
   * 
   * @Override public String getValue(Item item, Object itemId, Object propertyId) { // return
   * FontAwesome.TRASH_O.getHtml(); // The caption return "Delete"; // The caption
   * 
   * }
   * 
   * @Override public Class<String> getType() { return String.class; } });
   * 
   * 
   * devicesGrid = new Grid(gpc); // Create a grid
   * 
   * // devicesGrid.setWidth("100%"); devicesGrid.setSizeFull();
   * devicesGrid.setSelectionMode(SelectionMode.SINGLE);
   * devicesGrid.getColumn("delete").setRenderer(new HtmlRenderer()); // Render a button that
   * deletes the data row (item) devicesGrid.getColumn("delete").setRenderer( new ButtonRenderer(new
   * ClickableRenderer.RendererClickListener() {
   * 
   * private static final long serialVersionUID = 1217127696779125401L;
   * 
   * @Override public void click(RendererClickEvent event) { removeDevice((DeviceBean)
   * event.getItemId()); } }));
   * 
   * // devicesGrid.setEditorEnabled(true);
   * 
   * devicesLayout.addComponent(buttonLayout); devicesLayout.addComponent(devicesGrid);
   * 
   * // TODO filtering // HeaderRow filterRow = devicesGrid.prependHeaderRow();
   * 
   * return devicesLayout; }
   */

  /*
   * private BeanItemContainer<DeviceBean> getDevices() { BeanItemContainer<DeviceBean> devices =
   * new BeanItemContainer<DeviceBean>(DeviceBean.class); List<DeviceBean> devs =
   * DBManager.getDatabaseInstance().getDevices(); assert devs != null; devices.addAll(devs); return
   * devices; }
   */


  protected void removeDevice(DeviceBean db) {
    boolean removed = DBManager.getDatabaseInstance().removeDevice(db);
    if (removed) {
      devicesGrid.getContainerDataSource().removeItem(db);
    } else {
      // TODO log failed operation
      Notification.show("Failed to remove instrument from database.", Type.ERROR_MESSAGE);
    }
  }

  /*
   * private void addNewDevice() { final Window subWindow = new Window("Add Device"); FormLayout
   * form = new FormLayout(); form.setMargin(true); final TextField name = new TextField();
   * name.setImmediate(true); name.addValidator(new
   * StringLengthValidator("The name must be 1-85 letters long (Was {0}).", 1, 85, true));
   * name.setCaption("Name of new device"); form.addComponent(name); final TextArea description =
   * new TextArea(); description.setImmediate(true); description.addValidator(new
   * StringLengthValidator( "The name must be 1-255 letters long (Was {0}).", 1, 255, true));
   * description.setCaption("Description"); form.addComponent(description); final OptionGroup
   * restricted = new OptionGroup("Is Device restricted by operators?"); restricted.addItem("yes");
   * restricted.setMultiSelect(true); form.addComponent(restricted); HorizontalLayout buttons = new
   * HorizontalLayout(); Button save = new Button("save"); buttons.addComponent(save); Button
   * discard = new Button("discard"); discard
   * .setDescription("discarding will abort the process of adding a new device into the databse.");
   * buttons.addComponent(discard); buttons.setSpacing(true); form.addComponent(buttons);
   * subWindow.setContent(form);
   * 
   * form.setMargin(true); form.setSpacing(true); buttons.setMargin(true); buttons.setSpacing(true);
   * 
   * // Center it in the browser window subWindow.center(); subWindow.setModal(true);
   * subWindow.setWidth("50%"); // Open it in the UI UI.getCurrent().addWindow(subWindow);
   * 
   * discard.addClickListener(new ClickListener() {
   * 
   * private static final long serialVersionUID = -5808910314649620731L;
   * 
   * @Override public void buttonClick(ClickEvent event) { subWindow.close(); } });
   * save.addClickListener(new ClickListener() {
   * 
   * private static final long serialVersionUID = 3748395242651585005L;
   * 
   * @Override public void buttonClick(ClickEvent event) { if (name.isValid() &&
   * description.isValid()) { Set<String> restr = (Set<String>) restricted.getValue(); int deviceId
   * = DBManager.getDatabaseInstance().addDevice(name.getValue(), description.getValue(),
   * (restr.size() == 1)); DeviceBean bean = new DeviceBean(deviceId, name.getValue(),
   * description.getValue(), (restr.size() == 1)); devicesGrid.addRow(bean); } else {
   * Notification.show("Failed to add device to database."); } } });
   * 
   * // DeviceBean db = new DeviceBean(0, "Device 1","some description1", false); // TODO // add to
   * database
   * 
   * boolean added = false;//DBManager.getDatabaseInstance().addDevice(db); //TODO test //add to
   * grid if(added){ devicesGrid.addRow(db); }else{ //TODO log failed operation
   * Notification.show("Failed to add device to database.", Type.ERROR_MESSAGE); }
   * 
   * }
   */


}
