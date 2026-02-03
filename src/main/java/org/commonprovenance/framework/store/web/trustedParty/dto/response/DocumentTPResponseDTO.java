package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dto.HasFormat;
import org.commonprovenance.framework.store.common.dto.HasId;

public class DocumentTPResponseDTO implements HasId, HasFormat {
  private final String id;
  private final String graph;
  private final String format;

  public DocumentTPResponseDTO(String id, String graph, String format) {
    this.id = id;
    this.graph = graph;
    this.format = format;
  }

  public String getId() {
    return id;
  }

  public String getGraph() {
    return graph;
  }

  public String getFormat() {
    return format;
  }

}
