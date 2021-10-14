/*
 * Copyright 2020-Present The Serverless Workflow Specification Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.serverlessworkflow.utils;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.actions.Action;
import io.serverlessworkflow.api.branches.Branch;
import io.serverlessworkflow.api.events.EventDefinition;
import io.serverlessworkflow.api.functions.FunctionDefinition;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.api.start.Start;
import io.serverlessworkflow.api.states.*;
import java.util.*;
import java.util.stream.Collectors;

/** Provides common utility methods to provide most often needed answers from a workflow */
public final class WorkflowUtils {
  private static final int DEFAULT_STARTING_STATE_POSITION = 0;

  /**
   * Gets State matching Start state. If start is not present returns first state. If start is
   * present, returns the matching start State. If matching state is not present, returns null
   *
   * @param workflow workflow
   * @return {@code state} when present else returns {@code null}
   */
  public static State getStartingState(Workflow workflow) {
    if (!hasStates(workflow)) {
      return null;
    }

    Start start = workflow.getStart();
    if (start == null) {
      return workflow.getStates().get(DEFAULT_STARTING_STATE_POSITION);
    } else {
      Optional<State> startingState =
          workflow.getStates().stream()
              .filter(state -> state.getName().equals(start.getStateName()))
              .findFirst();
      return startingState.orElse(null);
    }
  }

  /**
   * Gets List of States matching stateType
   *
   * @param workflow
   * @param stateType
   * @return {@code List<State>}. Returns {@code null} when workflow is null.
   */
  public static List<State> getStates(Workflow workflow, DefaultState.Type stateType) {
    if (!hasStates(workflow)) {
      return null;
    }

    return workflow.getStates().stream()
        .filter(state -> state.getType() == stateType)
        .collect(Collectors.toList());
  }

  /**
   * @return {@code List<io.serverlessworkflow.api.events.EventDefinition>}. Returns {@code NULL}
   *     when workflow is null or when workflow does not contain events
   */
  public static List<EventDefinition> getDefinedConsumedEvents(Workflow workflow) {
    return getDefinedEvents(workflow, EventDefinition.Kind.CONSUMED);
  }

  /**
   * @return {@code List<io.serverlessworkflow.api.events.EventDefinition>}. Returns {@code NULL}
   *     when workflow is null or when workflow does not contain events
   */
  public static List<EventDefinition> getDefinedProducedEvents(Workflow workflow) {
    return getDefinedEvents(workflow, EventDefinition.Kind.PRODUCED);
  }

  /**
   * Gets list of event definition matching eventKind
   *
   * @param workflow
   * @return {@code List<io.serverlessworkflow.api.events.EventDefinition>}. Returns {@code NULL}
   *     when workflow is null or when workflow does not contain events
   */
  public static List<EventDefinition> getDefinedEvents(
      Workflow workflow, EventDefinition.Kind eventKind) {
    if (!hasEventDefs(workflow)) {
      return null;
    }

    List<EventDefinition> eventDefs = workflow.getEvents().getEventDefs();
    return eventDefs.stream()
        .filter(eventDef -> eventDef.getKind() == eventKind)
        .collect(Collectors.toList());
  }

  /** @return {@code int} Returns count of defined event count matching eventKind */
  public static int getDefinedEventsCount(Workflow workflow, EventDefinition.Kind eventKind) {
    List<EventDefinition> definedEvents = getDefinedEvents(workflow, eventKind);
    return definedEvents == null ? 0 : definedEvents.size();
  }

  /** @return {@code int} Returns count of Defined Consumed Event Count */
  public static int getDefinedConsumedEventsCount(Workflow workflow) {
    return getDefinedEventsCount(workflow, EventDefinition.Kind.CONSUMED);
  }

  /** @return {@code int} Returns count of Defined Produced Event Count */
  public static int getDefinedProducedEventsCount(Workflow workflow) {
    return getDefinedEventsCount(workflow, EventDefinition.Kind.PRODUCED);
  }

  /**
   * Gets Consumed Events of parent workflow Iterates through states in parent workflow and collects
   * all the ConsumedEvents. Sub Workflows of the Workflow <strong>are not</strong> considered for
   * getting Consumed Events
   *
   * @return Returns {@code List<EventDefinition>}
   */
  public static List<EventDefinition> getWorkflowConsumedEvents(Workflow workflow) {
    return getWorkflowEventDefinitions(workflow, EventDefinition.Kind.CONSUMED);
  }

  /**
   * Gets Produced Events of parent workflow Iterates through states in parent workflow and collects
   * all the ConsumedEvents. Sub Workflows of the Workflow <strong>are not</strong> considered for
   * getting Consumed Events
   *
   * @return Returns {@code List<EventDefinition>}
   */
  public static List<EventDefinition> getWorkflowProducedEvents(Workflow workflow) {
    return getWorkflowEventDefinitions(workflow, EventDefinition.Kind.PRODUCED);
  }

