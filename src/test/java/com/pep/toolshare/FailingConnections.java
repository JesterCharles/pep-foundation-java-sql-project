package com.pep.toolshare;

import com.pep.toolshare.db.ConnectionFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wraps a real database so that the Nth write ({@code executeUpdate}, {@code executeLargeUpdate} or {@code execute})
 * on any connection fails, as if the disk filled up in the middle of a transaction. Used to prove that a multi-statement change is rolled back as a unit.
 */
public final class FailingConnections {
    private static final Set<String> WRITES = Set.of("executeUpdate", "executeLargeUpdate", "execute");

    private FailingConnections() {
    }

    public static ConnectionFactory failingOnUpdate(ConnectionFactory real, int failingUpdate) {
        AtomicInteger updates = new AtomicInteger();
        return () -> {
            Connection connection = real.open();
            return (Connection) Proxy.newProxyInstance(FailingConnections.class.getClassLoader(),
                    new Class<?>[] {Connection.class}, (proxy, method, args) -> {
                        Object result = invoke(connection, method, args);
                        if (result instanceof PreparedStatement statement) {
                            return wrap(statement, updates, failingUpdate);
                        }
                        return result;
                    });
        };
    }

    private static PreparedStatement wrap(PreparedStatement statement, AtomicInteger updates, int failingUpdate) {
        return (PreparedStatement) Proxy.newProxyInstance(FailingConnections.class.getClassLoader(),
                new Class<?>[] {PreparedStatement.class}, (proxy, method, args) -> {
                    if (WRITES.contains(method.getName()) && (args == null || args.length == 0)
                            && updates.incrementAndGet() == failingUpdate) {
                        throw new SQLException("Simulated failure of write number " + failingUpdate, "08006");
                    }
                    return invoke(statement, method, args);
                });
    }

    private static Object invoke(Object target, java.lang.reflect.Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }
}
