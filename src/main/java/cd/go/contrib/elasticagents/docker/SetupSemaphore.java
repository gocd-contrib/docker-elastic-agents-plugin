/*
 * Copyright 2022 Thoughtworks, Inc.
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

import java.util.Map;
import java.util.concurrent.Semaphore;

class SetupSemaphore implements Runnable {
    private final Integer maxAllowedContainers;
    private final Map<?, ?> instances;
    private final Semaphore semaphore;

    public SetupSemaphore(Integer maxAllowedContainers, Map<?, ?> instances, Semaphore semaphore) {
        this.maxAllowedContainers = maxAllowedContainers;
        this.instances = instances;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        int currentContainers = instances.size();
        int availablePermits = maxAllowedContainers - currentContainers;
        if (availablePermits <= 0) {
            // no more capacity available.
            semaphore.drainPermits();
        } else {
            int semaphoreValueDifference = availablePermits - semaphore.availablePermits();
            if (semaphoreValueDifference > 0) {
                semaphore.release(semaphoreValueDifference);
            } else if (semaphoreValueDifference < 0) {
                semaphore.tryAcquire(Math.abs(semaphoreValueDifference));
            }
        }
    }
}
