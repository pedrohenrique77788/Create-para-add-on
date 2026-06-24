package com.simibubi.create.content.equipment.wrench;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

public class ChaveEnergiaItem extends Item {

    public ChaveEnergiaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.PASS;

        BlockPos posClicado = context.getClickedPos();
        var itemStack = context.getItemInHand();
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag nbt = customData.copyTag();

        if (nbt.contains("X_Fonte")) {
            // Segundo clique - aplicar energia
            BlockPos posFonte = new BlockPos(
                nbt.getInt("X_Fonte"),
                nbt.getInt("Y_Fonte"),
                nbt.getInt("Z_Fonte")
            );

            if (!(level.getBlockEntity(posFonte) instanceof KineticBlockEntity fonteKbe) ||
                !(level.getBlockEntity(posClicado) instanceof KineticBlockEntity destinoKbe)) {
                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(Component.literal("§cErro: Um dos blocos não é compatível com energia cinética."));
                }
                return InteractionResult.FAIL;
            }

            float velocidade = fonteKbe.getSpeed();
            if (velocidade == 0) {
                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(Component.literal("§cA fonte está parada!"));
                }
                return InteractionResult.SUCCESS;
            }

            // Aplicar velocidade de forma mais segura
            destinoKbe.setSpeed(velocidade);
            destinoKbe.updateFromNetwork(velocidade, fonteKbe.getStressLimit(), fonteKbe.getAddedStressCapacity());
            destinoKbe.notifyUpdate();
            destinoKbe.sendData();

            if (context.getPlayer() != null) {
                context.getPlayer().sendSystemMessage(
                    Component.literal("§aEnergia transferida! Velocidade: " + velocidade + " RPM")
                );
            }

            // Limpa o item
            nbt.remove("X_Fonte");
            nbt.remove("Y_Fonte");
            nbt.remove("Z_Fonte");
            itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

        } else {
            // Primeiro clique - salvar fonte
            nbt.putInt("X_Fonte", posClicado.getX());
            nbt.putInt("Y_Fonte", posClicado.getY());
            nbt.putInt("Z_Fonte", posClicado.getZ());
            itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

            if (context.getPlayer() != null) {
                context.getPlayer().sendSystemMessage(
                    Component.literal("§eFonte salva: " + posClicado.toShortString() + " §7(Clique no destino agora)")
                );
            }
        }

        return InteractionResult.SUCCESS;
    }
}
