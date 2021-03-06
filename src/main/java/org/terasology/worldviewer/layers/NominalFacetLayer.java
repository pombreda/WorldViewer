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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.rendering.nui.Color;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldFacet;
import org.terasology.world.generation.facets.base.ObjectFacet2D;

import com.google.common.base.Stopwatch;

/**
 * Provides info about an {@link ObjectFacet2D}.
 * @param <E> the object type
 * @author Martin Steiger
 */
public class NominalFacetLayer<E> extends AbstractFacetLayer {

    private static final Logger logger = LoggerFactory.getLogger(NominalFacetLayer.class);

    private final Function<? super E, Color> colorMap;
    private final Class<? extends ObjectFacet2D<E>> facetClass;

    public NominalFacetLayer(Class<? extends ObjectFacet2D<E>> clazz, Function<? super E, Color> colorMap) {
        this.colorMap = colorMap;
        this.facetClass = clazz;
    }

    @Override
    public void render(BufferedImage img, Region region) {
        ObjectFacet2D<E> facet = region.getFacet(facetClass);

        Stopwatch sw = Stopwatch.createStarted();

        int width = img.getWidth();
        int height = img.getHeight();

        DataBufferInt dataBuffer = (DataBufferInt) img.getRaster().getDataBuffer();

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                Color src = getColor(facet, x, z);
                int mix = src.rgba() >> 8;
                dataBuffer.setElem(z * width + x, mix);
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Rendered regions in {}ms.", sw.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private Color getColor(ObjectFacet2D<E> facet, int x, int z) {
        E val = facet.get(x, z);
        if (val == null)
            return Color.MAGENTA;

        return colorMap.apply(val);
    }

    @Override
    public String getWorldText(Region region, int wx, int wy) {
        ObjectFacet2D<E> facet = region.getFacet(facetClass);
        E val = facet.getWorld(wx, wy);
        if (val == null) {
            return "<missing>";
        }
        return val.toString();
    }

    @Override
    public Class<? extends WorldFacet> getFacetClass() {
        return facetClass;
    }
}
