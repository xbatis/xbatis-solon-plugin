/*
 *  Copyright (c) 2024-2025, Ai东 (abc-127@live.cn) xbatis.
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
import cn.xbatis.core.mybatis.configuration.MybatisConfiguration;
import cn.xbatis.db.Model;
import cn.xbatis.db.annotations.ConditionTarget;
import cn.xbatis.db.annotations.OrderByTarget;
import cn.xbatis.db.annotations.ResultEntity;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.solon.integration.MybatisAdapterDefault;
import org.noear.solon.Utils;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Props;
import org.noear.solon.core.util.ResourceUtil;

import java.util.Arrays;
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

    protected XbatisAdapterDefault(BeanWrap dsWrap) {
        super(dsWrap);
    }

    protected XbatisAdapterDefault(BeanWrap dsWrap, Props dsProps) {
        super(dsWrap, dsProps);
    }

    private boolean hasCheckBasePackages = false;

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

        Consumer<Class> execution = getPojoCheckerFunction();

        this.executeCheckPackages(basePackages, modelPackages, execution);
        this.executeCheckPackages(basePackages, resultEntityPackages, execution);
        this.executeCheckPackages(basePackages, conditionTargetPackages, execution);
        this.executeCheckPackages(basePackages, orderByTargetPackages, execution);
    }

    protected Consumer<Class> getPojoCheckerFunction() {
        return clazz -> {
            if (Model.class.isAssignableFrom(clazz)) {
                Models.get(clazz);
            } else if (clazz.isAnnotationPresent(ResultEntity.class)) {
                ResultInfos.get(clazz);
            } else if (clazz.isAnnotationPresent(ConditionTarget.class)) {
                Conditions.get(clazz);
            } else if (clazz.isAnnotationPresent(OrderByTarget.class)) {
                OrderBys.get(clazz);
            }
        };
    }

    protected void executeCheckPackages(String basePackages, String targetPackages, Consumer<Class> execution) {
        String packages;
        if (targetPackages == null || targetPackages.isEmpty()) {
            packages = basePackages;
        } else if (hasCheckBasePackages) {
            return;
        } else {
            packages = targetPackages;
        }

        if (packages == null || packages.isEmpty()) {
            return;
        }
        Arrays.stream(basePackages.split(",")).forEach(basePackage -> {
            ResourceUtil.scanClasses(basePackage).stream().forEach(execution);
        });

        if (targetPackages == null || targetPackages.isEmpty()) {
            hasCheckBasePackages = true;
        }
    }

}
