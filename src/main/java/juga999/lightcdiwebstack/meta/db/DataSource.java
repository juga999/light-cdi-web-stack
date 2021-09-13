package juga999.lightcdiwebstack.meta.db;

import org.jdbi.v3.core.Handle;

import java.util.function.Consumer;
import java.util.function.Function;

public interface DataSource {

    int getDbVersion();

    <R> R withHandle(Function<Handle, R> fct);

    void useHandle(Consumer<Handle> fct);

    boolean isInTransaction();

    Handle beginTransaction();

    void commitTransaction();

    void rollbackTransaction();
}
