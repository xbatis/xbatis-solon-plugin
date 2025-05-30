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

import cn.xbatis.core.mybatis.configuration.MybatisConfiguration;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.solon.integration.MybatisAdapterDefault;
import org.noear.solon.Utils;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Props;

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

    @Override
    protected void initConfiguration(Environment environment) {
        MybatisConfiguration configuration = new MybatisConfiguration(environment);
        this.config = configuration;
        Props cfgProps = this.dsProps.getProp("configuration");
        if (cfgProps.size() > 0) {
            Utils.injectProperties(this.config, cfgProps);
        }
        configuration.onInit();
    }
}
