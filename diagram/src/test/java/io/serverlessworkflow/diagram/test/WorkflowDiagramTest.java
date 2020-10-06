/*
 * Copyright 2020-Present The Serverless Workflow Specification Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.serverlessworkflow.diagram.test;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.interfaces.WorkflowDiagram;
import io.serverlessworkflow.diagram.WorkflowDiagramImpl;
import io.serverlessworkflow.diagram.test.utils.DiagramTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WorkflowDiagramTest {

    @ParameterizedTest
    @ValueSource(strings = {"/examples/applicantrequest.json", "/examples/applicantrequest.yml",
            "/examples/carauctionbids.json", "/examples/carauctionbids.yml",
            "/examples/creditcheck.json", "/examples/creditcheck.yml",
            "/examples/eventbasedgreeting.json", "/examples/eventbasedgreeting.yml",
            "/examples/finalizecollegeapplication.json", "/examples/finalizecollegeapplication.yml",
            "/examples/greeting.json", "/examples/greeting.yml",
            "/examples/helloworld.json", "/examples/helloworld.yml",
            "/examples/jobmonitoring.json", "/examples/jobmonitoring.yml",
            "/examples/monitorpatient.json", "/examples/monitorpatient.yml",
            "/examples/parallel.json", "/examples/parallel.yml",
            "/examples/provisionorder.json", "/examples/provisionorder.yml",
            "/examples/sendcloudevent.json", "/examples/sendcloudevent.yml",
            "/examples/solvemathproblems.json", "/examples/solvemathproblems.yml",
            "/examples/foreachstatewithactions.json", "/examples/foreachstatewithactions.yml",
            "/examples/periodicinboxcheck.json", "/examples/periodicinboxcheck.yml",
            "/examples/vetappointmentservice.json", "/examples/vetappointmentservice.yml",
            "/examples/eventbasedtransition.json", "/examples/eventbasedtransition.yml",
            "/examples/singleeventstate.json", "/examples/singleeventstate.yml",
            "/examples/singleswitchstateeventconditions.json", "/examples/singleswitchstateeventconditions.yml"
    })
    public void testSpecExamplesParsing(String workflowLocation) throws Exception {
        Workflow workflow = Workflow.fromSource(DiagramTestUtils.readWorkflowFile(workflowLocation));

        assertNotNull(workflow);
        assertNotNull(workflow.getId());
        assertNotNull(workflow.getName());
        assertNotNull(workflow.getStates());

        WorkflowDiagram workflowDiagram = new WorkflowDiagramImpl();
        workflowDiagram.setWorkflow(workflow);

        String diagramSVG = workflowDiagram.getSvgDiagram();
        Assertions.assertNotNull(diagramSVG);
    }
}
