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

package com.yammer.httptunnel.server;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.ServerSocketChannel;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.yammer.httptunnel.FakeChannelSink;
import com.yammer.httptunnel.FakeServerSocketChannel;

/**
 * @author The Netty Project (netty-dev@lists.jboss.org)
 * @author Iain McGinniss (iain.mcginniss@onedrum.com)
 */
@RunWith(JMock.class)
public class HttpTunnelServerChannelFactoryTest {

	private final JUnit4Mockery mockContext = new JUnit4Mockery();

	ServerSocketChannelFactory realChannelFactory;

	private HttpTunnelServerChannelFactory factory;

	ServerSocketChannel realChannel;

	@Before
	public void setUp() throws Exception {
		realChannelFactory = mockContext.mock(ServerSocketChannelFactory.class);
		factory = new HttpTunnelServerChannelFactory(realChannelFactory);
		ChannelPipeline pipeline = Channels.pipeline(new SimpleChannelHandler());
		realChannel = new FakeServerSocketChannel(factory, pipeline,
				new FakeChannelSink());
	}

	@Test
	public void testNewChannel() {
		mockContext.checking(new Expectations() {
			{
				one(realChannelFactory).newChannel(
						with(any(ChannelPipeline.class)));
				will(returnValue(realChannel));
			}
		});
		ChannelPipeline pipeline = Channels.pipeline(new SimpleChannelHandler());
		HttpTunnelServerChannel newChannel = factory.newChannel(pipeline);
		assertNotNull(newChannel);
		assertSame(pipeline, newChannel.getPipeline());
	}

	@Test
	public void testNewChannel_forwardsWrappedFactoryFailure() {
		final ChannelException innerException = new ChannelException();
		mockContext.checking(new Expectations() {
			{
				one(realChannelFactory).newChannel(
						with(any(ChannelPipeline.class)));
				will(throwException(innerException));
			}
		});

		try {
			factory.newChannel(Channels.pipeline(new SimpleChannelHandler()));
			fail("Expected ChannelException");
		} catch (ChannelException e) {
			assertSame(innerException, e);
		}
	}

	// @Test
	// public void testChannelCreation_withServerBootstrap() {
	// mockContext.checking(new Expectations() {{
	// one(realChannelFactory).newChannel(with(any(ChannelPipeline.class)));
	// will(returnValue(realChannel));
	// }});
	//
	// ServerBootstrap bootstrap = new ServerBootstrap(factory);
	// Channel newChannel = bootstrap.bind(new InetSocketAddress(80));
	// assertNotNull(newChannel);
	//
	// }

}
