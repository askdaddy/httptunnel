/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jboss.netty.channel.socket.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.ServerSocketChannel;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author The Netty Project (netty-dev@lists.jboss.org)
 * @author Iain McGinniss (iain.mcginniss@onedrum.com)
 */
@RunWith(JMock.class)
public class HttpTunnelServerChannelSinkTest {

    private final JUnit4Mockery mockContext = new JUnit4Mockery();

    private HttpTunnelServerChannelSink sink;

    private ChannelPipeline pipeline;

    private FakeSocketChannel channel;

    ServerSocketChannel realChannel;

    ChannelFuture realFuture;

    Throwable exceptionInPipeline;

    @Before
    public void setUp() throws Exception {
        realChannel = mockContext.mock(ServerSocketChannel.class);
        pipeline = Channels.pipeline();
        pipeline.addLast("exceptioncatcher", new ExceptionCatcher());
        sink = new HttpTunnelServerChannelSink();
        sink.setRealChannel(realChannel);
        channel = new FakeSocketChannel(null, null, pipeline, sink);
        realFuture = Channels.future(realChannel);
    }

    @After
    public void teardown() throws Exception {
        assertTrue("exception caught in pipeline: " + exceptionInPipeline,
                exceptionInPipeline == null);
    }

    public void testCloseRequest() throws Exception {
        mockContext.checking(new Expectations() {
            {
                one(realChannel).close();
                will(returnValue(realFuture));
            }
        });

        ChannelFuture virtualFuture1 = Channels.close(channel);
        mockContext.assertIsSatisfied();
        ChannelFuture virtualFuture = virtualFuture1;
        realFuture.setSuccess();
        assertTrue(virtualFuture.isSuccess());
    }

    @Test
    public void testUnbindRequest_withSuccess() throws Exception {
        ChannelFuture virtualFuture = checkUnbind();
        realFuture.setSuccess();
        assertTrue(virtualFuture.isSuccess());
    }

    @Test
    public void testUnbindRequest_withFailure() throws Exception {
        ChannelFuture virtualFuture = checkUnbind();
        realFuture.setFailure(new Exception("Something bad happened"));
        assertFalse(virtualFuture.isSuccess());
    }

    private ChannelFuture checkUnbind() {
        mockContext.checking(new Expectations() {
            {
                one(realChannel).unbind();
                will(returnValue(realFuture));
            }
        });

        ChannelFuture virtualFuture = Channels.unbind(channel);
        mockContext.assertIsSatisfied();
        return virtualFuture;
    }

    @Test
    public void testBindRequest_withSuccess() {
        ChannelFuture virtualFuture = checkBind();
        realFuture.setSuccess();
        assertTrue(virtualFuture.isSuccess());
    }

    @Test
    public void testBindRequest_withFailure() {
        ChannelFuture virtualFuture = checkBind();
        realFuture.setFailure(new Exception("Something bad happened"));
        assertFalse(virtualFuture.isSuccess());
    }

    private ChannelFuture checkBind() {
        final SocketAddress toAddress = new InetSocketAddress(80);
        mockContext.checking(new Expectations() {
            {
                one(realChannel).bind(toAddress);
                will(returnValue(realFuture));
            }
        });

        ChannelFuture virtualFuture = Channels.bind(channel, toAddress);
        return virtualFuture;
    }

    private final class ExceptionCatcher extends SimpleChannelUpstreamHandler {

        ExceptionCatcher() {
            super();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
                throws Exception {
            exceptionInPipeline = e.getCause();
        }
    }
}
