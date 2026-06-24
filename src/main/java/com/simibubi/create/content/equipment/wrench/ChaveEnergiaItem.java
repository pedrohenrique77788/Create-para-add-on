package com.simibubi.create.content.equipment.wrench;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.CustomData;

public class ChaveEnergiaItem extends Item {

    public ChaveEnergiaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        
        if (!level.isClientSide) {
            BlockPos posClicado = context.getClickedPos();
            var itemStack = context.getItemInHand();
            
            // Nova forma de pegar ou criar a tag customizada no Minecraft moderno
            CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag nbt = customData.copyTag();

            if (nbt.contains("X_Fonte")) {
                int fonteX = nbt.getInt("X_Fonte");
                int fonteY = nbt.getInt("Y_Fonte");
                int fonteZ = nbt.getInt("Z_Fonte");
                BlockPos posFonte = new BlockPos(fonteX, fonteY, fonteZ);

                var blocoFonteEntity = level.getBlockEntity(posFonte);
                var blocoDestinoEntity = level.getBlockEntity(posClicado);

                if (blocoFonteEntity instanceof com.simibubi.create.content.kinetics.base.KineticBlockEntity fonteKbe &&
                    blocoDestinoEntity instanceof com.simibubi.create.content.kinetics.base.KineticBlockEntity destinoKbe) {
                    
                    float velocidadeFonte = fonteKbe.getSpeed();

                    if (velocidadeFonte != 0) {
                        destinoKbe.setSpeed(velocidadeFonte);
                        destinoKbe.updateFromNetwork(velocidadeFonte, fonteKbe.getStressLimit(), fonteKbe.getAddedStressCapacity());
                        destinoKbe.notifyUpdate();
                        
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendSystemMessage(
                                Component.literal("§aLink de energia estabelecido! Velocidade: " + velocidadeFonte + " RPM")
                            );
                        }
                    } else {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendSystemMessage(Component.literal("§cErro: A fonte selecionada está parada!"));
                        }
                    }
                } else {
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendSystemMessage(Component.literal("§cErro: Blocos incompatíveis."));
                    }
                }
                
                // Remove e limpa os dados salvando de volta no item
                nbt.remove("X_Fonte");
                nbt.remove("Y_Fonte");
                nbt.remove("Z_Fonte");
                itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                
            } else {
                // Guarda a nova fonte e salva no item usando Data Components
                nbt.putInt("X_Fonte", posClicado.getX());
                nbt.putInt("Y_Fonte", posClicado.getY());
                nbt.putInt("Z_Fonte", posClicado.getZ());
                itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                
                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(
                        Component.literal("§eFonte mecânica guardada: " + posClicado.toShortString())
                    );
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
