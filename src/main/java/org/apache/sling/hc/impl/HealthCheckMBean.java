/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.sling.hc.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.apache.sling.hc.api.Constants;
import org.apache.sling.hc.api.HealthCheck;
import org.apache.sling.hc.api.Result;
import org.apache.sling.hc.api.ResultLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A {@link DynamicMBean} used to execute a {@link HealthCheck} service */
public class HealthCheckMBean implements DynamicMBean, Serializable {

    private static final long serialVersionUID = -90745301105975287L;
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckMBean.class);
    private final String beanName;
    private final HealthCheck healthCheck;
    
    public static final String HC_OK_ATTRIBUTE_NAME = "ok";
    public static final String LOG_ATTRIBUTE_NAME = "log";
    
    private static CompositeType LOG_ROW_TYPE;
    private static TabularType LOG_TABLE_TYPE;
    
    public static final String INDEX_COLUMN = "index";
    public static final String LEVEL_COLUMN = "level";
    public static final String MESSAGE_COLUMN = "message";
    
    static {
        try {
            // Define the log row and table types
            LOG_ROW_TYPE = new CompositeType(
                    "LogLine",
                    "A line in the Rule log",
                    new String [] { INDEX_COLUMN, LEVEL_COLUMN, MESSAGE_COLUMN },
                    new String [] { "log line index", "log level", "log message"},
                    new OpenType[] { SimpleType.INTEGER, SimpleType.STRING, SimpleType.STRING }
                    );
            final String [] indexes = { INDEX_COLUMN };
            LOG_TABLE_TYPE = new TabularType("LogTable", "Rule log messages", LOG_ROW_TYPE, indexes);
        } catch(Exception ignore) {
            // row or table type will be null if this happens
        }
    }

    public HealthCheckMBean(HealthCheck hc) {
        String name = hc.getInfo() == null ? null : hc.getInfo().get(Constants.HC_NAME);
        if(name == null) {
            name = hc.toString();
        }
        beanName = name;
        healthCheck = hc;
    }
    
    @Override
    public Object getAttribute(String attribute)
            throws AttributeNotFoundException, MBeanException, ReflectionException {
        
        // TODO cache the result of execution for a few seconds?
        final ResultLog resultLog = new ResultLog(logger);
        final Result result = healthCheck.execute(resultLog);
        
        if(HC_OK_ATTRIBUTE_NAME.equals(attribute)) {
            return result.isOk();
        } else if(LOG_ATTRIBUTE_NAME.equals(attribute)) {
            return logData(result);
        } else {
            final Object o = healthCheck.getInfo().get(attribute);
            if(o == null) {
                throw new AttributeNotFoundException(attribute);
            }
            return o;
        }
    }
    
    private TabularData logData(Result er) {
        final TabularDataSupport result = new TabularDataSupport(LOG_TABLE_TYPE);
        int i=1;
        for(ResultLog.Entry e : er.getLogEntries()) {
            final Map<String, Object> data = new HashMap<String, Object>();
            data.put(INDEX_COLUMN, i++);
            data.put(LEVEL_COLUMN, e.getLevel().toString());
            data.put(MESSAGE_COLUMN, e.getMessage());
            try {
                result.put(new CompositeDataSupport(LOG_ROW_TYPE, data));
            } catch(OpenDataException ode) {
                throw new IllegalStateException("OpenDataException while creating log data", ode);
            }
        }
        return result;
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        final AttributeList result = new AttributeList();
        for(String key : attributes) {
            try {
                result.add(new Attribute(key, getAttribute(key)));
            } catch(Exception e) {
                logger.error("Exception getting Attribute " + key, e);
            }
        }
        return result;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        final MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[healthCheck.getInfo().size() + 2];
        int i=0;
        attrs[i++] = new MBeanAttributeInfo(HC_OK_ATTRIBUTE_NAME, Boolean.class.getName(), "The HealthCheck result", true, false, false);
        attrs[i++] = new OpenMBeanAttributeInfoSupport(LOG_ATTRIBUTE_NAME, "The rule log", LOG_TABLE_TYPE, true, false, false);
        
        for(String key : healthCheck.getInfo().keySet()) {
            attrs[i++] = new MBeanAttributeInfo(key, List.class.getName(), "Description of " + key, true, false, false);
        }
        
        return new MBeanInfo(this.getClass().getName(), beanName, attrs, null, null, null);
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support operations on Rules");
    }

    @Override
    public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support setting Rules attributes");
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support setting Rules attributes");
    }
    
    public String getName() {
        return beanName;
    }
}