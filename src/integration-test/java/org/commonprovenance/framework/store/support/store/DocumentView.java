package org.commonprovenance.framework.store.support.store;

import org.commonprovenance.framework.store.support.ProvGraph;

public record DocumentView(ProvGraph graph, String jwt) {
}
