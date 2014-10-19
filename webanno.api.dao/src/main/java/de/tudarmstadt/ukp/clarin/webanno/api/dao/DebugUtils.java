/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.clarin.webanno.api.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DebugUtils
{
    private static final Log LOG = LogFactory.getLog(DebugUtils.class);
    
    public static void smallStack()
    {
        smallStack(0);
    }
    
    public static void smallStack(int aLimit)
    {
        StringBuilder sb = new StringBuilder();
        Exception e = new RuntimeException();
        boolean reqNewLine = true;
        boolean firstSkipped = false;
        int count = 0;
        for (StackTraceElement f : e.getStackTrace()) {
            if (!firstSkipped) {
                firstSkipped = true;
                continue;
            }
            if (f.getClassName().startsWith("de.tudarmstadt")) {
                if (reqNewLine) {
                    sb.append("\n");
                }
                sb.append(f);
                count++;
            }
            else {
                sb.append(".");
                reqNewLine = true;
            }
            if (aLimit > 0 && count >= aLimit) {
                break;
            }
        }
        LOG.debug(sb);
    }
}
