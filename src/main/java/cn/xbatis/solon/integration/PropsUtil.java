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

import org.noear.solon.Utils;
import org.noear.solon.core.Props;

import java.util.ArrayList;
import java.util.List;

public class PropsUtil {

    public static <T> List<T> resolve(Props root, Class<T> clazz) {
        List<T> arrays = new ArrayList<>();
        int index = 0;
        while (true) {
            Props props = root.getProp("[" + index++ + "]");
            if (props.size() == 0) {
                break;
            } else {
                try {
                    arrays.add(Utils.injectProperties(clazz.newInstance(), props));
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return arrays;
    }
}
