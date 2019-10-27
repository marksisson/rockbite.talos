/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.values.EmConfigValue;

public class EmConfigModule extends Module {

    public static final int OUTPUT = 0;

    private EmConfigValue userValue;
    private EmConfigValue outputValue;

    @Override
    public void init () {
        super.init();

        userValue = new EmConfigValue();

        userValue.attached = false;
        userValue.continuous = true;
        userValue.additive = true;
        userValue.aligned = false;
    }

    @Override
    protected void defineSlots() {
        outputValue = (EmConfigValue) createOutputSlot(OUTPUT, new EmConfigValue());
    }

    @Override
    public void processValues() {
        outputValue.set(userValue);
    }

    public EmConfigValue getUserValue() {
        return userValue;
    }


    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("additive", getUserValue().additive);
        json.writeValue("attached", getUserValue().attached);
        json.writeValue("continuous", getUserValue().continuous);
        json.writeValue("aligned", getUserValue().aligned);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        getUserValue().additive = jsonData.getBoolean("additive");
        getUserValue().attached = jsonData.getBoolean("attached");
        getUserValue().continuous = jsonData.getBoolean("continuous");
        getUserValue().aligned = jsonData.getBoolean("aligned");
    }
}
