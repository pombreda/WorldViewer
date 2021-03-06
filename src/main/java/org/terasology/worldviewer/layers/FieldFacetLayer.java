/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.worldviewer.layers;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.TeraMath;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldFacet;
import org.terasology.world.generation.facets.base.FieldFacet2D;
import org.terasology.worldviewer.config.FacetConfig;

import com.google.common.base.Stopwatch;
import com.google.common.math.DoubleMath;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class FieldFacetLayer extends AbstractFacetLayer {

    private static final List<Color> GRAYS = IntStream
            .range(0, 256)
            .mapToObj(i -> new Color(i, i, i))
            .collect(Collectors.toList());

    private static final Color MISSING = Color.MAGENTA;

    private static final Logger logger = LoggerFactory.getLogger(FieldFacetLayer.class);

    private Config config = new Config();

    /**
     * This can be called only through reflection since Config is private
     * @param config the layer configuration info
     */
    public FieldFacetLayer(Config config) {
        this.config = config;
    }

    public FieldFacetLayer(Class<? extends FieldFacet2D> clazz, double offset, double scale) {
        this.config.clazz = clazz;
        this.config.offset = offset;
        this.config.scale = scale;
    }

    @Override
    public String getWorldText(Region region, int wx, int wy) {
        FieldFacet2D facet = region.getFacet(config.clazz);
        double value = facet.getWorld(wx, wy);
        return String.format("%.2f", value);
    }

    @Override
    public void render(BufferedImage img, Region region) {
        FieldFacet2D facet = region.getFacet(config.clazz);

        Stopwatch sw = Stopwatch.createStarted();

        int width = img.getWidth();
        int height = img.getHeight();

        DataBufferInt dataBuffer = (DataBufferInt) img.getRaster().getDataBuffer();

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                Color col = getColor(facet, x, z);
                int src = col.rgba() >> 8; // we ignore alpha  | (col.a() << 24);
                int dst = dataBuffer.getElem(z * width + x);
                int mix = 0xFF000000;
                mix |= Math.min(0x0000FF, (dst & 0x0000FF) + (src & 0x0000FF));
                mix |= Math.min(0x00FF00, (dst & 0x00FF00) + (src & 0x00FF00));
                mix |= Math.min(0xFF0000, (dst & 0xFF0000) + (src & 0xFF0000));
                dataBuffer.setElem(z * width + x, mix);
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Rendered regions in {}ms.", sw.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private Color getColor(FieldFacet2D facet, int x, int z) {
        double value = facet.get(x, z);
        if (Double.isFinite(value)) {
            int round = DoubleMath.roundToInt(config.offset + config.scale * value, RoundingMode.HALF_UP);
            int idx = TeraMath.clamp(round, 0, 255);
            return GRAYS.get(idx);
        } else {
            return MISSING;
        }
    }

    @Override
    public Class<? extends WorldFacet> getFacetClass() {
        return config.clazz;
    }

    public double getOffset() {
        return config.offset;
    }

    public double getScale() {
        return config.scale;
    }

    /**
     * @param scale the new scale factor
     */
    public void setScale(double scale) {
        if (scale != config.scale) {
            config.scale = scale;
            notifyObservers();
        }
    }

    /**
     * @param offset the new offset
     */
    public void setOffset(double offset) {
        if (offset != config.offset) {
            config.offset = offset;
            notifyObservers();
        }
    }

    @Override
    public FacetConfig getConfig() {
        return config;
    }

    /**
     * Persistent data
     */
    private static class Config implements FacetConfig {
        private Class<? extends FieldFacet2D> clazz;

        @Range(min = -100, max = 100, increment = 1f, precision = 1)
        private double offset;

        @Range(min = 0, max = 100, increment = 0.1f, precision = 1)
        private double scale;
    }
}
