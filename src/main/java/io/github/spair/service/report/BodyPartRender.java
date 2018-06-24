package io.github.spair.service.report;

public interface BodyPartRender<T> {

    String render(T status);
}
