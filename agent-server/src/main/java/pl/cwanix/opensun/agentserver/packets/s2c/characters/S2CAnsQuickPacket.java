package pl.cwanix.opensun.agentserver.packets.s2c.characters;

import org.apache.commons.lang3.ArrayUtils;
import pl.cwanix.opensun.commonserver.packets.Packet;
import pl.cwanix.opensun.commonserver.packets.PacketCategory;
import pl.cwanix.opensun.commonserver.packets.annotations.OutgoingPacket;
import pl.cwanix.opensun.utils.datatypes.FixedLengthField;

@OutgoingPacket(category = PacketCategory.CHARACTER, type = (byte) 0xBE)
public class S2CAnsQuickPacket implements Packet {
	
	private FixedLengthField value;
	
	public S2CAnsQuickPacket() {
		value = new FixedLengthField(1);
	}

	@Override
	public Object[] getOrderedFields() {
		return ArrayUtils.toArray(value);
	}
}
