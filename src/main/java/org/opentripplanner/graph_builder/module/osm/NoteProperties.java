package org.opentripplanner.graph_builder.module.osm;

import java.util.Map;
import java.util.regex.Pattern;
import org.opentripplanner.graph_builder.module.osm.tagmapping.DefaultMapper;
import org.opentripplanner.model.NoteMatcher;
import org.opentripplanner.model.StreetNote;
import org.opentripplanner.openstreetmap.model.OSMWithTags;
import org.opentripplanner.transit.model.basic.I18NString;
import org.opentripplanner.transit.model.basic.TranslatedString;

/**
 * Currently unused since notes are disabled in {@link DefaultMapper}
 */
public class NoteProperties {

  private static final Pattern patternMatcher = Pattern.compile("\\{(.*?)}");

  public String notePattern;

  public NoteMatcher noteMatcher;

  public NoteProperties(String notePattern, NoteMatcher noteMatcher) {
    this.notePattern = notePattern;
    this.noteMatcher = noteMatcher;
  }

  public StreetNoteAndNoteMatcher generateNote(OSMWithTags way) {
    I18NString text;
    //TODO: this could probably be made without patternMatch for {} since all notes (at least currently) have {note} as notePattern
    if (patternMatcher.matcher(notePattern).matches()) {
      //This gets language -> translation of notePattern and all tags (which can have translations name:en for example)
      Map<String, String> noteText = TemplateLibrary.generateI18N(notePattern, way);
      text = TranslatedString.getI18NString(noteText, true, false);
    } else {
      text = LocalizedStringMapper.getInstance().map(notePattern, way);
    }
    StreetNote note = new StreetNote(text);

    return new StreetNoteAndNoteMatcher(note, noteMatcher);
  }
}
