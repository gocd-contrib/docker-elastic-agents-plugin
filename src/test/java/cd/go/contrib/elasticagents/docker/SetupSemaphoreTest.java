/*
 * Copyright 2016 ThoughtWorks, Inc.
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

package cd.go.contrib.elasticagents.docker;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SetupSemaphoreTest {

    @Test
    public void shouldDrainSemaphoreCompletely_when_maxAllowedContainersIsLessThanCurrentContainerCount() throws Exception {
        Map<?, ?> instances = instancesWithSize(10);
        Semaphore semaphore = new Semaphore(9, true);

        new SetupSemaphore(5, instances, semaphore).run();

        assertThat(semaphore.availablePermits(), is(0));
    }

    @Test
    public void shouldDecreaseSemaphore_when_maxAllowedContainersIsMoreThanCurrentContainerCount_and_availablePermitInSemaphoreIsLessThanNumberOfInstances() throws Exception {
        Map<?, ?> instances = instancesWithSize(10);
        Semaphore semaphore = new Semaphore(11, true);

        new SetupSemaphore(15, instances, semaphore).run();

        assertThat(semaphore.availablePermits(), is(5));
    }

    @Test
    public void shouldIncreaseSemaphore_when_maxAllowedContainersIsMoreThanCurrentContainerCount_and_availablePermitInSemaphoreIsLessThanNumberOfInstances() throws Exception {
        Map<?, ?> instances = instancesWithSize(10);
        Semaphore semaphore = new Semaphore(1, true);

        new SetupSemaphore(15, instances, semaphore).run();

        assertThat(semaphore.availablePermits(), is(5));
    }

    private Map<?, ?> instancesWithSize(int size) {
        Map<Object, Object> instances = new LinkedHashMap<>();

        while (size != 0) {
            instances.put(size, size);
            size--;
        }

        return instances;
    }
}
