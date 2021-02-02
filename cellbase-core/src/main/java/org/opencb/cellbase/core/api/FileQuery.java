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

package org.opencb.cellbase.core.api;

import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.api.query.AbstractQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.api.query.QueryParameter;

import java.util.List;
import java.util.Map;

public class FileQuery extends AbstractQuery {

    @QueryParameter(id = "filePath")
    private String filePath;
    @QueryParameter(id = "fileType")
    private String fileType;
    @QueryParameter(id = "region")
    private List<Region> regions;

    public FileQuery() {
    }

    public FileQuery(Map<String, String> params) throws QueryException {
        super(params);
    }

    @Override
    protected void validateQuery() throws QueryException {
        // check that file is present?
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileQuery{");
        sb.append("filePath='").append(filePath).append('\'');
        sb.append(", fileType='").append(fileType).append('\'');
        sb.append(", regions='").append(regions).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getFilePath() {
        return filePath;
    }

    public FileQuery setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public String getFileType() {
        return fileType;
    }

    public FileQuery setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public FileQuery setRegions(List<Region> regions) {
        this.regions = regions;
        return this;
    }


    public static final class FileQueryBuilder {
        private String filePath;
        private String fileType;
        private List<Region> regions;

        private FileQueryBuilder() {
        }

        public static FileQueryBuilder aFileQuery() {
            return new FileQueryBuilder();
        }

        public FileQueryBuilder withFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public FileQueryBuilder withFileType(String fileType) {
            this.fileType = fileType;
            return this;
        }

        public FileQueryBuilder withRegions(List<Region> regions) {
            this.regions = regions;
            return this;
        }

        public FileQuery build() {
            FileQuery fileQuery = new FileQuery();
            fileQuery.setFilePath(filePath);
            fileQuery.setFileType(fileType);
            fileQuery.setRegions(regions);
            return fileQuery;
        }
    }
}
