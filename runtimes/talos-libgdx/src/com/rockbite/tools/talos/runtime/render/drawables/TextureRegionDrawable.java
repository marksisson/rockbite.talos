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

package com.rockbite.tools.talos.runtime.render.drawables;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rockbite.tools.talos.runtime.ParticleDrawable;

public class TextureRegionDrawable implements ParticleDrawable {

    private TextureRegion region;

    public TextureRegionDrawable(TextureRegion region) {
        this.region = region;
    }

    public void setRegion(TextureRegion region) {
        this.region = region;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height, float rotation) {
        batch.draw(region, x - width/2f, y - height/2f, width/2f, height/2f, width, height,1f,1f, rotation);
    }

    @Override
    public float getAspectRatio() {
        if(region == null) {
            return 1;
        }
        return region.getRegionWidth()/ (float)region.getRegionHeight();
    }

    @Override
    public void setSeed(float seed) {

    }
}
