package org.jboss.qe.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.descriptions.NonResolvingResourceDescriptionResolver;
import org.jboss.as.server.suspend.SuspendController;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.requestcontroller.ControlPoint;
import org.wildfly.extension.requestcontroller.RequestController;
import org.wildfly.extension.requestcontroller.RunResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 20.4.16.
 */
public class RunTestOperationHandler implements OperationStepHandler {

    private static final String TASK_COUNT = "task-count";

    private final InjectedValue<RequestController> requestControllerInjectedValue = new InjectedValue<>();
    private final InjectedValue<SuspendController> suspendControllerInjectedValue = new InjectedValue<>();

    public static final OperationDefinition DEFINITION = new SimpleOperationDefinitionBuilder("run-test",
            new NonResolvingResourceDescriptionResolver())
            .setParameters(new SimpleAttributeDefinition(TASK_COUNT, ModelType.INT, false))
            .setRuntimeOnly()
            .build();

    public static final OperationStepHandler INSTANCE = new RunTestOperationHandler();

    private RequestController requestController;
    private SuspendController suspendController;

    private RunTestOperationHandler() {}

    private AtomicInteger counter = new AtomicInteger(0);

    private static final long DEFAULT_TIMEOUT = 60000;
    private static final int DEFAULT_TASK_COUNT = 20;

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {

        int taskCount = DEFAULT_TASK_COUNT;
        this.requestController = requestControllerInjectedValue.getValue();
        this.suspendController = suspendControllerInjectedValue.getValue();
        ControlPoint controlPoint = this.requestController.getControlPoint("ejbRequestControllerTest", "bean");
        ExecutorService executor = Executors.newSingleThreadExecutor();

        /*final ModelNode mimetypes = context.readResourceForUpdate(PathAddress.EMPTY_ADDRESS).getModel().get(PARAM);
        if (operation.hasDefined("param-name") && operation.hasDefined("param-value")) {
            mimetypes.get(operation.get("param-name").asString()).set(operation.get("param-value").asString());
        } else {
            throw new OperationFailedException("Operation failed");
        }*/

        if (operation.hasDefined(TASK_COUNT)) {
            taskCount = operation.get(TASK_COUNT).asInt();
        }

        try {
            suspendController.suspend(0);
            waitForSuspend(DEFAULT_TIMEOUT);
            for (int i = 0; i < taskCount; i++) {
                RunResult runResult = controlPoint.beginRequest();
                if (runResult == RunResult.RUN) {
                    throw new IllegalStateException("Server should be already suspended and reject all requests!");
                } else {
                    controlPoint.queueTask(new DummyWorker(controlPoint), executor, DEFAULT_TIMEOUT, null, false);
                }
            }
            //resume and wait for running
            this.requestController.resume();
            waitForRunning(DEFAULT_TIMEOUT);

            //wait for execution of tasks to finnish
            waitForTasksEnded(DEFAULT_TIMEOUT, taskCount);

        } catch (Exception e) {
            throw new OperationFailedException(e);
        } finally {
            executor.shutdown();
            suspendController.resume();
        }

        context.stepCompleted();
    }

    private void waitForSuspend(long timeout) throws InterruptedException, TimeoutException {
        waitFor(timeout, new Condition() {
            @Override
            public boolean isConditionMet() {
                return requestController.isPaused();
            }
        });
    }

    private void waitForRunning(long timeout) throws InterruptedException, TimeoutException {
        waitFor(timeout, new Condition() {
            @Override
            public boolean isConditionMet() {
                return !requestController.isPaused();
            }
        });
    }

    private void waitForTasksEnded(long timeout, final int finalTaskCount) throws TimeoutException, InterruptedException {
        waitFor(timeout, new Condition() {
            @Override
            public boolean isConditionMet() {
                synchronized(this) {
                    return counter.get() != finalTaskCount;
                }
            }
        });
    }

    private void waitFor(long timeout, Condition condition) throws InterruptedException, TimeoutException {
        long end = System.currentTimeMillis() + timeout;
        while (!condition.isConditionMet()) {
            if (System.currentTimeMillis() > end) {
                throw new TimeoutException("Waiting timed out!");
            }
            Thread.sleep(500);
        }
    }

    private interface Condition {

        boolean isConditionMet();

    }

    private class DummyWorker implements Runnable {

        private ControlPoint controlPoint;

        DummyWorker(ControlPoint controlPoint) {
            this.controlPoint = controlPoint;
        }

        @Override
        public void run() {
            counter.getAndIncrement();
            this.controlPoint.requestComplete();
        }
    }
}
