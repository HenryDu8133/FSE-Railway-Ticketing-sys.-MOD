package net.fsefmgftc.fseticket.item;

import java.util.List;

import net.fsefmgftc.fseticket.util.TicketDataUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTicketItem extends Item {
    protected AbstractTicketItem(Item.Properties properties, String ticketType) {
        super(properties.component(DataComponents.CUSTOM_DATA, CustomData.of(TicketDataUtil.createBaseTicketTag(ticketType))));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        CompoundTag ticketData = getTicketData(stack);
        String from = TicketDataUtil.normalizedDisplay(ticketData.getString(TicketDataUtil.START_NAME_EN));
        String to = TicketDataUtil.normalizedDisplay(ticketData.getString(TicketDataUtil.TERMINAL_NAME_EN));
        int rides = ticketData.getInt(TicketDataUtil.RIDES);
        boolean entered = ticketData.getBoolean(TicketDataUtil.ENTERED);
        boolean exited = ticketData.getBoolean(TicketDataUtil.EXITED);

        tooltip.add(Component.translatable("tooltip.fseticket.route", from, to));
        tooltip.add(Component.translatable("tooltip.fseticket.cost", String.format("%.2f", ticketData.getDouble(TicketDataUtil.COST))));
        tooltip.add(Component.translatable("tooltip.fseticket.rides", rides));
        tooltip.add(Component.translatable("tooltip.fseticket.ticket_id", ticketData.getString(TicketDataUtil.TICKET_ID)));
        tooltip.add(Component.translatable(
                entered
                        ? "tooltip.fseticket.entered"
                        : exited
                          ? "tooltip.fseticket.exited"
                          : "tooltip.fseticket.not_entered"
        ));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack heldItem = serverPlayer.getItemInHand(hand);
            CompoundTag ticketData = getTicketData(heldItem);

            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return Component.literal(getMenuTitle());
                }

                @Override
                public AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player menuPlayer) {
                    FriendlyByteBuf buffer = TicketDataUtil.createBuffer();
                    TicketDataUtil.writeTicketMenuData(buffer, player.blockPosition(), hand, ticketData);
                    return createTicketMenu(id, inventory, buffer);
                }
            }, buffer -> TicketDataUtil.writeTicketMenuData(buffer, player.blockPosition(), hand, ticketData));
        }

        return super.use(level, player, hand);
    }

    protected CompoundTag getTicketData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    protected abstract String getMenuTitle();

    protected abstract AbstractContainerMenu createTicketMenu(int id, Inventory inventory, FriendlyByteBuf buffer);
}
