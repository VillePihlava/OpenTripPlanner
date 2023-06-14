package org.opentripplanner.graph_builder.module;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.opentripplanner.test.support.VariableSource;

class GtfsFeedIdTest {

  private static final String NUMBERS_ONLY_REGEX = "^\\d$";

  static Stream<Arguments> emptyCases = Stream.of(null, "", "     ", "\n", "  ").map(Arguments::of);

  @ParameterizedTest
  @VariableSource("emptyCases")
  void autogenerateNumber(String id) {
    assertTrue(feedId(id).matches(NUMBERS_ONLY_REGEX));
  }

  @Test
  void removeColon() {
    assertEquals(feedId("feed:id:"), "feedid");
  }

  @Test
  void keepUnderscore() {
    assertEquals(feedId("feed_id_"), "feed_id_");
  }

  @Nonnull
  private static String feedId(String input) {
    var id = new GtfsFeedId.Builder().id(input).build().getId();
    assertNotNull(id);
    return id;
  }
}
