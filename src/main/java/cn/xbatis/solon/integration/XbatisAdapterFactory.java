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

import org.apache.ibatis.solon.MybatisAdapter;
import org.apache.ibatis.solon.MybatisAdapterFactory;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Props;

/**
 * xbatis 适配器工厂。
 *
 * @author Ai东
 * @since 2.6.4
 */
public class XbatisAdapterFactory implements MybatisAdapterFactory {

    @Override
    public MybatisAdapter create(BeanWrap dsWrap) {
        return new XbatisAdapterDefault(dsWrap);
    }

    @Override
    public MybatisAdapter create(BeanWrap dsWrap, Props dsProps) {
        return new XbatisAdapterDefault(dsWrap, dsProps);
    }

}
