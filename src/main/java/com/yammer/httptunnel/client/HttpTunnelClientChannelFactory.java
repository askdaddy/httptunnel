/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.yammer.httptunnel.client;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;

/**
 * Factory used to create new client channels.
 *
 * @author The Netty Project (netty-dev@lists.jboss.org)
 * @author Iain McGinniss (iain.mcginniss@onedrum.com)
 * @author Jamie Furness (jamie@onedrum.com)
 * @author OneDrum Ltd.
 */
public class HttpTunnelClientChannelFactory implements ClientSocketChannelFactory {

	private final ClientSocketChannelFactory factory;
	private final ChannelGroup realConnections;

	public HttpTunnelClientChannelFactory(ClientSocketChannelFactory factory) {
		this.factory = factory;

		realConnections = new DefaultChannelGroup();
	}

	@Override
	public HttpTunnelClientChannel newChannel(ChannelPipeline pipeline) {
		return new HttpTunnelClientChannel(this, pipeline, new HttpTunnelClientChannelSink(), factory, realConnections);
	}

	@Override
	public void releaseExternalResources() {
		realConnections.close().awaitUninterruptibly();
		factory.releaseExternalResources();
	}
}
