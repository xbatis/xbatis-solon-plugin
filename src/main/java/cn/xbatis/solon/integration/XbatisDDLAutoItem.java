/*
 *  Copyright (c) 2024-2026, Ai东 (abc-127@live.cn) xbatis.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License").
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 */

package cn.xbatis.solon.integration;

import cn.xbatis.ddl.auto.Mode;

public class XbatisDDLAutoItem {

    private String dbType;

    private String entityPackages;

    private Mode mode;

    private String dataSource;

    private Class<?> makerInterface;

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getEntityPackages() {
        return entityPackages;
    }

    public void setEntityPackages(String entityPackages) {
        this.entityPackages = entityPackages;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public Class<?> getMakerInterface() {
        return makerInterface;
    }

    public void setMakerInterface(Class<?> makerInterface) {
        this.makerInterface = makerInterface;
    }
}
