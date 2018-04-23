/*
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

package com.amazonaws.QueryEventListener;

import com.facebook.presto.spi.eventlistener.EventListener;
import com.facebook.presto.spi.eventlistener.QueryCompletedEvent;
import com.facebook.presto.spi.eventlistener.QueryCreatedEvent;
import com.facebook.presto.spi.eventlistener.SplitCompletedEvent;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class QueryEventListener
        implements EventListener
{
    Logger logger;
    FileHandler fh;
    final String loggerName = "QueryLog";

    public QueryEventListener()
    {
        createLogFile(new HashMap<String, String>());
    }

    public QueryEventListener(Map<String, String> config)
    {
        createLogFile(config);
    }

    public void queryCreated(QueryCreatedEvent queryCreatedEvent)
    {
        StringBuilder msg = new StringBuilder();

        try {
            msg.append("---------------Query Created----------------------------");
            msg.append("\n");
            msg.append("     ");
            msg.append("Query ID: ");
            msg.append(queryCreatedEvent.getMetadata().getQueryId().toString());
            msg.append("\n");
            msg.append("     ");
            msg.append("Query Text: ");
            msg.append(queryCreatedEvent.getMetadata().getQuery().toString());
            msg.append("\n");
            msg.append("     ");
            msg.append("Query State: ");
            msg.append(queryCreatedEvent.getMetadata().getQueryState().toString());
            msg.append("\n");
            msg.append("     ");
            msg.append("User: ");
            msg.append(queryCreatedEvent.getContext().getUser().toString());
            msg.append("\n");
            msg.append("     ");
            msg.append("Create Time: ");
            msg.append(queryCreatedEvent.getCreateTime());
            msg.append("\n");
            msg.append("     ");
            msg.append("Principal: ");
            msg.append(queryCreatedEvent.getContext().getPrincipal());
            msg.append("\n");
            msg.append("     ");
            msg.append("Remote Client Address: ");
            msg.append(queryCreatedEvent.getContext().getRemoteClientAddress());
            msg.append("\n");
            msg.append("     ");
            msg.append("Source: ");
            msg.append(queryCreatedEvent.getContext().getSource());
            msg.append("\n");
            msg.append("     ");
            msg.append("User Agent: ");
            msg.append(queryCreatedEvent.getContext().getUserAgent());
            msg.append("\n");
            msg.append("     ");
            msg.append("Catalog: ");
            msg.append(queryCreatedEvent.getContext().getCatalog());
            msg.append("\n");
            msg.append("     ");
            msg.append("Schema: ");
            msg.append(queryCreatedEvent.getContext().getSchema());
            msg.append("\n");
            msg.append("     ");
            msg.append("Server Address: ");
            msg.append(queryCreatedEvent.getContext().getServerAddress());

            logger.info(msg.toString());
        }
        catch (Exception ex) {
            logger.info(ex.getMessage());
        }
    }

    public void queryCompleted(QueryCompletedEvent queryCompletedEvent)
    {
        String errorCode = null;
        StringBuilder msg = new StringBuilder();

        try {
            errorCode = queryCompletedEvent.getFailureInfo().get().getErrorCode().getName().toString();
        }
        catch (NoSuchElementException noElEx) {
            errorCode = null;
        }

        try {
            if (errorCode != null) {
                msg.append("---------------Query Completed----------------------------");
                msg.append("\n");
                msg.append("     ");
                msg.append("Query ID: ");
                msg.append(queryCompletedEvent.getMetadata().getQueryId().toString());
                msg.append("\n");
                msg.append("     ");
                msg.append("Query Text: ");
                msg.append(queryCompletedEvent.getMetadata().getQuery().toString());
                msg.append("\n");
                msg.append("     ");
                msg.append("Create Time: ");
                msg.append(queryCompletedEvent.getCreateTime());
                msg.append("\n");
                msg.append("     ");
                msg.append("User: ");
                msg.append(queryCompletedEvent.getContext().getUser().toString());
                msg.append("\n");
                msg.append("     ");
                msg.append("Complete: ");
                msg.append(queryCompletedEvent.getStatistics().isComplete());
                msg.append("\n");
                msg.append("     ");
                msg.append("Query Failure Error: ");
                msg.append(errorCode);
                msg.append("\n");
                msg.append("     ");
                msg.append("Remote Client Address: ");
                msg.append(queryCompletedEvent.getContext().getRemoteClientAddress().toString());

                logger.info(msg.toString());
            }
            else {
                msg.append("---------------Query Completed----------------------------");
                msg.append("\n");
                msg.append("     ");
                msg.append("Query ID: ");
                msg.append(queryCompletedEvent.getMetadata().getQueryId().toString());
                msg.append("\n");
                msg.append("     ");
                msg.append("Query Text: ");
                msg.append(queryCompletedEvent.getMetadata().getQuery().toString());
                msg.append("\n");
                msg.append("     ");
                msg.append("Create Time: ");
                msg.append(queryCompletedEvent.getCreateTime());
                msg.append("\n");
                msg.append("     ");
                msg.append("User: ");
                msg.append(queryCompletedEvent.getContext().getUser().toString());
                msg.append("\n");
                msg.append("     ");
                msg.append("Complete: ");
                msg.append(queryCompletedEvent.getStatistics().isComplete());
                msg.append("\n");
                msg.append("     ");
                msg.append("Remote Client Address: ");
                msg.append(queryCompletedEvent.getContext().getRemoteClientAddress().toString());

                logger.info(msg.toString());
            }
        }
        catch (Exception ex) {
            logger.info(ex.getMessage());
        }
    }

    public void splitCompleted(SplitCompletedEvent splitCompletedEvent)
    {
        StringBuilder msg = new StringBuilder();

        try {
            msg.append("---------------Split Completed----------------------------");
            msg.append("\n");
            msg.append("     ");
            msg.append("Query ID: ");
            msg.append(splitCompletedEvent.getQueryId().toString());
            msg.append("\n");
            msg.append("     ");
            msg.append("Stage ID: ");
            msg.append(splitCompletedEvent.getStageId().toString());
            msg.append("\n");
            msg.append("     ");
            msg.append("Task ID: ");
            msg.append(splitCompletedEvent.getTaskId().toString());

            logger.info(msg.toString());
        }
        catch (Exception ex) {
            logger.info(ex.getMessage());
        }
    }

    public void createLogFile(Map<String, String> config)
    {
        SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        StringBuilder logPath = new StringBuilder();

        String logFile = config.getOrDefault("log-file", "/var/log/presto/queries-%g.log");
        int maxSize;
        try {
            String maxSizeString = config.getOrDefault("max-size", "524288000");
            maxSize = Integer.parseInt(maxSizeString);
        }
        catch (NumberFormatException e) {
            maxSize = 524288000;
        }

        int maxCounts;
        try {
            String maxCountsString = config.getOrDefault("max-counts", "10");
            maxCounts = Integer.parseInt(maxCountsString);
        }
        catch (NumberFormatException e) {
            maxCounts = 10;
        }
        try {
            logger = Logger.getLogger(loggerName);
            fh = new FileHandler(logFile, maxSize, maxCounts, true);
            logger.addHandler(fh);
            logger.setUseParentHandlers(false);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        }
        catch (IOException e) {
            logger.info(e.getMessage());
        }
    }
}
