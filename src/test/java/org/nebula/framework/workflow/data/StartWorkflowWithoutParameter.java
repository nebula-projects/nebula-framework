package org.nebula.framework.workflow.data;

import org.nebula.framework.annotation.Start;
import org.nebula.framework.annotation.Workflow;

@Workflow
public interface StartWorkflowWithoutParameter {

  @Start
  public void testStart();

}