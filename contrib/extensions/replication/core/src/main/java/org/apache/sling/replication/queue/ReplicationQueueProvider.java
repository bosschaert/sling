/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.replication.queue;

import java.util.Collection;

/**
 * A provider for {@link ReplicationQueue}s
 */
public interface ReplicationQueueProvider {

    /**
     * provide a named queue for the given agent or creates it if the queue doesn't exist
     *
     * @param agentName the replication agent needing the queue
     * @param name      the name of the queue to retrieve
     * @return a replication queue to be used for the given parameters
     * @throws ReplicationQueueException
     */
    ReplicationQueue getQueue(String agentName, String name)
            throws ReplicationQueueException;


    /**
     * get the default queue to be used for a certain agent
     *
     * @param agentName a replication agent
     * @return the default replication queue for the given agent
     * @throws ReplicationQueueException
     */
    ReplicationQueue getDefaultQueue(String agentName)
            throws ReplicationQueueException;

    /**
     * get all the available queues from this provider
     *
     * @return a collection of replication queues
     */
    Collection<ReplicationQueue> getAllQueues();

    /**
     * removes an existing queue owned by this provider
     *
     * @param queue a replication queue to be removed
     * @throws ReplicationQueueException
     */
    void removeQueue(ReplicationQueue queue) throws ReplicationQueueException;

    /**
     * enables queue driven processing for an agent
     *
     * @param agentName      a replication agent
     * @param queueProcessor the queue processor to be used
     */
    void enableQueueProcessing(String agentName, ReplicationQueueProcessor queueProcessor);


    /**
     * disables queue driven processing for an agent
     *
     * @param agentName a replication agent
     */
    void disableQueueProcessing(String agentName);
}
