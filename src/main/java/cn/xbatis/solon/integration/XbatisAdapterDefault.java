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

import cn.xbatis.core.db.reflect.Conditions;
import cn.xbatis.core.db.reflect.Models;
import cn.xbatis.core.db.reflect.OrderBys;
import cn.xbatis.core.db.reflect.ResultInfos;
import cn.xbatis.core.dbType.DbTypeUtil;
import cn.xbatis.core.mybatis.configuration.MybatisConfiguration;
import cn.xbatis.db.Model;
import cn.xbatis.db.annotations.ConditionTarget;
import cn.xbatis.db.annotations.OrderByTarget;
import cn.xbatis.db.annotations.ResultEntity;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.ddl.auto.DDLAuto;
import cn.xbatis.ddl.auto.Mode;
import db.sql.api.DbTypes;
import db.sql.api.IDbType;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.solon.integration.MybatisAdapterDefault;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Props;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.util.ClassUtil;
import org.noear.solon.data.datasource.AbstractRoutingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 基于 mybatis solon 插件 修改
 * 替换 mybatis Configuration 配置
 *
 * @author Ai东
 * @since 2.6.4
 */
public class XbatisAdapterDefault extends MybatisAdapterDefault {
    private static final Logger logger = LoggerFactory.getLogger(XbatisAdapterDefault.class);

    protected XbatisAdapterDefault(BeanWrap dsWrap) {
        super(dsWrap);
    }

    protected XbatisAdapterDefault(BeanWrap dsWrap, Props dsProps) {
        super(dsWrap, dsProps);
    }

    @Override
    protected void initConfiguration(Environment environment) {
        MybatisConfiguration configuration = new MybatisConfiguration(environment);
        this.config = configuration;
        Props cfgProps = this.dsProps.getProp("configuration");
        if (cfgProps.size() > 0) {
            Utils.injectProperties(this.config, cfgProps);
        }
        configuration.onInit();
        this.checkPojo();
        this.autoDDL();
    }

