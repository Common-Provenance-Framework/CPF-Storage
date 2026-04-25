package org.commonprovenance.framework.store.filter;

import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

/**
 * Propagates Reactor Context values into SLF4J MDC on each reactive signal so
 * that standard loggers pick up context-scoped fields (e.g. requestId) on
 * every thread the reactive pipeline uses.
 */
public class MdcContextLifter<T> implements CoreSubscriber<T> {

  static final String REQUEST_ID_KEY = "requestId";

  private final CoreSubscriber<T> delegate;

  public MdcContextLifter(CoreSubscriber<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Context currentContext() {
    return delegate.currentContext();
  }

  @Override
  public void onSubscribe(Subscription s) {
    copyToMdc(currentContext());
    delegate.onSubscribe(s);
  }

  @Override
  public void onNext(T t) {
    copyToMdc(currentContext());
    delegate.onNext(t);
  }

  @Override
  public void onError(Throwable t) {
    copyToMdc(currentContext());
    delegate.onError(t);
  }

  @Override
  public void onComplete() {
    copyToMdc(currentContext());
    delegate.onComplete();
  }

  private void copyToMdc(Context context) {
    context.getOrEmpty(REQUEST_ID_KEY)
        .ifPresentOrElse(
            value -> MDC.put(REQUEST_ID_KEY, value.toString()),
            () -> MDC.remove(REQUEST_ID_KEY));
  }
}
