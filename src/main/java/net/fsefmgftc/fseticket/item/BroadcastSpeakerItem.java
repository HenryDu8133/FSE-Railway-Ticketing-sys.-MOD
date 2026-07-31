package net.fsefmgftc.fseticket.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

public class BroadcastSpeakerItem extends BlockItem {

    public BroadcastSpeakerItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && !level.isClientSide()) {
            checkBindings(player, itemstack);
            return InteractionResultHolder.success(itemstack);
        }
        return InteractionResultHolder.pass(itemstack);
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            if (!context.getLevel().isClientSide()) {
                checkBindings(context.getPlayer(), context.getItemInHand());
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    private void checkBindings(Player player, ItemStack itemstack) {
        CompoundTag tag = itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag != null && tag.contains("BoundHostNames", Tag.TAG_LIST)) {
            ListTag namesList = tag.getList("BoundHostNames", Tag.TAG_STRING);
            player.sendSystemMessage(Component.literal("当前扬声器绑定的广播主机："));
            for (int i = 0; i < namesList.size(); i++) {
                player.sendSystemMessage(Component.literal("- " + namesList.getString(i)));
            }
            if (namesList.isEmpty()) {
                player.sendSystemMessage(Component.literal("暂无绑定"));
            }
        } else {
            player.sendSystemMessage(Component.literal("当前扬声器暂无绑定的广播主机。"));
        }
    }
}
