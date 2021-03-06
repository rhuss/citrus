/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Looping test container iterating the nested test actions in case an error occurred in one
 * of them. Iteration continues until a aborting condition evaluates to true.
 * 
 * Number of iterations is kept in a index variable. The nested test actions can access this variable
 * as normal test variable.
 * 
 * Between the iterations container can sleep automatically a given amount of time.
 * 
 * @author Christoph Deppisch
 */
public class RepeatOnErrorUntilTrue extends AbstractIteratingTestAction {
    /** Auto sleep in seconds */
    private double autoSleep = 1.0;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(RepeatOnErrorUntilTrue.class);

    /**
     * Default constructor.
     */
    public RepeatOnErrorUntilTrue() {
        setName("repeat-on-error");
    }

    /**
     * @see com.consol.citrus.container.AbstractIteratingTestAction#executeIteration(com.consol.citrus.context.TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void executeIteration(TestContext context) {
        CitrusRuntimeException exception = null;

        while(!checkCondition()) {
            try {
                exception = null;
                executeActions(context);
                break;
            } catch (CitrusRuntimeException e) {
                exception = e;

                log.info("Caught exception of type " + e.getClass().getName() + " '" +
                        e.getMessage() + "' - performing retry #" + index);

                doAutoSleep();
                index++;
            }
        }

        if (exception != null) {
            log.info("All retries have failed - raising exception " + exception.getClass().getName());
            throw exception;
        }
    }

    /**
     * Sleep amount of time in between iterations.
     */
    private void doAutoSleep() {
        if (autoSleep > 0) {
            long autoSleepMs = Math.round(autoSleep * 1000L);
            log.info("Sleeping " + autoSleepMs + " milliseconds");

            try {
                Thread.sleep(autoSleepMs);
            } catch (InterruptedException e) {
                log.error("Error during doc generation", e);
            }

            log.info("Returning after " + autoSleepMs + " milliseconds");
        }
    }

    /**
     * Setter for auto sleep time (in seconds).
     * @param autoSleep
     */
    public void setAutoSleep(double autoSleep) {
        this.autoSleep = autoSleep;
    }

    /**
     * Gets the autoSleep.
     * @return the autoSleep
     */
    public double getAutoSleep() {
        return autoSleep;
    }
}
