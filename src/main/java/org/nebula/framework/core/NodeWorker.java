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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class NodeWorker<E> {

  private final Log log = LogFactory.getLog(getClass());

  private List<NodeDefinitionRealm>
      nodeDefinitionRealms =
      new ArrayList<NodeDefinitionRealm>();

  private NebulaClient nebulaClient;

  private Configuration configuration;

  private volatile boolean isStarted = false;

  private ExecutorService nodeExecutor;

  private ExecutorService heartbeatExecutor;

  private NodeRegistry<E> registry;

  public NodeWorker(NebulaClient nebulaClient, Configuration configuration) {
    this.nebulaClient = nebulaClient;
    this.configuration = configuration;
  }

  /**
   * Add the node implementation.
   *
   * @param nodeImplementation the implementation of the interface with annotation {@link
   *                           org.nebula.framework.annotation.Workflow} or {@link
   *                           org.nebula.framework.annotation.Activity}
   * @throws IllegalArgumentException if the nodeImplementation is null or already added into the
   *                                  worker; or if the realms is null or empty.
   */
  public synchronized void add(Class nodeImplementation, List<String> realms) {
    E nodeDefinition = createNodeDefinition(nodeImplementation);

    checkDuplicate(nodeDefinition);

    nodeDefinitionRealms.add(createNodeDefinitionRealm(nodeDefinition, realms));

  }

  public boolean isStarted() {
    return isStarted;
  }

  public List<NodeDefinitionRealm> getNodeDefinitionRealms() {
    return nodeDefinitionRealms;
  }

  private void checkDuplicate(E definition) {

    for (NodeDefinitionRealm nodeDefinitionRealm : nodeDefinitionRealms) {
      if (nodeDefinitionRealm.hasDefinition(definition)) {
        throw new IllegalArgumentException("Duplicate " + nodeDefinitionRealm.getNodeInfo());
      }
    }
  }

  protected abstract E createNodeDefinition(Class nodeImplementation);

  protected abstract NodeDefinitionRealm createNodeDefinitionRealm(E nodeDefinition,
                                                                   List<String> realms);

//  protected abstract Runnable createEvenPoller(NebulaClient nebulaClient, E definition,
//                                               List<String> realms, Configuration configuration);

  protected abstract Runnable createEvenPoller(NebulaClient nebulaClient, E nodeDefinition, List<String> realms, Configuration configuration);

  protected abstract NodeRegistry createNodeRegistry(NebulaClient nebulaClient);


  /**
   * Start the node.
   *
   * @throws IllegalStateException if there is no NodeDefinition for running.
   */
  public synchronized void start() {

    if (isStarted) {
      log.warn("The Node Worker already started.");
      return;
    }

    if (nodeDefinitionRealms.size() == 0) {
      throw new IllegalStateException("No definition for running.");
    }

    isStarted = true;

    try {

      registerAndStartHeatbeat();

      for (NodeDefinitionRealm nodeDefinitionRealm : nodeDefinitionRealms) {
        startNode(nodeDefinitionRealm);
      }

    } catch (Exception e) {
      stop();
      throw new IllegalStateException("The node worker failed to start.", e);
    }

    log.info("The Node Worker started.");

  }

  protected void startNode(NodeDefinitionRealm nodeDefinitionRealm) {

    int pollThreads = configuration.getPollThreads();

    nodeExecutor = Executors
        .newFixedThreadPool(pollThreads);

    for (int i = 0; i < pollThreads; i++) {
      nodeExecutor.execute(createEvenPoller(nebulaClient, (E)nodeDefinitionRealm.getDefinition(), nodeDefinitionRealm.getRealms(), configuration));
    }
  }

  public void stop() {

    if (isStarted) {

      if (registry != null) {
        registry.cancelRegister();
      }

      shutdownExecutor(heartbeatExecutor);
      shutdownExecutor(nodeExecutor);
    }

    isStarted = false;
    log.info("The Node Worker stopped.");
  }


  private void shutdownExecutor(ExecutorService executor) {
    if (executor != null) {
      executor.shutdown();
      try {
        executor.awaitTermination(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        //ignore the exception
      }
    }
  }

  /**
   * If it failed to register with the server for one node definition, the exception is thrown and
   * the NodeWorker will be stopped. The worker still works even if the heartbeat failed.
   */
  private void registerAndStartHeatbeat() throws Exception {

    registry = createNodeRegistry(nebulaClient);

    heartbeatExecutor = Executors
        .newFixedThreadPool(nodeDefinitionRealms.size());

    for (NodeDefinitionRealm nodeDefinitionRealm : nodeDefinitionRealms) {

      String
          registrationId =
          registry.register((E) nodeDefinitionRealm.getDefinition(),
                            nodeDefinitionRealm.getRealms());

      log.debug(nodeDefinitionRealm.getNodeInfo()
                + ", registerId=" + registrationId + ", path=" + getClass().getResource("/")
                    .getPath());

      heartbeat(heartbeatExecutor, registrationId);
    }
  }

  private void heartbeat(ExecutorService heartbeatExecutor,
                         String registrationId) {

    HeartbeatWorker heartbeatWorker = new HeartbeatWorker(nebulaClient, configuration
        .getHeartbeatInSeconds(), registrationId);

    //Use the configuration from server before the WorkflowWorker start
    heartbeatWorker.runOnce();

    heartbeatExecutor.execute(heartbeatWorker);

  }


  public static abstract class NodeDefinitionRealm<E> {

    private E definition;
    private List<String> realms;

    protected NodeDefinitionRealm(E definition, List<String> realms) {
      if (definition == null) {
        throw new IllegalArgumentException("The definition can't be null.");
      }
      if (realms == null || realms.size() == 0) {
        throw new IllegalArgumentException("The realms can't be empty.");
      }
      this.definition = definition;
      this.realms = realms;
    }

    public E getDefinition() {
      return definition;
    }

    public List<String> getRealms() {
      return realms;
    }

    protected abstract boolean hasDefinition(E definition);

    protected abstract String getNodeInfo();
  }

}