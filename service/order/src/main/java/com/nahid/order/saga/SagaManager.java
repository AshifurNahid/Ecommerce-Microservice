package com.nahid.order.saga;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class SagaManager {

    private final List<SagaCommand> commands = new ArrayList<>();
    private final Deque<SagaCommand> executedCommands = new ArrayDeque<>();

    public void addStep(SagaCommand command) {
        commands.add(command);
    }

    public void execute() throws Exception {
        try {
            for (SagaCommand command : commands) {
                command.execute();
                executedCommands.push(command);
            }
        } catch (Exception ex) {
            rollback();
            throw ex;
        }
    }

    private void rollback() {
        while (!executedCommands.isEmpty()) {
            try {
                executedCommands.pop().compensate();
            } catch (Exception rollbackEx) {
                // log and continue rollback
            }
        }
    }
}
