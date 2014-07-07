/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.testing.simple.simpletests;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.junit.annotations.TestParameter;
import org.apache.sling.junit.annotations.TestReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.naming.NamingException;
import org.apache.sling.performance.PerformanceRunner;
import org.apache.sling.performance.annotation.PerformanceTest;

//@RunWith(SlingAnnotationsTestRunner.class)
@RunWith(PerformanceRunner.class)
public class SimpleTest {

    @TestReference
    private ResourceResolverFactory resolverFactory;

    private Session session;

    private Node testRoot;
    
    private ResourceResolver adminResolver;

    @TestParameter
    public String path;

    @Before
    public void beforeSuite() throws Exception {
        adminResolver = this.resolverFactory.getAdministrativeResourceResolver(null);
        this.session = adminResolver.adaptTo(Session.class);
        this.testRoot = getTestRootNode();
    }

    @After
    public void afterSuite() throws Exception {
    	if (session.isLive()) {
            if (testRoot != null) {
                testRoot.remove();
            }
            session.save();
            adminResolver.close();
    	}

    }

    @Test
    public void writeNode() throws Exception {
        testRoot.addNode("test");
        session.save();
    }

    @Test
    public void readNode() throws Exception {
        Node rootNode = session.getRootNode();
        rootNode.getNode(testRoot.getName());
        session.save();
    }

    @PerformanceTest(runinvocations=10, warmuptime=0)
    public void readPerf() throws Exception {

        Node rootNode = session.getRootNode();

        NodeIterator ni = rootNode.getNodes();
        while(ni.hasNext()) {
            Node node = (Node)ni.next();
            rootNode.getNode(node.getName());
        }
        session.save();
    }

    protected Node getTestRootNode() throws RepositoryException, NamingException {
        if(testRoot==null) {
            final Node root = session.getRootNode();
            testRoot = root.addNode(getClass().getSimpleName() + "_" + System.currentTimeMillis());
        }
        return testRoot;
    }


}