  /**
   * Gets Events of parent workflow matching {@code EventDefinition.Kind} Iterates through states in
   * parent workflow and collects all the events matching {@code EventDefinition.Kind} .
   *
   * @return Returns {@code List<EventDefinition>}
   */
  private static List<EventDefinition> getWorkflowEventDefinitions(
      Workflow workflow, EventDefinition.Kind eventKind) {
    if (!hasStates(workflow)) {
      return null;
    }

    List<String> uniqueWorkflowEventsFromStates = getUniqueWorkflowEventsFromStates(workflow);
    List<EventDefinition> definedConsumedEvents = getDefinedEvents(workflow, eventKind);
    if (definedConsumedEvents == null) {
      return null;
    }
    return definedConsumedEvents.stream()
        .filter(definedEvent -> uniqueWorkflowEventsFromStates.contains(definedEvent.getName()))
        .collect(Collectors.toList());
  }

  /** Returns a list of unique event names from workflow states */
  private static List<String> getUniqueWorkflowEventsFromStates(Workflow workflow) {
    List<String> eventReferences = new ArrayList<>();

    for (State state : workflow.getStates()) {
      if (state instanceof SwitchState) {
        SwitchState switchState = (SwitchState) state;
        if (switchState.getEventConditions() != null) {
          switchState
              .getEventConditions()
              .forEach(eventCondition -> eventReferences.add(eventCondition.getEventRef()));
        }
      } else if (state instanceof CallbackState) {
        CallbackState callbackState = (CallbackState) state;
        if (callbackState.getEventRef() != null) eventReferences.add(callbackState.getEventRef());
        if (callbackState.getAction() != null && callbackState.getAction().getEventRef() != null) {
          eventReferences.addAll(getActionEvents(callbackState.getAction()));
        }
      } else if (state instanceof EventState) {
        EventState eventState = (EventState) state;
        if (eventState.getOnEvents() != null) {
          eventState
              .getOnEvents()
              .forEach(
                  onEvents -> {
                    eventReferences.addAll(onEvents.getEventRefs());
                    if (onEvents.getActions() != null) {
                      for (Action action : onEvents.getActions()) {
                        eventReferences.addAll(getActionEvents(action));
                      }
                    }
                  });
        }
      } else if (state instanceof OperationState) {
        OperationState operationState = (OperationState) state;
        if (operationState.getActions() != null) {
          for (Action action : operationState.getActions()) {
            eventReferences.addAll(getActionEvents(action));
          }
        }
      } else if (state instanceof ParallelState) {
        ParallelState parallelState = (ParallelState) state;
        if (parallelState.getBranches() != null) {
          for (Branch branch : parallelState.getBranches()) {
            if (branch.getActions() != null) {
              for (Action action : branch.getActions()) {
                eventReferences.addAll(getActionEvents(action));
              }
            }
          }
        }
      }
    }

    return eventReferences.stream().distinct().collect(Collectors.toList());
  }

  /**
   * @return Returns {@code int } Count of the workflow consumed events. <strong>Does not</strong>
   *     consider sub-workflows
   */
  public static int getWorkflowConsumedEventsCount(Workflow workflow) {
    List<EventDefinition> workflowConsumedEvents = getWorkflowConsumedEvents(workflow);
    return workflowConsumedEvents == null ? 0 : workflowConsumedEvents.size();
  }

  /**
   * @return Returns {@code int} Count of the workflow produced events. <strong>Does not</strong>
   *     consider sub-workflows in the count
   */
  public static int getWorkflowProducedEventsCount(Workflow workflow) {
    List<EventDefinition> workflowProducedEvents = getWorkflowProducedEvents(workflow);
    return workflowProducedEvents == null ? 0 : workflowProducedEvents.size();
  }

  /** @return Returns function definition for actions */
  public static List<FunctionDefinition> getFunctionDefinitionsForAction(
      Workflow workflow, String action) {
    if (hasFunctionDefs(workflow)) {
      return workflow.getFunctions().getFunctionDefs().stream()
          .filter(functionDef -> functionDef.getName().equals(action))
          .collect(Collectors.toList());
    }
    return null;
  }

  private static boolean hasFunctionDefs(Workflow workflow) {
    return workflow != null
        && workflow.getFunctions() != null
        && workflow.getFunctions().getFunctionDefs() != null
        && !workflow.getFunctions().getFunctionDefs().isEmpty();
  }

  /** Returns true if workflow has states, otherwise false */
  private static boolean hasStates(Workflow workflow) {
    return workflow != null && workflow.getStates() != null && !workflow.getStates().isEmpty();
  }

  /** Returns true if workflow has events definitions, otherwise false */
  private static boolean hasEventDefs(Workflow workflow) {
    return workflow != null
        && workflow.getEvents() != null
        && workflow.getEvents().getEventDefs() != null
        && !workflow.getEvents().getEventDefs().isEmpty();
  }

  /** Gets event refs of an action */
  private static List<String> getActionEvents(Action action) {
    List<String> actionEvents = new ArrayList<>();

    if (action != null && action.getEventRef() != null) {
      if (action.getEventRef().getTriggerEventRef() != null) {
        actionEvents.add(action.getEventRef().getTriggerEventRef());
      }
      if (action.getEventRef().getResultEventRef() != null) {
        actionEvents.add(action.getEventRef().getResultEventRef());
      }
    }

    return actionEvents;
  }
}
