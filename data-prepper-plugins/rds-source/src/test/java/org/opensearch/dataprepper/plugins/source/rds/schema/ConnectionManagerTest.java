/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.plugins.source.rds.schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.opensearch.dataprepper.plugins.source.rds.schema.ConnectionManager.FALSE_VALUE;
import static org.opensearch.dataprepper.plugins.source.rds.schema.ConnectionManager.PASSWORD_KEY;
import static org.opensearch.dataprepper.plugins.source.rds.schema.ConnectionManager.REQUIRE_SSL_KEY;
import static org.opensearch.dataprepper.plugins.source.rds.schema.ConnectionManager.TINY_INT_ONE_IS_BIT_KEY;
import static org.opensearch.dataprepper.plugins.source.rds.schema.ConnectionManager.TRUE_VALUE;
import static org.opensearch.dataprepper.plugins.source.rds.schema.ConnectionManager.USERNAME_KEY;
import static org.opensearch.dataprepper.plugins.source.rds.schema.ConnectionManager.USE_SSL_KEY;


class ConnectionManagerTest {

    private String hostName;
    private int port;
    private String username;
    private String password;
    private boolean requireSSL;
    private final Random random = new Random();

    @BeforeEach
    void setUp() {
        hostName = UUID.randomUUID().toString();
        port = random.nextInt();
        username = UUID.randomUUID().toString();
        password = UUID.randomUUID().toString();
    }

    @Test
    void test_getConnection_when_requireSSL_is_true() throws SQLException {
        requireSSL = true;
        final ConnectionManager connectionManager = spy(createObjectUnderTest());
        final ArgumentCaptor<String> jdbcUrlArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Properties> propertiesArgumentCaptor = ArgumentCaptor.forClass(Properties.class);
        doReturn(mock(Connection.class)).when(connectionManager).doGetConnection(jdbcUrlArgumentCaptor.capture(), propertiesArgumentCaptor.capture());

        connectionManager.getConnection();

        assertThat(jdbcUrlArgumentCaptor.getValue(), is(String.format(ConnectionManager.JDBC_URL_FORMAT, hostName, port)));
        final Properties properties = propertiesArgumentCaptor.getValue();
        assertThat(properties.getProperty(USERNAME_KEY), is(username));
        assertThat(properties.getProperty(PASSWORD_KEY), is(password));
        assertThat(properties.getProperty(USE_SSL_KEY), is(TRUE_VALUE));
        assertThat(properties.getProperty(REQUIRE_SSL_KEY), is(TRUE_VALUE));
        assertThat(properties.getProperty(TINY_INT_ONE_IS_BIT_KEY), is(FALSE_VALUE));
    }

    @Test
    void test_getConnection_when_requireSSL_is_false() throws SQLException {
        requireSSL = false;
        final ConnectionManager connectionManager = spy(createObjectUnderTest());
        final ArgumentCaptor<String> jdbcUrlArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Properties> propertiesArgumentCaptor = ArgumentCaptor.forClass(Properties.class);
        doReturn(mock(Connection.class)).when(connectionManager).doGetConnection(jdbcUrlArgumentCaptor.capture(), propertiesArgumentCaptor.capture());

        connectionManager.getConnection();

        assertThat(jdbcUrlArgumentCaptor.getValue(), is(String.format(ConnectionManager.JDBC_URL_FORMAT, hostName, port)));
        final Properties properties = propertiesArgumentCaptor.getValue();
        assertThat(properties.getProperty(USERNAME_KEY), is(username));
        assertThat(properties.getProperty(PASSWORD_KEY), is(password));
        assertThat(properties.getProperty(USE_SSL_KEY), is(FALSE_VALUE));
        assertThat(properties.getProperty(TINY_INT_ONE_IS_BIT_KEY), is(FALSE_VALUE));
    }

    private ConnectionManager createObjectUnderTest() {
        return new ConnectionManager(hostName, port, username, password, requireSSL);
    }
}