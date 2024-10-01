/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib.download;

import java.util.concurrent.TimeUnit;

public class DownloadFile {

    private String startTime;
    private String elapsedTime;
    private Status status;
    private String message;
    private long expectedFileSize;
    private long actualFileSize;
    private String outputFile;
    private String url;

    enum Status {
        OK, WARNING, ERROR
    };

    public DownloadFile(String url, String outputFile, String startTime) {
        this.url = url;
        this.outputFile = outputFile;
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "DownloadFile{"
                + "startTime='" + startTime + '\''
                + ", elapsedTime='" + elapsedTime + '\''
                + ", status=" + status
                + ", message='" + message + '\''
                + ", expectedFileSize=" + expectedFileSize
                + ", actualFileSize=" + actualFileSize
                + ", outputFile='" + outputFile + '\''
                + ", url='" + url + '\''
                + '}';
    }

    public String getStartTime() {
        return startTime;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public DownloadFile setElapsedTime(Long startTime, Long endTime) {
        long elapsedTime = endTime - startTime;
        this.elapsedTime = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) + " seconds";
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public DownloadFile setStatus(Status status) {
        this.status = status;
        return this;
    }

    public long getExpectedFileSize() {
        return expectedFileSize;
    }

    public DownloadFile setExpectedFileSize(long expectedFileSize) {
        this.expectedFileSize = expectedFileSize;
        return this;
    }

    public long getActualFileSize() {
        return actualFileSize;
    }

    public DownloadFile setActualFileSize(long actualFileSize) {
        this.actualFileSize = actualFileSize;
        return this;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getUrl() {
        return url;
    }

    public String getMessage() {
        return message;
    }

    public DownloadFile setMessage(String message) {
        this.message = message;
        return this;
    }

}
