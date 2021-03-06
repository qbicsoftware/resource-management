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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;
import facs.db.DBManager;
import facs.model.DeviceBean;
import facs.model.MachineOccupationBean;
import facs.utils.Formatter;

public class UploadBox extends CustomComponent implements Receiver, ProgressListener,
    FailedListener, SucceededListener {

  /**
   * 
   */
  private static final long serialVersionUID = -7104908728643930175L;

  // private final String UPLOAD_CAPTION = "Upload Statistics Here";
  private final String CAPTION = "Upload Instrument Statistics";

  ByteArrayOutputStream os = new ByteArrayOutputStream(10240);
  String filename;
  ProgressBar progress = new ProgressBar(0.0f);

  Label finishedMessage = new Label("Finito!");

  final String cvsSplitBy = ",";
  private HashMap<String, Integer> deviceNameToId;
  private NativeSelect devices;
  private Grid occupationGrid;

  public UploadBox() {
    this.setCaption(CAPTION);
    devices = new NativeSelect("Select Instrument");
    devices
        .setDescription("Select an instrument in order to upload information for that specific instrument.");
    devices.setNullSelectionAllowed(false);
    deviceNameToId = new HashMap<String, Integer>();
    for (DeviceBean bean : DBManager.getDatabaseInstance().getDevices()) {
      deviceNameToId.put(bean.getName(), bean.getId());
      devices.addItem(bean.getName());
    }
    occupationGrid = new Grid();
    occupationGrid.setSizeFull();

    final Upload upload = new Upload();
    upload.setReceiver(this);
    upload.addProgressListener(this);
    upload.addFailedListener(this);
    upload.addSucceededListener(this);
    upload.setVisible(false);

    devices.addValueChangeListener(new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 7890499571475184208L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        upload.setVisible(event.getProperty().getValue() != null);
      }
    });

    VerticalLayout panelContent = new VerticalLayout();
    panelContent.setSpacing(true);
    panelContent.addComponent(devices);
    panelContent.addComponent(upload);
    panelContent.addComponent(progress);
    panelContent.addComponent(occupationGrid);

    panelContent.setMargin(true);
    panelContent.setSpacing(true);

    progress.setVisible(false);

    setCompositionRoot(panelContent);
  }

  @Override
  public OutputStream receiveUpload(String filename, String mimeType) {
    this.filename = filename;
    os.reset();
    return os;
  }

  @Override
  public void updateProgress(long readBytes, long contentLength) {
    progress.setVisible(true);
    if (contentLength == -1)
      progress.setIndeterminate(true);
    else {
      progress.setIndeterminate(false);
      progress.setValue(((float) readBytes) / ((float) contentLength));
    }
  }

  @Override
  public void uploadSucceeded(SucceededEvent event) {

    progress.setVisible(false);

    String line = "";

    try {
      BeanItemContainer<MachineOccupationBean> container =
          new BeanItemContainer<MachineOccupationBean>(MachineOccupationBean.class);

      StringReader sr = new StringReader(os.toString());
      BufferedReader br = new BufferedReader(sr);

      br.readLine();

      while ((line = br.readLine()) != null) {
        String[] userInfo = line.split(cvsSplitBy);
        MachineOccupationBean bean = new MachineOccupationBean();
        bean.setBean(userInfo, deviceNameToId.get((getCurrentDevice())), deviceNameToId);

        try {
          bean.setBean(userInfo, deviceNameToId.get((getCurrentDevice())));
        } catch (Exception e) {
        }
        container.addBean(bean);

        int userId =
            DBManager.getDatabaseInstance().findUserByFullName(
                bean.getUserFullName().replace("\"", ""));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(bean.getStart());

        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % 30;
        calendar.add(Calendar.MINUTE, mod < 15 ? -mod : (30 - mod));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        float cost = -1.f;

        Date end = bean.getEnd() == null ? bean.getStart() : bean.getEnd();

        cost = getCost(userId, bean.getStart(), end, bean.getDeviceId());

        long s = bean.getStart().getTime();
        long e = bean.getEnd().getTime();
        long duration = e - s;
        int durationTimes = 0;
        float durationInHours = 0;

        if (duration <= 900000) {
          duration = 900000;
          durationInHours = (float) 0.25;
        } else {
          durationTimes = (int) (duration / 900000);
          duration = (durationTimes++) * 900000;
          durationInHours = (float) ((durationTimes++) * 0.25);
        }

        cost = getCostByDuration(userId, durationInHours, bean.getDeviceId());

        DBManager.getDatabaseInstance().addPhysicalTimeBlock(bean.getDeviceId(),
            devices.getValue().toString(), bean.getUserName(), bean.getUserFullName(),
            calendar.getTime(), bean.getStart(), bean.getEnd(), duration,
            LiferayAndVaadinUtils.getUser().getScreenName(), cost);

        occupationGrid.setContainerDataSource(container);
      }
    } catch (IOException e) {
      e.printStackTrace();
      showErrorNotification("Can't read uploaded file.",
          "An error occured during the upload process. Make sure that you didn't upload a corrupted file.");
      return;
    }
    showSuccessfulNotification("Nailed it! Upload successful.",
        "Hopefully all good! Successful uploads expectedly lead to successful parsing of the documents.");
  }

  private float getCost(int userId, Date start, Date end, int resourceId) {
    float cost = 0f;
    float costPerHour =
        DBManager.getDatabaseInstance().getCostByResourceAndUserIds(userId, resourceId);
    if (costPerHour > 0) {
      float hoursUsed = Formatter.toHours(start, end);
      System.out.println("Hours Used: " + hoursUsed);
      cost = hoursUsed * costPerHour;
      System.out.println("Cost: " + cost);
    }
    return cost;
  }

  private float getCostByDuration(int userId, float durationInHours, int resourceId) {
    float cost = 0f;
    float costPerHour =
        DBManager.getDatabaseInstance().getCostByResourceAndUserIds(userId, resourceId);
    if (costPerHour > 0) {
      System.out.println("durationInHours: " + durationInHours);
      cost = durationInHours * costPerHour;
      System.out.println("Cost: " + cost);
    }
    return cost;
  }

  private void showErrorNotification(String title, String description) {
    Notification notify = new Notification(title, description);
    notify.setDelayMsec(16000);
    notify.setPosition(Position.TOP_CENTER);
    notify.setIcon(FontAwesome.FROWN_O);
    notify.setStyleName(ValoTheme.NOTIFICATION_ERROR + " " + ValoTheme.NOTIFICATION_CLOSABLE);
    notify.show(Page.getCurrent());
  }

  private void showSuccessfulNotification(String title, String description) {
    Notification notify = new Notification(title, description);
    notify.setDelayMsec(8000);
    notify.setPosition(Position.TOP_CENTER);
    notify.setIcon(FontAwesome.SMILE_O);
    notify.setStyleName(ValoTheme.NOTIFICATION_SUCCESS + " " + ValoTheme.NOTIFICATION_CLOSABLE);
    notify.show(Page.getCurrent());
  }

  private String getCurrentDevice() {
    return (String) devices.getValue();
  }

  @Override
  public void uploadFailed(FailedEvent event) {
    showErrorNotification(
        "oops! Upload failed, reason unknown!",
        "Obviously there is a problem, please ask yourself the following questions: Did I do everything correctly? Did I focus on my work? If the answers are 'Yes' please call a technician.");
  }

}
