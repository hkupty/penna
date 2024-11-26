package penna.core.slf4j;

import org.slf4j.spi.MDCAdapter;
import penna.core.slf4j.mdcv2.PennaMDCImpl.Control;

import java.util.Deque;
import java.util.Map;
import java.util.function.BiConsumer;

public class PennaMDCAdapter implements MDCAdapter {
    @Override
    public void put(String s, String s1) {
        Control.mdcStorage.get().put(s, s1);
    }

    @Override
    public String get(String s) {
        return Control.mdcStorage.get().get(s);
    }

    @Override
    public void remove(String s) {
        Control.mdcStorage.get().remove(s);
    }

    @Override
    public void clear() {
        Control.mdcStorage.get().clear();
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        return Control.mdcStorage.get().getCopyOfContextMap();
    }

    @Override
    public void setContextMap(Map<String, String> map) {
       Control.mdcStorage.get().setContextMap(map);
    }

    @Override
    public void pushByKey(String s, String s1) {

    }

    @Override
    public String popByKey(String s) {
        return "";
    }

    @Override
    public Deque<String> getCopyOfDequeByKey(String s) {
        return null;
    }

    @Override
    public void clearDequeByKey(String s) {

    }

    public boolean isNotEmpty() {
        return Control.mdcStorage.get() != Control.empty;
    }

    public void forEach(BiConsumer<String, String> action) {
        Control.mdcStorage.get().forEach(action);
    }

}
