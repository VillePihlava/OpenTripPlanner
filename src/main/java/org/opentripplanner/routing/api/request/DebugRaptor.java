package org.opentripplanner.routing.api.request;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.opentripplanner.model.base.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this class to configure Raptor Event Debugging. There are two ways to debug:
 * <ol>
 *     <li>
 *         Add a list of stops, and Raptor will print all events for these stops. This can be
 *         a bit overwhelming so using the path option might be a better option.
 *     </li>
 *     <li>
 *         Add a path (also a list of stops), and Raptor will print all events on the path.
 *         So if you arrive at a stop listed, but have not followed the exact same path of
 *         stops, then the event is NOT listed. Note! there are events for dropping an accepted
 *         path. If an none matching path dominate a matching path, then both paths are logged
 *         as part of the event.
 *         <p>
 *         You may also specify the first stop in the path to start logging events for. For example
 *         given the path {@code [1010, 1183, 3211, 492]}, then you may know, that you can get to
 *         stop 2 without any problems. So, to avoid getting spammed by logging events at the first
 *         two stops, you set the stop index 3211 as the first stop to print events for. This is
 *         done by adding an {@code *} to the stop: {@code [1010,1183,3211*,492]}.
 *     </li>
 *     To list stops you need to know the Raptor stop index. Enable log {@code level="debug"} for
 *     the {@code org.opentripplanner.transit.raptor} package to list all paths found by Raptor.
 *     The paths will contain the stop index. For paths not listed you will have to do some
 *     research.
 * </ol>
 */
public class DebugRaptor {

    private static final Logger LOG = LoggerFactory.getLogger(DebugRaptor.class);
    private static final Pattern FIRST_STOP_PATTERN = Pattern.compile("(\\d+)\\*");
    private static final int FIRST_STOP_INDEX = 0;

    private List<Integer> stops = List.of();
    private List<Integer> path = List.of();
    private int debugPathFromStopIndex = 0;

    public List<Integer> stops() {
        return stops;
    }

    public DebugRaptor withStops(String stops) {
        this.stops = split(stops);
        return this;
    }

    public List<Integer> path() {
        return path;
    }

    public DebugRaptor withPath(String path) {
        this.path = split(path);
        this.debugPathFromStopIndex = firstStopIndexToDebug(this.path, path);
        return this;
    }

    public int debugPathFromStopIndex() {
        return debugPathFromStopIndex;
    }

    private static List<Integer> split(String stops) {
        try {
            if(stops == null) { return List.of(); }

            return Arrays.stream(stops.split("[\\s,;_*]+"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toUnmodifiableList());
        }
        catch (NumberFormatException e) {
            LOG.error(e.getMessage(), e);
            // Keep going, we do not want to abort a
            // request because the debug info is wrong.
            return List.of();
        }
    }

    private static int firstStopIndexToDebug(List<Integer> stops, String text) {
        if(text == null) { return FIRST_STOP_INDEX; }

        var m = FIRST_STOP_PATTERN.matcher(text);
        Integer stop = m.find() ? Integer.parseInt(m.group(1)) : null;
        return stop == null ? FIRST_STOP_INDEX : stops.indexOf(stop);
    }

    @Override
    public String toString() {
        return ToStringBuilder.of(DebugRaptor.class)
                .addObj("stops", toString(stops, FIRST_STOP_INDEX))
                .addObj("path", toString(path, debugPathFromStopIndex))
                .toString();
    }

    private static String toString(List<Integer> stops, int fromStopIndex) {
        if(stops == null || stops.isEmpty()) { return null; }

        var buf = new StringBuilder();
        for (int i=0; i<stops.size(); ++i) {
            buf.append(stops.get(i));
            if(i > FIRST_STOP_INDEX && i == fromStopIndex) {
                buf.append("*");
            }
            buf.append(", ");
        }
        return buf.substring(0, buf.length()-2);
    }
}
