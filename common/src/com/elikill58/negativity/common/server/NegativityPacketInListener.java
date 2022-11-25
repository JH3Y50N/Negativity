package com.elikill58.negativity.common.server;

import java.util.Locale;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.block.Block;
import com.elikill58.negativity.api.entity.Entity;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.EventListener;
import com.elikill58.negativity.api.events.EventManager;
import com.elikill58.negativity.api.events.Listeners;
import com.elikill58.negativity.api.events.block.BlockBreakEvent;
import com.elikill58.negativity.api.events.packets.PacketReceiveEvent;
import com.elikill58.negativity.api.events.player.PlayerChatEvent;
import com.elikill58.negativity.api.events.player.PlayerCommandPreProcessEvent;
import com.elikill58.negativity.api.events.player.PlayerDamageEntityEvent;
import com.elikill58.negativity.api.events.player.PlayerMoveEvent;
import com.elikill58.negativity.api.events.player.PlayerToggleActionEvent;
import com.elikill58.negativity.api.events.player.PlayerToggleActionEvent.ToggleAction;
import com.elikill58.negativity.api.packets.PacketType;
import com.elikill58.negativity.api.packets.packet.NPacket;
import com.elikill58.negativity.api.packets.packet.playin.NPacketPlayInBlockDig;
import com.elikill58.negativity.api.packets.packet.playin.NPacketPlayInBlockDig.DigAction;
import com.elikill58.negativity.api.packets.packet.playin.NPacketPlayInChat;
import com.elikill58.negativity.api.packets.packet.playin.NPacketPlayInEntityAction;
import com.elikill58.negativity.api.packets.packet.playin.NPacketPlayInFlying;
import com.elikill58.negativity.api.packets.packet.playin.NPacketPlayInUseEntity;
import com.elikill58.negativity.api.packets.packet.playin.NPacketPlayInUseEntity.EnumEntityUseAction;

public class NegativityPacketInListener implements Listeners {

	@EventListener
	public void onPacketReceive(PacketReceiveEvent e) {
		if (!e.hasPlayer() || e.getPacket().getPacketType() == null)
			return;
		Player p = e.getPlayer();
		NPacket packet = e.getPacket();
		NegativityPlayer np = NegativityPlayer.getNegativityPlayer(p);
		PacketType type = packet.getPacketType();
		if (type == PacketType.Client.BLOCK_DIG && packet instanceof NPacketPlayInBlockDig) {
			NPacketPlayInBlockDig blockDig = (NPacketPlayInBlockDig) packet;
			if (blockDig.action != DigAction.FINISHED_DIGGING)
				return;

			Block b = blockDig.getBlock(p.getWorld());
			BlockBreakEvent event = new BlockBreakEvent(p, b);
			EventManager.callEvent(event);
			if (event.isCancelled())
				e.setCancelled(event.isCancelled());
		} else if (type == PacketType.Client.USE_ENTITY) {
			np.isAttacking = true;
			NPacketPlayInUseEntity useEntityPacket = (NPacketPlayInUseEntity) packet;
			if (useEntityPacket.action.equals(EnumEntityUseAction.ATTACK)) {
				for (Entity entity : p.getWorld().getEntities()) {
					if (entity.isSameId(String.valueOf(useEntityPacket.entityId))) {
						PlayerDamageEntityEvent event = new PlayerDamageEntityEvent(p, entity, false);
						EventManager.callEvent(event);
						if (event.isCancelled())
							e.setCancelled(event.isCancelled());
					}
				}
			}
		} else if (type.isFlyingPacket()) {
			NPacketPlayInFlying flying = (NPacketPlayInFlying) packet;
			if (flying.hasLocation()) {
				if (p.getLocation() == null) {
					p.setLocation(flying.getLocation(p.getWorld()));
				}
				PlayerMoveEvent moveEvent = new PlayerMoveEvent(p, p.getLocation(), flying.getLocation(p.getWorld()));
				EventManager.callEvent(moveEvent);
				if (moveEvent.isCancelled())
					e.setCancelled(true);
				else
					p.setLocation(flying.getLocation(p.getWorld()));
			}
		} else if (packet instanceof NPacketPlayInEntityAction) {
			NPacketPlayInEntityAction action = (NPacketPlayInEntityAction) packet;

			switch (action.action) {
			case LEAVE_BED:
				p.setSleeping(false);
				break;
			case OPEN_INVENTORY:
				break;
			case START_FALL_FLYING:
				break;
			case START_RIDING_JUMP:
				break;
			case STOP_RIDING_JUMP:
				break;
			case STOP_SLEEPING:
				p.setSleeping(false);
				break;
			case START_SNEAKING:
				EventManager.callEvent(new PlayerToggleActionEvent(p, ToggleAction.SNEAK, false));
				p.setSneaking(true);
				break;
			case STOP_SNEAKING:
				p.setSneaking(false);
				break;
			case START_SPRINTING:
				EventManager.callEvent(new PlayerToggleActionEvent(p, ToggleAction.SPRINT, false));
				p.setSprinting(true);
				break;
			case STOP_SPRINTING:
				p.setSprinting(false);
				break;
			}
		} else if (packet instanceof NPacketPlayInChat) {
			NPacketPlayInChat chat = (NPacketPlayInChat) packet;
			if (chat.message.startsWith("/")) {
				String cmd = chat.message.substring(1).split(" ")[0];
				String cmdArg = chat.message.substring(cmd.length() + 1); // +1 for the '/'
				if (cmdArg.startsWith(" "))
					cmdArg = cmdArg.substring(1);
				String[] arg = cmdArg.replace(" ", "").isEmpty() ? new String[0] : cmdArg.split(" ");
				String prefix = arg.length == 0 ? "" : arg[arg.length - 1].toLowerCase(Locale.ROOT);
				PlayerCommandPreProcessEvent preProcess = new PlayerCommandPreProcessEvent(p, cmd, arg, prefix, false);
				EventManager.callEvent(preProcess);
				if (preProcess.isCancelled())
					e.setCancelled(true);
			} else {
				PlayerChatEvent chatEvent = new PlayerChatEvent(p, chat.message);
				EventManager.callEvent(chatEvent);
				e.setCancelled(chatEvent.isCancelled());
			}
		}
	}
}