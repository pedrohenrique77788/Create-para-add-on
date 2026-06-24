package com.simibubi.create.content.equipment.wrench;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ChaveEnergiaItem extends Item {


    public ChaveEnergiaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        
        if (!level.isClientSide) {
            BlockPos posClicado = context.getClickedPos();
            CompoundTag nbt = context.getItemInHand().getOrCreateTag();

            if (nbt.contains("X_Fonte")) {
                int fonteX = nbt.getInt("X_Fonte");
                int fonteY = nbt.getInt("Y_Fonte");
                int fonteZ = nbt.getInt("Z_Fonte");
                BlockPos posFonte = new BlockPos(fonteX, fonteY, fonteZ);

                // --- AQUI ENTRA A MÁGICA DO CREATE MOD ---
                
                // 1. Buscamos as entidades mecânicas (Block Entities) da Fonte e do Destino
                var blocoFonteEntity = level.getBlockEntity(posFonte);
                var blocoDestinoEntity = level.getBlockEntity(posClicado);

                // 2. Verificamos se ambos os blocos são blocos cinéticos/mecânicos do Create
                if (blocoFonteEntity instanceof com.simibubi.create.content.kinetics.base.KineticBlockEntity fonteKbe &&
                    blocoDestinoEntity instanceof com.simibubi.create.content.kinetics.base.KineticBlockEntity destinoKbe) {
                    
                    // 3. Pegamos a velocidade atual do bloco gerador
                    float velocidadeFonte = fonteKbe.getSpeed();

                    if (velocidadeFonte != 0) {
                        // 4. Injetamos diretamente a velocidade e atualizamos a rede de estresse (SU)
                        destinoKbe.setSpeed(velocidadeFonte);
                        
                        // Avisa o Create que a rede mecânica mudou e precisa recalcular o estresse
                        destinoKbe.updateFromNetwork(velocidadeFonte, fonteKbe.getStressLimit(), fonteKbe.getAddedStressCapacity());
                        destinoKbe.notifyUpdate();
                        
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendSystemMessage(
                                Component.literal("§aLink de energia estabelecido! Velocidade transmitida: " + velocidadeFonte + " RPM")
                            );
                        }
                    } else {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendSystemMessage(Component.literal("§cErro: A fonte selecionada está parada!"));
                        }
                    }
                } else {
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendSystemMessage(Component.literal("§cErro: Um ou ambos os blocos não são compatíveis com energia mecânica."));
                    }
                }
                
                // Limpa a memória da ferramenta para a próxima conexão
                nbt.remove("X_Fonte");
                nbt.remove("Y_Fonte");
                nbt.remove("Z_Fonte");
                
            } else {
                nbt.putInt("X_Fonte", posClicado.getX());
                nbt.putInt("Y_Fonte", posClicado.getY());
                nbt.putInt("Z_Fonte", posClicado.getZ());
                
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