    protected void autoDDL() {
        Props ddlAutosProps = this.dsProps.getProp("ddlAuto");
        if (ddlAutosProps.size() < 1) {
            return;
        }

        List<XbatisDDLAutoItem> ddlAutos = PropsUtil.resolve(ddlAutosProps, XbatisDDLAutoItem.class);

        DataSource primary = getDataSource();
        for (XbatisDDLAutoItem item : ddlAutos) {
            if (item.getEntityPackages() == null || item.getEntityPackages().isEmpty()) {
                throw new RuntimeException("mybatis." + dsWrap.name() + ".ddlAuto.entityPackages 不能缺省");
            }
            if (item.getMode() == Mode.NONE) {
                continue;
            }
            if (item.getMode() == null) {
                item.setMode(Mode.CREATE);
            }
            DataSource ds;
            if (item.getDataSource() != null && !item.getDataSource().isEmpty()) {
                Map<String, DataSource> dataSourceMap = Solon.context().getBeansMapOfType(DataSource.class);
                ds = dataSourceMap.get(item.getDataSource());
                if (ds == null) {
                    if (ds instanceof AbstractRoutingDataSource) {
                        try {
                            Map<String, DataSource> targetDataSources = (Map<String, DataSource>) AbstractRoutingDataSource.class.getDeclaredField("targetDataSources").get(ds);
                            ds = targetDataSources.get(item.getDataSource());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if (ds == null) {
                    throw new RuntimeException("找不到 dataSource beanName:" + item.getDataSource());
                }
            } else {
                ds = primary;
                if (ds == null) {
                    throw new RuntimeException("找不到可用的dataSource bean");
                }
            }

            String entityPackages = item.getEntityPackages() == null ? "" : item.getEntityPackages();
            final List<Class<?>> entities = new ArrayList<>();
            Arrays.stream(entityPackages.split(",")).forEach(basePackage -> {
                ClassUtil.scanClasses(basePackage, i -> {
                    if (item.getMakerInterface() != null && !item.getMakerInterface().isAssignableFrom(i)) {
                        return;
                    }
                    if (!i.isAnnotationPresent(Table.class)) {
                        return;
                    }
                    entities.add(i);
                });
            });

            logger.info("扫描到{}个需要DDL的实体类 扫描目录：{}", entities.size(), entityPackages);

            IDbType dbType = item.getDbType() == null || item.getDbType().isEmpty() ? DbTypeUtil.getDbType(ds) : DbTypes.getByName(item.getDbType());

            DDLAuto.of(dbType)
                    .add(entities)
                    .execute(ds);
        }
        logger.info("Xbatis DDL Auto 全部完成，发送 XbatisDDLAutoCompleteEvent 事件");
        EventBus.publish(new XbatisDDLAutoCompleteEvent());
    }

    protected void checkPojo() {
        Map<String, String> checkMap = this.dsProps.getMap("pojoCheck");
        if (checkMap == null || checkMap.isEmpty()) {
            return;
        }

        String basePackages = checkMap.get("basePackages");
        String modelPackages = checkMap.get("modelPackages");
        String resultEntityPackages = checkMap.get("resultEntityPackages");
        String conditionTargetPackages = checkMap.get("conditionTargetPackages");
        String orderByTargetPackages = checkMap.get("orderByTargetPackages");

        PojoCheckInfo pojoCheckInfo = new PojoCheckInfo();
        pojoCheckInfo.setCheckModel(modelPackages != null && !modelPackages.isEmpty());
        pojoCheckInfo.setCheckResultEntity(resultEntityPackages != null && !resultEntityPackages.isEmpty());
        pojoCheckInfo.setCheckConditionTarget(conditionTargetPackages != null && !conditionTargetPackages.isEmpty());
        pojoCheckInfo.setCheckOrderTarget(orderByTargetPackages != null && !orderByTargetPackages.isEmpty());

        //假如单独配置了 则使用单独配置的
        if (pojoCheckInfo.isCheckModel()) {
            PojoCheckInfo checkInfo = new PojoCheckInfo();
            checkInfo.setCheckModel(true);
            this.executeCheckPackages(modelPackages, getPojoCheckerFunction(checkInfo));
        }
        if (pojoCheckInfo.isCheckResultEntity()) {
            PojoCheckInfo checkInfo = new PojoCheckInfo();
            checkInfo.setCheckResultEntity(true);
            this.executeCheckPackages(resultEntityPackages, getPojoCheckerFunction(checkInfo));
        }
        if (pojoCheckInfo.isCheckConditionTarget()) {
            PojoCheckInfo checkInfo = new PojoCheckInfo();
            checkInfo.setCheckConditionTarget(true);
            this.executeCheckPackages(conditionTargetPackages, getPojoCheckerFunction(checkInfo));
        }
        if (pojoCheckInfo.isCheckOrderTarget()) {
            PojoCheckInfo checkInfo = new PojoCheckInfo();
            checkInfo.setCheckOrderTarget(true);
            this.executeCheckPackages(orderByTargetPackages, getPojoCheckerFunction(checkInfo));
        }

        //假如其中一个没有配置
        if (!pojoCheckInfo.isCheckModel() || !pojoCheckInfo.isCheckResultEntity() || !pojoCheckInfo.isCheckConditionTarget() || !pojoCheckInfo.isCheckOrderTarget()) {
            if (basePackages == null || basePackages.isEmpty()) {
                return;
            }
            //以basePackages为准
            pojoCheckInfo.setCheckModel(!pojoCheckInfo.isCheckModel());
            pojoCheckInfo.setCheckResultEntity(!pojoCheckInfo.isCheckResultEntity());
            pojoCheckInfo.setCheckConditionTarget(!pojoCheckInfo.isCheckConditionTarget());
            pojoCheckInfo.setCheckOrderTarget(!pojoCheckInfo.isCheckOrderTarget());
            this.executeCheckPackages(basePackages, getPojoCheckerFunction(pojoCheckInfo));
        }
    }

    protected Consumer<Class<?>> getPojoCheckerFunction(PojoCheckInfo pojoCheckInfo) {
        return clazz -> {
            if (pojoCheckInfo.isCheckModel() && Model.class.isAssignableFrom(clazz)) {
                Models.get(clazz);
            } else if (pojoCheckInfo.isCheckResultEntity() && clazz.isAnnotationPresent(ResultEntity.class)) {
                ResultInfos.get(clazz);
            } else if (pojoCheckInfo.isCheckConditionTarget() && clazz.isAnnotationPresent(ConditionTarget.class)) {
                Conditions.get(clazz);
            } else if (pojoCheckInfo.isCheckOrderTarget() && clazz.isAnnotationPresent(OrderByTarget.class)) {
                OrderBys.get(clazz);
            }
        };
    }

    protected void executeCheckPackages(String targetPackages, Consumer<Class<?>> execution) {
        if (targetPackages == null || targetPackages.isEmpty()) {
            return;
        }
        Arrays.stream(targetPackages.split(",")).forEach(basePackage -> {
            ClassUtil.scanClasses(basePackage, execution);
        });
    }
}
