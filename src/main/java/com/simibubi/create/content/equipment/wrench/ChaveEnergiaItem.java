package com.simibubi.create.content.equipment.wrench;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
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
        
        // Pega ou cria a tag NBT padrão do sistema da 1.20.1
        CompoundTag nbt = itemStack.getOrCreateTag();

        if (nbt.contains("X_Fonte")) {
            // Segundo clique - aplicar energia
            BlockPos posFonte = new BlockPos(
                nbt.getInt("X_Fonte"),
                nbt.getInt("Y_Fonte"),
                nbt.getInt("Z_Fonte")
            );

            if (level.getBlockEntity(posFonte) instanceof KineticBlockEntity fonteKbe &&
                level.getBlockEntity(posClicado) instanceof KineticBlockEntity destinoKbe) {

                float velocidade = fonteKbe.getSpeed();
                if (velocidade == 0) {
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendSystemMessage(Component.literal("§cA fonte está parada!"));
                    }
                    return InteractionResult.SUCCESS;
                }

                // Transferir velocidade
                destinoKbe.setSpeed(velocidade);
                destinoKbe.notifyUpdate();

                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(
                        Component.literal("§aEnergia transferida! Velocidade: " + (int)velocidade + " RPM")
                    );
                }

                // Limpa o item removendo as tags
                nbt.remove("X_Fonte");
                nbt.remove("Y_Fonte");
                nbt.remove("Z_Fonte");

            } else {
                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(Component.literal("§cNão foi possível transferir energia! Ambos os blocos precisam ser Kinetic."));
                }
                return InteractionResult.FAIL;
            }

        } else {
            // Primeiro clique - salvar fonte
            nbt.putInt("X_Fonte", posClicado.getX());
            nbt.putInt("Y_Fonte", posClicado.getY());
            nbt.putInt("Z_Fonte", posClicado.getZ());

            if (context.getPlayer() != null) {
                context.getPlayer().sendSystemMessage(
                    Component.literal("§eFonte salva em " + posClicado.toShortString() + " §7(Clique agora no destino)")
                );
            }
        }

        return InteractionResult.SUCCESS;
    }
}
