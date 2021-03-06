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
package org.apache.sling.replication.agent.impl;

import org.apache.sling.commons.osgi.PropertiesUtil;

import java.util.*;

public class SettingsUtils {

    private static final char COMPONENT_DELIM = '/';
    private static final char COMPONENT_MAP_BEGIN = '[';
    private static final char COMPONENT_MAP_END = ']';

    public static Map<String, Object> compactMap(Map<String, Object> valueMap) {

        Map<String, Object> result = new HashMap<String, Object>();

        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            int beginDelim = key.indexOf(COMPONENT_MAP_BEGIN);
            int endDelim = key.indexOf(COMPONENT_MAP_END);

            if (beginDelim >=0 && endDelim > beginDelim && value instanceof String) {
                String newKey = key.substring(0, beginDelim);
                String partialKey = key.substring(beginDelim+1, endDelim);

                String newValue = (String) value;

                try {
                    Integer.parseInt(partialKey);
                    // newKey[0] = newValue
                } catch(NumberFormatException e) {
                    // newKey[partialKey] = newValue
                    newValue = partialKey + "=" + newValue;
                }

                addValueInArray(result, newKey, newValue);
            }
            else {
                result.put(key, value);
            }
        }

        return result;

    }

    public static Map<String, Object> toMap(String[] lines) {
        Map<String, Object> result = new HashMap<String, Object>();

        Map<String, String> valueMap = PropertiesUtil.toMap(lines, new String[0]);

        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            int firstDelim = key.indexOf(COMPONENT_DELIM);
            if (firstDelim >= 0) {
                String newKey = key.substring(0, firstDelim);
                String newValue = key.substring(firstDelim+1) + "=" + value;

                addValueInArray(result, newKey, newValue);
            }
            else {
                result.put(key, value);
            }

        }

        return result;

    }

    public static void addValueInArray(Map<String, Object> map, String key, String value) {
        String[] arayValue;

        if (map.containsKey(key) && map.get(key) instanceof String[]) {
            String[] existingArray = (String[]) map.get(key);


            List<String> newList = new ArrayList<String>();
            Collections.addAll(newList, existingArray);
            newList.add(value);
            arayValue = newList.toArray(new String[newList.size()]);
        }
        else {
            arayValue = new String[] { value };
        }

        map.put(key, arayValue);

    }

    public static String[] toStringArray(Map<String, Object> objectMap) {

        List<String> result = new ArrayList<String>();

        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();


            if (value instanceof Map) {
                Map<String,Object> map = (Map<String, Object>) value;
                for (Map.Entry<String, Object> e : map.entrySet()) {
                    if (e.getValue() != null) {
                        result.add(key + "[" + e.getKey() + "]=" + e.getValue().toString());
                    }
                }

            }
            else if (value.getClass().isArray()) {
                Object[] array = (Object[]) value;
                for (int i=0; i < array.length; i++) {
                    if (array[i] != null) {
                        result.add(key + "[" + i + "]=" + array[i].toString());
                    }
                }

            }
            else {
                result.add(key + "=" + value.toString());
            }
        }

        return result.toArray(new String[result.size()]);
    }
}
