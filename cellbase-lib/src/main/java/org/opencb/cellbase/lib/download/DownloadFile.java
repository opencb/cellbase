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

public class DownloadFile {
    private String startTime;
    private String elapsedTime;
    private Status status;
    private String message;
    private int expectedFileSize;
    private int actualFileSize;
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

    public String getStartTime() {
        return startTime;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public DownloadFile setElapsedTime(String elapsedTime) {
        this.elapsedTime = elapsedTime;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public DownloadFile setStatus(Status status) {
        this.status = status;
        return this;
    }

    public int getExpectedFileSize() {
        return expectedFileSize;
    }

    public DownloadFile setExpectedFileSize(int expectedFileSize) {
        this.expectedFileSize = expectedFileSize;
        return this;
    }

    public int getActualFileSize() {
        return actualFileSize;
    }

    public DownloadFile setActualFileSize(int actualFileSize) {
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
}
