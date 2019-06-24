package pl.cwanix.opensun.commonserver;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.client.RestTemplate;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import pl.cwanix.opensun.commonserver.packets.IncomingPacket;
import pl.cwanix.opensun.commonserver.packets.Packet;
import pl.cwanix.opensun.commonserver.packets.PacketException;
import pl.cwanix.opensun.commonserver.properties.SUNServerProperties;
import pl.cwanix.opensun.commonserver.server.SUNServer;
import pl.cwanix.opensun.commonserver.server.SUNServerChannelHandlerFactory;
import pl.cwanix.opensun.commonserver.server.SUNServerChannelInitializer;
import pl.cwanix.opensun.commonserver.server.messages.PacketDecoder;
import pl.cwanix.opensun.commonserver.server.messages.PacketEncoder;
import pl.cwanix.opensun.utils.functions.ThrowingFunction;
import pl.cwanix.opensun.utils.packets.PacketHeader;

@Configuration
public class SUNServerAutoConfiguration {
	
	@Bean
	@ConditionalOnMissingBean
	public EventExecutorGroup eventExecutorGroup(SUNServerProperties properties) {
		return new DefaultEventExecutorGroup(properties.getClient().getMaxThreadCount());
	}
	
	@Bean
	@ConditionalOnMissingBean
	public PacketDecoder packetDecoder(Map<PacketHeader, ThrowingFunction<byte[], Packet, Exception>> clientPacketDefinitions, RestTemplate restTemplate) {
		return new PacketDecoder(clientPacketDefinitions);
	}
	
	@Bean
	@ConditionalOnMissingBean
	public PacketEncoder packetEncoder() {
		return new PacketEncoder();
	}
	
	@Bean
	@ConditionalOnMissingBean
	public ChannelInitializer<SocketChannel> sunServerChannelInitializer(EventExecutorGroup eventExecutorGroup, SUNServerChannelHandlerFactory sunServerChannelHandlerFactory, PacketDecoder packetDecoder, PacketEncoder packetEncoder) {
		return new SUNServerChannelInitializer(eventExecutorGroup, sunServerChannelHandlerFactory, packetDecoder, packetEncoder);
	}
	
	@Bean
	@ConditionalOnMissingBean
	public SUNServer sunServer(ChannelInitializer<SocketChannel> sunServerChannelHandler, SUNServerProperties properties) {		
		return new SUNServer(sunServerChannelHandler, properties);
	}
	
	@Bean
	@ConditionalOnMissingBean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
	
	@Bean
	@ConditionalOnMissingBean
	@SuppressWarnings("unchecked")
	public Map<PacketHeader, ThrowingFunction<byte[], Packet, Exception>> clientPacketDefinitions() throws Exception {
		ClassPathScanningCandidateComponentProvider classPathScanner = new ClassPathScanningCandidateComponentProvider(false);
		classPathScanner.addIncludeFilter(new AnnotationTypeFilter(IncomingPacket.class));
		
		Map<PacketHeader, ThrowingFunction<byte[], Packet, Exception>> definitions = new HashMap<>();
		
		for (BeanDefinition packetDefinition : classPathScanner.findCandidateComponents("pl.cwanix.opensun")) {
			Class<? extends Packet> packetClass = (Class<? extends Packet>) Class.forName(packetDefinition.getBeanClassName());
			
			ThrowingFunction<byte[], Packet, Exception> packetConstructorFunction = t -> {
				try {
					return packetClass.getConstructor(byte[].class).newInstance(t);
				} catch (Exception e) {
					throw new PacketException("Unable to parse packet", e);
				}
			};
			
			definitions.put((PacketHeader) packetClass.getDeclaredField("PACKET_ID").get(null), packetConstructorFunction);
		}
		
		return definitions;
	}
}
