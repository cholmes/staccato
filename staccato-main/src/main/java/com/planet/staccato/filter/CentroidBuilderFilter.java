package com.planet.staccato.filter;

import com.planet.staccato.model.Centroid;
import com.planet.staccato.model.Item;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.io.GeoJSONReader;
import org.locationtech.spatial4j.shape.Shape;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Automatically generates a centroid for any item being inserted/indexed in STAC.  This is an unfortunate necessity
 * to support geo aggregations in Elasticsearch.
 *
 * @author joshfix
 * Created on 1/30/18
 */
@Slf4j
@Component
public class CentroidBuilderFilter implements ItemIndexFilter {

    private final static Set<String> types = new HashSet<>(Arrays.asList("*"));
    private final GeoJSONReader geoJsonReader = new GeoJSONReader(JtsSpatialContext.GEO, new JtsSpatialContextFactory());

    @Autowired
    private ObjectMapper mapper;

    @Override
    public Set<String> types() {
        return types;
    }

    @Override
    public Item doFilter(Item item) {
        return addCentroid(item);
    }

    private Item addCentroid(Item item) {
        if (item.getGeometry() != null) {
            try {
                Shape read = geoJsonReader.read(mapper.writeValueAsString(item.getGeometry()));
                Centroid centroid = new Centroid().lat(read.getCenter().getY()).lon(read.getCenter().getX());
                item.setCentroid(centroid);
            } catch (Exception ex) {
                log.warn("Error reading geojson: {}", ex.getMessage());
            }
        }
        return item;
    }

}
