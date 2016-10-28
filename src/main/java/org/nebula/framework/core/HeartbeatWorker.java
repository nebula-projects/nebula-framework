/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nebula.framework.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebula.framework.client.NebulaClient;
import org.nebula.framework.client.request.HeartbeatRequest;
import org.nebula.framework.client.response.HeartbeatResponse;

import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class HeartbeatWorker implements Runnable {

  private final static Log log = LogFactory.getLog(HeartbeatWorker.class);

  private NebulaClient nebulaClient;

  private int defaultHeatbeatIntervalSecs;

  private String registrationId;

  private String ip = "Unknown";

  private String host = "Unknown";

  private String processId = "Unknown";

  private String workingDir = "Unknown";

  private List<StatusChangedListener> listeners;

  public HeartbeatWorker(NebulaClient nebulaClient, int defaultHeatbeatIntervalSecs,
                         String registrationId) {
    this.nebulaClient = nebulaClient;
    this.defaultHeatbeatIntervalSecs = defaultHeatbeatIntervalSecs;
    this.registrationId = registrationId;
    this.listeners = new ArrayList<StatusChangedListener>();

    acquireIp();
    acquireHost();
    acquireProcessId();
    acquireWorkingDir();
  }

  public void addStatusChangedListener(StatusChangedListener listener) {
    listeners.add(listener);
  }

  public void run() {

    while (true) {

      sleep(defaultHeatbeatIntervalSecs);

      runOnce();

    }
  }

  public void runOnce() {
    HeartbeatResponse response = defaultHeatbeatResponse();

    try {
      response = heatbeat(response);

      // TODO the worker run parameters would be configured from console
      // and
      // auto adjust according to statistics.
      // e.g. the ratio of pollThreads/maxExecutionThreads
      // if (statusChanged(response)) {
      // for (StatusChangedListener listener : listeners)
      // listener.statusChanged(response.getNewStatus(),
      // response.getNewPollThreads(),
      // response.getNewMaxExecutionThreads());
      // }
    } catch (Exception e) {
      log.warn("Heatbeat failed for registrationId " + registrationId, e);
      // We ignore the heatbeat failure.
    }
  }

  private HeartbeatResponse heatbeat(HeartbeatResponse previousResponse)
      throws Exception {
    HeartbeatRequest request = new HeartbeatRequest();

    request.setRegistrationId(previousResponse.getRegistrationId());
    request.setHost(host);
    request.setIp(ip);
    request.setProcessId(processId);
    request.setWorkingDir(workingDir);

    return nebulaClient.post(request);

  }

  private HeartbeatResponse defaultHeatbeatResponse() {
    HeartbeatResponse response = new HeartbeatResponse();
    response.setIntervalSecs(defaultHeatbeatIntervalSecs);
    response.setRegistrationId(registrationId);
    response.setNewStatus(HeartbeatResponse.Status.RUNNING);

    return response;
  }

  private void sleep(int secs) {
    try {
      Thread.sleep(1000 * secs);
    } catch (InterruptedException e) {
      // Ignore
    }
  }

  private void acquireIp() {
    Enumeration<NetworkInterface> net = null;
    try {
      net = NetworkInterface.getNetworkInterfaces();

      while (net.hasMoreElements()) {
        NetworkInterface element = net.nextElement();
        Enumeration<InetAddress> addresses = element.getInetAddresses();
        while (addresses.hasMoreElements()) {

          InetAddress ipAddress = addresses.nextElement();
          if (ipAddress instanceof Inet4Address) {

            if (ipAddress.isSiteLocalAddress()) {
              ip = ipAddress.getHostAddress();
            }
          }
        }
      }
    } catch (Exception e) {
      // ignore
    }
  }

  private void acquireHost() {
    try {
      host = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      // ignore
    }
  }

  private void acquireProcessId() {
    try {
      processId = ManagementFactory.getRuntimeMXBean().getName()
          .split("@")[0];
    } catch (Exception e) {
      // ignore
    }
  }

  private void acquireWorkingDir() {
    workingDir = System.getProperty("user.dir");
  }

}
